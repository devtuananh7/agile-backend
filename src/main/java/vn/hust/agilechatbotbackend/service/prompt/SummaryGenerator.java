package vn.hust.agilechatbotbackend.service.prompt;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import vn.hust.agilechatbotbackend.entity.Conversation;
import vn.hust.agilechatbotbackend.entity.Message;
import vn.hust.agilechatbotbackend.entity.enums.SenderRole;
import vn.hust.agilechatbotbackend.repository.ConversationRepository;
import vn.hust.agilechatbotbackend.service.MessageService;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Generates and regenerates structured medical summaries for conversations.
 * Uses a lightweight LLM model to minimize cost.
 * Runs asynchronously to avoid blocking chat responses.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SummaryGenerator {

    private static final int SUMMARY_THRESHOLD = 20;
    private static final int RECENT_MESSAGES_TO_KEEP = 10;

    private static final String SUMMARY_SYSTEM_PROMPT = """
            Bạn là hệ thống tóm tắt hồ sơ y tế. Nhiệm vụ: tạo bản tóm tắt CÓ CẤU TRÚC từ cuộc hội thoại y tế.

            BẮT BUỘC giữ lại TẤT CẢ thông tin sau (nếu có):
            - Triệu chứng: mô tả, vị trí, mức độ, thời gian xuất hiện
            - Thuốc đã dùng/được khuyên: tên thuốc, liều lượng, tần suất
            - Chỉ số y tế: nhiệt độ, huyết áp, nhịp tim, cân nặng...
            - Tiền sử bệnh: bệnh nền, dị ứng, phẫu thuật
            - Diễn biến: triệu chứng cải thiện/xấu đi
            - Khuyến nghị từ bot/bác sĩ

            Format output:
            **Triệu chứng:** [liệt kê]
            **Thuốc:** [liệt kê]
            **Chỉ số y tế:** [liệt kê]
            **Tiền sử:** [liệt kê]
            **Diễn biến:** [mô tả]
            **Khuyến nghị:** [liệt kê]

            Viết ngắn gọn, đầy đủ, bằng tiếng Việt. KHÔNG bỏ sót bất kỳ thông tin y tế nào.
            """;

    private static final String REGENERATE_INSTRUCTION = """
            Dưới đây là bản tóm tắt TRƯỚC ĐÓ và các tin nhắn MỚI.
            Hãy tạo bản tóm tắt MỚI kết hợp cả hai, giữ lại TẤT CẢ thông tin y tế.
            """;

    private final MessageService messageService;
    private final ConversationRepository conversationRepository;

    @Value("${caretalk.summary.api-key:${caretalk.embedding.api-key:}}")
    private String apiKey;

    @Value("${caretalk.summary.base-url:https://api.openai.com/v1}")
    private String baseUrl;

    @Value("${caretalk.summary.model:gpt-4o-mini}")
    private String summaryModel;

    /**
     * Check if a conversation needs summary generation or regeneration.
     * Called after each message to determine if async summary should run.
     */
    public boolean needsSummary(Conversation conversation) {
        long totalMessages = messageService.countByConversationId(conversation.getId());

        if (totalMessages <= SUMMARY_THRESHOLD) {
            return false;
        }

        // No summary yet → needs first generation
        if (conversation.getSummary() == null || conversation.getSummaryUntilId() == null) {
            return true;
        }

        // Has summary → check if enough new messages since last summary
        List<Message> messagesSinceSummary = messageService
                .getMessagesAfterSummary(conversation.getId(), conversation.getSummaryUntilId());

        return messagesSinceSummary.size() > SUMMARY_THRESHOLD;
    }

    /**
     * Generate or regenerate summary asynchronously.
     * Non-blocking — does not affect chat response time.
     */
    @Async
    public void generateSummaryAsync(Conversation conversation) {
        try {
            if (conversation.getSummary() == null || conversation.getSummaryUntilId() == null) {
                generateFirstSummary(conversation);
            } else {
                regenerateSummary(conversation);
            }
        } catch (Exception e) {
            log.error("Async summary generation failed for conversation {}: {}",
                    conversation.getId(), e.getMessage(), e);
        }
    }

    /**
     * Generate the first summary for a conversation.
     * Summarizes messages from M1 to M(total - RECENT_MESSAGES_TO_KEEP).
     */
    @Transactional
    public void generateFirstSummary(Conversation conversation) {
        List<Message> allMessages = messageService.getAllMessages(conversation.getId());

        if (allMessages.size() <= SUMMARY_THRESHOLD) {
            log.debug("Conversation {} has {} messages, below threshold. Skipping summary.",
                    conversation.getId(), allMessages.size());
            return;
        }

        // Summarize all messages except the most recent ones
        int summarizeUntilIndex = allMessages.size() - RECENT_MESSAGES_TO_KEEP;
        List<Message> toSummarize = allMessages.subList(0, summarizeUntilIndex);
        Message lastSummarizedMessage = toSummarize.get(toSummarize.size() - 1);

        String messagesText = formatMessagesForSummary(toSummarize);
        String summary = callLlmForSummary(SUMMARY_SYSTEM_PROMPT, messagesText);

        // Update conversation
        updateConversationSummary(conversation.getId(), summary, lastSummarizedMessage.getId());

        log.info("Generated first summary for conversation {} (summarized {} messages, until message id={})",
                conversation.getId(), toSummarize.size(), lastSummarizedMessage.getId());
    }

    /**
     * Regenerate summary by combining old summary with new messages.
     * Input: previous summary + messages since last summary → new summary.
     */
    @Transactional
    public void regenerateSummary(Conversation conversation) {
        List<Message> newMessages = messageService
                .getMessagesAfterSummary(conversation.getId(), conversation.getSummaryUntilId());

        if (newMessages.size() <= SUMMARY_THRESHOLD) {
            log.debug("Conversation {} has {} new messages since summary, below threshold. Skipping.",
                    conversation.getId(), newMessages.size());
            return;
        }

        // Keep the most recent messages out of the new summary
        int summarizeUntilIndex = newMessages.size() - RECENT_MESSAGES_TO_KEEP;
        List<Message> toSummarize = newMessages.subList(0, summarizeUntilIndex);
        Message lastSummarizedMessage = toSummarize.get(toSummarize.size() - 1);

        // Build input: old summary + new messages
        String inputText = REGENERATE_INSTRUCTION + "\n\n"
                + "=== BẢN TÓM TẮT TRƯỚC ĐÓ ===\n"
                + conversation.getSummary() + "\n\n"
                + "=== TIN NHẮN MỚI ===\n"
                + formatMessagesForSummary(toSummarize);

        String newSummary = callLlmForSummary(SUMMARY_SYSTEM_PROMPT, inputText);

        // Update conversation
        updateConversationSummary(conversation.getId(), newSummary, lastSummarizedMessage.getId());

        log.info("Regenerated summary for conversation {} (added {} messages, until message id={})",
                conversation.getId(), toSummarize.size(), lastSummarizedMessage.getId());
    }

    /**
     * Format messages into readable text for the summary LLM.
     */
    private String formatMessagesForSummary(List<Message> messages) {
        return messages.stream()
                .map(m -> {
                    String role = switch (m.getSenderRole()) {
                        case USER -> "Bệnh nhân";
                        case BOT -> "Bot";
                        case DOCTOR -> "Bác sĩ";
                        case SYSTEM -> "Hệ thống";
                    };
                    return role + ": " + m.getContent();
                })
                .collect(Collectors.joining("\n"));
    }

    /**
     * Call lightweight LLM to generate summary.
     */
    private String callLlmForSummary(String systemPrompt, String userContent) {
        WebClient client = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .build();

        Map<String, Object> requestBody = Map.of(
                "model", summaryModel,
                "temperature", 0.1,
                "messages", List.of(
                        Map.of("role", "system", "content", systemPrompt),
                        Map.of("role", "user", "content", userContent)
                )
        );

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> response = client.post()
                    .uri("/chat/completions")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (response == null || !response.containsKey("choices")) {
                throw new RuntimeException("Invalid LLM response for summary");
            }

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
            @SuppressWarnings("unchecked")
            Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");

            return (String) message.get("content");

        } catch (Exception e) {
            log.error("Summary LLM call failed: {}", e.getMessage(), e);
            throw new RuntimeException("Summary generation LLM call failed", e);
        }
    }

    /**
     * Update conversation summary and summary_until_id in database.
     */
    private void updateConversationSummary(Long conversationId, String summary, Long summaryUntilId) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new IllegalArgumentException("Conversation not found: " + conversationId));

        conversation.setSummary(summary);
        conversation.setSummaryUntilId(summaryUntilId);
        conversationRepository.save(conversation);
    }
}
