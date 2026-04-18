package vn.hust.agilechatbotbackend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.hust.agilechatbotbackend.dto.ConversationResponse;
import vn.hust.agilechatbotbackend.entity.Conversation;
import vn.hust.agilechatbotbackend.entity.enums.ConversationStatus;
import vn.hust.agilechatbotbackend.entity.enums.ConversationType;
import vn.hust.agilechatbotbackend.repository.ConversationRepository;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConversationService {

    private final ConversationRepository conversationRepository;

    /**
     * Create a new BOT conversation.
     */
    @Transactional
    public Conversation createConversation(String userId, String username, UUID sessionId) {
        Conversation conversation = Conversation.builder()
                .sessionId(sessionId != null ? sessionId : UUID.randomUUID())
                .status(ConversationStatus.ACTIVE)
                .userId(userId)
                .username(username)
                .doctor("BOT")
                .type(ConversationType.BOT)
                .promptName("medical_general")
                .build();

        Conversation saved = conversationRepository.save(conversation);
        log.info("Created new conversation id={} sessionId={} for user={}",
                saved.getId(), saved.getSessionId(), userId);
        return saved;
    }

    /**
     * Find an active conversation by session ID.
     */
    public Optional<Conversation> findActiveBySessionId(UUID sessionId) {
        return conversationRepository.findBySessionIdAndStatus(sessionId, ConversationStatus.ACTIVE);
    }

    /**
     * Find any conversation by session ID (regardless of status).
     */
    public Optional<Conversation> findBySessionId(UUID sessionId) {
        return conversationRepository.findBySessionId(sessionId);
    }

    /**
     * Find conversation by ID.
     */
    public Optional<Conversation> findById(Long id) {
        return conversationRepository.findById(id);
    }

    /**
     * Session resolution logic:
     * - If sessionId is provided and an ACTIVE conversation exists → return it
     * - If sessionId is provided but no ACTIVE conversation → create new with new sessionId
     * - If sessionId is null → create new conversation with generated sessionId
     */
    @Transactional
    public Conversation resolveConversation(String userId, String username, UUID sessionId) {
        if (sessionId != null) {
            // Try to find an existing ACTIVE conversation
            Optional<Conversation> activeConversation = findActiveBySessionId(sessionId);
            if (activeConversation.isPresent()) {
                log.debug("Resumed conversation id={} for sessionId={}",
                        activeConversation.get().getId(), sessionId);
                return activeConversation.get();
            }

            // Session exists but conversation is DONE → create new with new sessionId
            Optional<Conversation> existingConversation = findBySessionId(sessionId);
            if (existingConversation.isPresent()) {
                log.info("Session {} exists but conversation is {}. Creating new conversation.",
                        sessionId, existingConversation.get().getStatus());
                return createConversation(userId, username, null);
            }

            // Session ID provided but doesn't exist → create with that sessionId
            log.info("No conversation found for sessionId={}. Creating new.", sessionId);
            return createConversation(userId, username, sessionId);
        }

        // No sessionId → create brand new conversation
        return createConversation(userId, username, null);
    }

    /**
     * Update conversation status.
     */
    @Transactional
    public Conversation updateStatus(Long conversationId, ConversationStatus newStatus) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Conversation not found: " + conversationId));

        ConversationStatus oldStatus = conversation.getStatus();
        conversation.setStatus(newStatus);
        Conversation saved = conversationRepository.save(conversation);

        log.info("Conversation id={} status changed: {} → {}", conversationId, oldStatus, newStatus);
        return saved;
    }

    /**
     * Close a conversation (set status to DONE).
     */
    @Transactional
    public Conversation closeConversation(Long conversationId) {
        return updateStatus(conversationId, ConversationStatus.DONE);
    }

    /**
     * Doctor concludes a conversation with a conclusion text.
     */
    @Transactional
    public Conversation concludeConversation(Long conversationId, String conclusion) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Conversation not found: " + conversationId));

        conversation.setConclusion(conclusion);
        conversation.setStatus(ConversationStatus.DONE);
        Conversation saved = conversationRepository.save(conversation);

        log.info("Conversation id={} concluded by doctor={}", conversationId, conversation.getDoctor());
        return saved;
    }

    /**
     * Escalate a BOT conversation to DOCTOR.
     * 1. Closes the BOT conversation (status=DONE)
     * 2. Creates a new ESCALATED conversation with ref_id and new session_id
     */
    @Transactional
    public Conversation escalateToDoctor(Long botConversationId, String doctorId) {
        Conversation botConversation = conversationRepository.findById(botConversationId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Conversation not found: " + botConversationId));

        if (botConversation.getType() != ConversationType.BOT) {
            throw new IllegalStateException(
                    "Can only escalate BOT conversations. Current type: " + botConversation.getType());
        }

        if (botConversation.getStatus() != ConversationStatus.ACTIVE) {
            throw new IllegalStateException(
                    "Can only escalate ACTIVE conversations. Current status: " + botConversation.getStatus());
        }

        // 1. Close the BOT conversation
        botConversation.setStatus(ConversationStatus.DONE);
        conversationRepository.save(botConversation);

        // 2. Create new ESCALATED conversation with new session_id
        Conversation escalated = Conversation.builder()
                .sessionId(UUID.randomUUID())
                .ref(botConversation)
                .status(ConversationStatus.ACTIVE)
                .userId(botConversation.getUserId())
                .username(botConversation.getUsername())
                .doctor(doctorId)
                .type(ConversationType.ESCALATED)
                .promptName(botConversation.getPromptName())
                .build();

        Conversation saved = conversationRepository.save(escalated);
        log.info("Escalated conversation id={} → new id={} (doctor={}, sessionId={})",
                botConversationId, saved.getId(), doctorId, saved.getSessionId());

        return saved;
    }

    /**
     * Get the referenced BOT conversation for an ESCALATED conversation.
     * Used to provide bot chat history context to the doctor.
     */
    public Optional<Conversation> getEscalationSource(Long escalatedConversationId) {
        return conversationRepository.findById(escalatedConversationId)
                .map(Conversation::getRef);
    }

    /**
     * List conversations for a user with pagination.
     */
    public Page<ConversationResponse> listByUserId(String userId, Pageable pageable) {
        return conversationRepository.findByUserIdOrderByUpdatedAtDesc(userId, pageable)
                .map(this::toResponse);
    }

    /**
     * Convert entity to response DTO.
     */
    public ConversationResponse toResponse(Conversation conversation) {
        return ConversationResponse.builder()
                .id(conversation.getId())
                .sessionId(conversation.getSessionId())
                .refId(conversation.getRef() != null ? conversation.getRef().getId() : null)
                .status(conversation.getStatus())
                .userId(conversation.getUserId())
                .username(conversation.getUsername())
                .doctor(conversation.getDoctor())
                .type(conversation.getType())
                .promptName(conversation.getPromptName())
                .conclusion(conversation.getConclusion())
                .summary(conversation.getSummary())
                .createdAt(conversation.getCreatedAt())
                .updatedAt(conversation.getUpdatedAt())
                .build();
    }
}
