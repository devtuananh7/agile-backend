package vn.hust.agilechatbotbackend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.hust.agilechatbotbackend.dto.MessageResponse;
import vn.hust.agilechatbotbackend.entity.Conversation;
import vn.hust.agilechatbotbackend.entity.Message;
import vn.hust.agilechatbotbackend.entity.enums.ContentType;
import vn.hust.agilechatbotbackend.entity.enums.SenderRole;
import vn.hust.agilechatbotbackend.repository.MessageRepository;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class MessageService {

    private static final int DEFAULT_RECENT_COUNT = 20;

    private final MessageRepository messageRepository;

    /**
     * Save a user message.
     */
    @Transactional
    public Message saveUserMessage(Conversation conversation, String userId, String content) {
        Message message = Message.builder()
                .conversation(conversation)
                .senderId(userId)
                .senderRole(SenderRole.USER)
                .content(content)
                .contentType(ContentType.TEXT)
                .build();

        Message saved = messageRepository.save(message);
        log.debug("Saved USER message id={} for conversation={}", saved.getId(), conversation.getId());
        return saved;
    }

    /**
     * Save a bot response message (after full streaming is concatenated).
     */
    @Transactional
    public Message saveBotMessage(Conversation conversation, String content) {
        Message message = Message.builder()
                .conversation(conversation)
                .senderId("BOT")
                .senderRole(SenderRole.BOT)
                .content(content)
                .contentType(ContentType.TEXT)
                .build();

        Message saved = messageRepository.save(message);
        log.debug("Saved BOT message id={} for conversation={}", saved.getId(), conversation.getId());
        return saved;
    }

    /**
     * Save a doctor message.
     */
    @Transactional
    public Message saveDoctorMessage(Conversation conversation, String doctorId, String content) {
        Message message = Message.builder()
                .conversation(conversation)
                .senderId(doctorId)
                .senderRole(SenderRole.DOCTOR)
                .content(content)
                .contentType(ContentType.TEXT)
                .build();

        Message saved = messageRepository.save(message);
        log.debug("Saved DOCTOR message id={} for conversation={}", saved.getId(), conversation.getId());
        return saved;
    }

    /**
     * Save a system event message (e.g., escalation notification).
     */
    @Transactional
    public Message saveSystemMessage(Conversation conversation, String content) {
        Message message = Message.builder()
                .conversation(conversation)
                .senderId("SYSTEM")
                .senderRole(SenderRole.SYSTEM)
                .content(content)
                .contentType(ContentType.TEXT)
                .build();

        Message saved = messageRepository.save(message);
        log.debug("Saved SYSTEM message id={} for conversation={}", saved.getId(), conversation.getId());
        return saved;
    }

    /**
     * Get the most recent 20 messages for a conversation (default, no pagination params).
     * Ordered by created_at ASC for display order.
     */
    public List<Message> getRecentMessages(Long conversationId) {
        return messageRepository.findTop20ByConversationIdOrderByCreatedAtAsc(conversationId);
    }

    /**
     * Get messages with full pagination support.
     * Ordered by created_at ASC.
     */
    public Page<MessageResponse> getMessages(Long conversationId, Pageable pageable) {
        return messageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId, pageable)
                .map(this::toResponse);
    }

    /**
     * Get messages with default pagination (page 0, size 20).
     */
    public Page<MessageResponse> getMessages(Long conversationId) {
        return getMessages(conversationId, PageRequest.of(0, DEFAULT_RECENT_COUNT));
    }

    /**
     * Count total messages in a conversation.
     * Used by auto-summary to check if threshold is exceeded.
     */
    public long countByConversationId(Long conversationId) {
        return messageRepository.countByConversationId(conversationId);
    }

    /**
     * Get all messages after a specific message ID (for context building with summary).
     * Returns messages created after the summary_until_id.
     */
    public List<Message> getMessagesAfterSummary(Long conversationId, Long summaryUntilId) {
        if (summaryUntilId == null) {
            return messageRepository.findAllByConversationIdOrderByCreatedAtAsc(conversationId);
        }
        return messageRepository.findMessagesAfterSummary(conversationId, summaryUntilId);
    }

    /**
     * Get all messages in a conversation (for full context when ≤20 messages).
     */
    public List<Message> getAllMessages(Long conversationId) {
        return messageRepository.findAllByConversationIdOrderByCreatedAtAsc(conversationId);
    }

    /**
     * Convert entity to response DTO.
     */
    public MessageResponse toResponse(Message message) {
        return MessageResponse.builder()
                .id(message.getId())
                .conversationId(message.getConversation().getId())
                .senderId(message.getSenderId())
                .senderRole(message.getSenderRole())
                .content(message.getContent())
                .contentType(message.getContentType())
                .metadata(message.getMetadata())
                .createdAt(message.getCreatedAt())
                .build();
    }
}
