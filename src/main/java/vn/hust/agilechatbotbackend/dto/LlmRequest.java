package vn.hust.agilechatbotbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO representing a fully assembled request to the LLM.
 * Built by PromptAssembler, consumed by LlmClient.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LlmRequest {

    /**
     * System prompt content (from SystemPromptResolver).
     */
    private String systemPrompt;

    /**
     * RAG context from knowledge base (may be null if no relevant docs found).
     */
    private String ragContext;

    /**
     * Conversation summary for long conversations (may be null if ≤20 messages).
     */
    private String summary;

    /**
     * Recent messages formatted as chat history.
     */
    private List<ChatMessage> recentMessages;

    /**
     * The new user message to respond to.
     */
    private String userMessage;

    /**
     * LLM model name (e.g., "gpt-4o", "gemini-2.0-flash").
     */
    private String model;

    /**
     * Temperature for response generation.
     */
    private double temperature;

    /**
     * Whether to stream the response via SSE.
     */
    @Builder.Default
    private boolean stream = true;

    /**
     * Represents a single message in the chat history.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ChatMessage {
        private String role;    // "user", "assistant", "system"
        private String content;
    }
}
