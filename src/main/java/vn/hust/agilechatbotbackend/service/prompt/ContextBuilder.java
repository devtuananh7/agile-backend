package vn.hust.agilechatbotbackend.service.prompt;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import vn.hust.agilechatbotbackend.dto.LlmRequest;
import vn.hust.agilechatbotbackend.entity.Conversation;
import vn.hust.agilechatbotbackend.entity.Message;
import vn.hust.agilechatbotbackend.entity.enums.SenderRole;
import vn.hust.agilechatbotbackend.service.MessageService;

import java.util.ArrayList;
import java.util.List;

/**
 * Builds conversation context for the LLM prompt.
 * Strategy:
 * - If ≤20 total messages: include ALL messages as context
 * - If >20 total messages: include SUMMARY + messages after summary point
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ContextBuilder {

    private static final int FULL_CONTEXT_THRESHOLD = 20;

    private final MessageService messageService;

    /**
     * Build conversation context as a list of ChatMessages.
     * Automatically chooses between full history or summary+recent strategy.
     */
    public ContextResult build(Conversation conversation) {
        long totalMessages = messageService.countByConversationId(conversation.getId());

        if (totalMessages <= FULL_CONTEXT_THRESHOLD) {
            return buildFullContext(conversation);
        } else {
            return buildSummaryContext(conversation);
        }
    }

    /**
     * Full context: include all messages (for short conversations).
     */
    private ContextResult buildFullContext(Conversation conversation) {
        List<Message> allMessages = messageService.getAllMessages(conversation.getId());
        List<LlmRequest.ChatMessage> chatMessages = convertMessages(allMessages);

        log.debug("Built FULL context for conversation {} ({} messages)",
                conversation.getId(), chatMessages.size());

        return new ContextResult(null, chatMessages);
    }

    /**
     * Summary + recent context: include summary as system context + messages after summary.
     */
    private ContextResult buildSummaryContext(Conversation conversation) {
        String summary = conversation.getSummary();
        Long summaryUntilId = conversation.getSummaryUntilId();

        List<Message> recentMessages;
        if (summaryUntilId != null) {
            recentMessages = messageService.getMessagesAfterSummary(
                    conversation.getId(), summaryUntilId);
        } else {
            // No summary generated yet, get recent 20
            recentMessages = messageService.getRecentMessages(conversation.getId());
        }

        List<LlmRequest.ChatMessage> chatMessages = convertMessages(recentMessages);

        log.debug("Built SUMMARY context for conversation {} (summary={}, {} recent messages)",
                conversation.getId(), summary != null ? "yes" : "no", chatMessages.size());

        return new ContextResult(summary, chatMessages);
    }

    /**
     * Convert Message entities to LlmRequest.ChatMessage DTOs.
     */
    private List<LlmRequest.ChatMessage> convertMessages(List<Message> messages) {
        List<LlmRequest.ChatMessage> chatMessages = new ArrayList<>();

        for (Message message : messages) {
            String role = mapSenderRoleToLlmRole(message.getSenderRole());
            chatMessages.add(LlmRequest.ChatMessage.builder()
                    .role(role)
                    .content(message.getContent())
                    .build());
        }

        return chatMessages;
    }

    /**
     * Map internal SenderRole to LLM API role names.
     */
    private String mapSenderRoleToLlmRole(SenderRole senderRole) {
        return switch (senderRole) {
            case USER -> "user";
            case BOT -> "assistant";
            case DOCTOR -> "assistant";
            case SYSTEM -> "system";
        };
    }

    /**
     * Result of context building, containing optional summary and message history.
     */
    public record ContextResult(String summary, List<LlmRequest.ChatMessage> messages) {
    }
}
