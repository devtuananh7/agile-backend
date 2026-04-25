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

import java.io.IOException;

/**
 * Public chat endpoint for anonymous users.
 * Requires X-API-Key header (validated by PublicApiKeyFilter).
 * Does NOT require Firebase authentication.
 *
 * Pipeline: session resolution → message limit check → save message → assemble prompt → stream LLM → save response
 * SSE events: session, message, done, limit_reached, error
 */
@RestController
@RequestMapping("/api/v1/public")
@RequiredArgsConstructor
@Slf4j
public class AnonymousChatController {

    private static final int MAX_MESSAGES_PER_SESSION = 10;

    private final ConversationService conversationService;
    private final MessageService messageService;
    private final PromptAssembler promptAssembler;
    private final LlmRouter llmRouter;

    @PostMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chat(@RequestBody ChatRequest request) {
        SseEmitter emitter = new SseEmitter(120_000L); // 2 min timeout

        Thread.startVirtualThread(() -> {
            try {
                // 1. Resolve anonymous conversation (create or resume)
                Conversation conversation = conversationService.resolveAnonymousConversation(
                        request.getSessionId());

                // Send session_id back as first SSE event
                emitter.send(SseEmitter.event()
                        .name("session")
                        .data("{\"sessionId\":\"" + conversation.getSessionId() + "\","
                                + "\"conversationId\":" + conversation.getId() + "}"));

                // 2. Check message limit
                long userMessageCount = conversationService.countUserMessages(conversation.getId());
                if (userMessageCount >= MAX_MESSAGES_PER_SESSION) {
                    log.info("Anonymous session {} reached message limit ({}/{})",
                            conversation.getSessionId(), userMessageCount, MAX_MESSAGES_PER_SESSION);
                    emitter.send(SseEmitter.event()
                            .name("limit_reached")
                            .data("{\"error\": \"Message limit reached\", \"suggestion\": \"register\"}"));
                    emitter.complete();
                    return;
                }

                // 3. Save user message (sender = ANONYMOUS)
                messageService.saveUserMessage(conversation, "ANONYMOUS", request.getMessage());

                // 4. Assemble prompt (system prompt + RAG + context)
                LlmRequest llmRequest = promptAssembler.assemble(conversation, request.getMessage());

                // 5. Stream LLM response + accumulate full response
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
                                // 6. Save bot response
                                Message botMessage = messageService.saveBotMessage(
                                        conversation, fullResponse.toString());

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
                log.error("Anonymous chat pipeline error: {}", e.getMessage(), e);
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
