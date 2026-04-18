package vn.hust.agilechatbotbackend.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import vn.hust.agilechatbotbackend.dto.ChatRequest;
import vn.hust.agilechatbotbackend.dto.LlmRequest;
import vn.hust.agilechatbotbackend.entity.Conversation;
import vn.hust.agilechatbotbackend.entity.Message;
import vn.hust.agilechatbotbackend.service.ConversationService;
import vn.hust.agilechatbotbackend.service.MessageService;
import vn.hust.agilechatbotbackend.service.llm.LlmRouter;
import vn.hust.agilechatbotbackend.service.prompt.PromptAssembler;
import vn.hust.agilechatbotbackend.service.prompt.SummaryGenerator;

import java.io.IOException;

/**
 * Chat endpoint for AI bot conversations via SSE streaming.
 * Full pipeline: session resolution → save message → assemble prompt → stream LLM → save response → check summary
 */
@RestController
@RequestMapping("/api/v1/chatbot")
@RequiredArgsConstructor
@Slf4j
public class ChatbotController {

    private final ConversationService conversationService;
    private final MessageService messageService;
    private final PromptAssembler promptAssembler;
    private final LlmRouter llmRouter;
    private final SummaryGenerator summaryGenerator;

    @PostMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chat(@RequestBody ChatRequest request) {
        SseEmitter emitter = new SseEmitter(120_000L); // 2 min timeout

        // Run the full pipeline in a separate thread to not block the servlet thread
        Thread.startVirtualThread(() -> {
            try {
                // 1. Session resolution
                Conversation conversation = conversationService.resolveConversation(
                        request.getUserId(), request.getUsername(), request.getSessionId());

                // Send session_id back as first SSE event
                emitter.send(SseEmitter.event()
                        .name("session")
                        .data("{\"sessionId\":\"" + conversation.getSessionId() + "\","
                                + "\"conversationId\":" + conversation.getId() + "}"));

                // 2. Save user message
                messageService.saveUserMessage(conversation, request.getUserId(), request.getMessage());

                // 3. Assemble prompt (system prompt + RAG + context)
                LlmRequest llmRequest = promptAssembler.assemble(conversation, request.getMessage());

                // 4. Stream LLM response + accumulate full response
                StringBuilder fullResponse = new StringBuilder();

                llmRouter.streamChat(llmRequest)
                        .doOnNext(chunk -> {
                            try {
                                fullResponse.append(chunk);
                                emitter.send(SseEmitter.event()
                                        .name("message")
                                        .data(chunk));
                            } catch (IOException e) {
                                log.warn("SSE send failed: {}", e.getMessage());
                            }
                        })
                        .doOnComplete(() -> {
                            try {
                                // 5. Save bot response
                                Message botMessage = messageService.saveBotMessage(
                                        conversation, fullResponse.toString());

                                // 6. Check if summary is needed (async)
                                if (summaryGenerator.needsSummary(conversation)) {
                                    summaryGenerator.generateSummaryAsync(conversation);
                                }

                                // Send completion event
                                emitter.send(SseEmitter.event()
                                        .name("done")
                                        .data("{\"messageId\":" + botMessage.getId() + "}"));
                                emitter.complete();
                            } catch (IOException e) {
                                log.error("SSE completion failed: {}", e.getMessage());
                                emitter.completeWithError(e);
                            }
                        })
                        .doOnError(e -> {
                            log.error("LLM streaming error: {}", e.getMessage());
                            emitter.completeWithError(e);
                        })
                        .subscribe();

            } catch (Exception e) {
                log.error("Chat pipeline error: {}", e.getMessage(), e);
                try {
                    emitter.send(SseEmitter.event()
                            .name("error")
                            .data("{\"error\":\"" + e.getMessage() + "\"}"));
                } catch (IOException ignored) {
                }
                emitter.completeWithError(e);
            }
        });

        return emitter;
    }
}
