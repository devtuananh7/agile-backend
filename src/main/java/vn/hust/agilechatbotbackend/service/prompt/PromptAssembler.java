package vn.hust.agilechatbotbackend.service.prompt;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import vn.hust.agilechatbotbackend.dto.LlmRequest;
import vn.hust.agilechatbotbackend.entity.Conversation;
import vn.hust.agilechatbotbackend.entity.SystemPrompt;

import java.util.ArrayList;
import java.util.List;

/**
 * Orchestrates the prompt assembly pipeline.
 * Combines 4 components into a single LlmRequest:
 * 1. System Prompt (from SystemPromptResolver)
 * 2. RAG Context (from RagRetriever)
 * 3. Conversation Context - summary + history (from ContextBuilder)
 * 4. New User Message
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PromptAssembler {

    private final SystemPromptResolver systemPromptResolver;
    private final RagRetriever ragRetriever;
    private final ContextBuilder contextBuilder;

    /**
     * Assemble a complete LLM request from all context sources.
     *
     * @param conversation  the active conversation
     * @param userMessage   the new user message to respond to
     * @return fully assembled LlmRequest ready for LlmClient
     */
    public LlmRequest assemble(Conversation conversation, String userMessage) {
        // 1. Resolve system prompt
        SystemPrompt systemPrompt = systemPromptResolver.resolveByName(conversation.getPromptName());
        String systemPromptContent = systemPrompt.getContent();
        String model = systemPrompt.getModelName();
        double temperature = systemPrompt.getTemperature();

        // 2. Retrieve RAG context (may be null)
        String ragContext = ragRetriever.retrieve(userMessage);

        // 3. Build conversation context
        ContextBuilder.ContextResult contextResult = contextBuilder.build(conversation);

        // 4. Compose the final system message (system prompt + RAG + summary)
        String composedSystemPrompt = composeSystemMessage(
                systemPromptContent, ragContext, contextResult.summary());

        // 5. Build the LlmRequest
        LlmRequest request = LlmRequest.builder()
                .systemPrompt(composedSystemPrompt)
                .ragContext(ragContext)
                .summary(contextResult.summary())
                .recentMessages(contextResult.messages())
                .userMessage(userMessage)
                .model(model)
                .temperature(temperature)
                .stream(true)
                .build();

        log.info("Assembled LLM request: model={}, temp={}, historySize={}, ragContext={}, summary={}",
                model, temperature, contextResult.messages().size(),
                ragContext != null ? "yes" : "no",
                contextResult.summary() != null ? "yes" : "no");

        return request;
    }

    /**
     * Compose the full system message by combining:
     * - Base system prompt
     * - RAG context (if available)
     * - Conversation summary (if available)
     */
    private String composeSystemMessage(String systemPrompt, String ragContext, String summary) {
        StringBuilder sb = new StringBuilder();

        // Base system prompt
        sb.append(systemPrompt);

        // Append RAG context
        if (ragContext != null && !ragContext.isBlank()) {
            sb.append("\n\n").append(ragContext);
        }

        // Append conversation summary
        if (summary != null && !summary.isBlank()) {
            sb.append("\n\n[TÓM TẮT CUỘC HỘI THOẠI TRƯỚC ĐÓ]\n");
            sb.append(summary);
        }

        return sb.toString();
    }

    /**
     * Convert assembled LlmRequest to OpenAI-compatible message list.
     * Useful for LlmClient implementations.
     */
    public List<LlmRequest.ChatMessage> toOpenAiMessages(LlmRequest request) {
        List<LlmRequest.ChatMessage> messages = new ArrayList<>();

        // System message (includes RAG + summary)
        messages.add(LlmRequest.ChatMessage.builder()
                .role("system")
                .content(request.getSystemPrompt())
                .build());

        // Conversation history
        if (request.getRecentMessages() != null) {
            messages.addAll(request.getRecentMessages());
        }

        // New user message
        messages.add(LlmRequest.ChatMessage.builder()
                .role("user")
                .content(request.getUserMessage())
                .build());

        return messages;
    }
}
