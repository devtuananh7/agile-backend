package vn.hust.agilechatbotbackend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import vn.hust.agilechatbotbackend.entity.Conversation;
import vn.hust.agilechatbotbackend.entity.enums.ConversationType;
import vn.hust.agilechatbotbackend.repository.ConversationRepository;
import vn.hust.agilechatbotbackend.repository.MessageRepository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Scheduled job that hard-deletes anonymous conversations older than 24 hours.
 * Runs every 1 hour.
 * Deletes messages first (FK constraint), then conversations.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AnonymousCleanupJob {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;

    @Scheduled(fixedRate = 3600000) // every 1 hour
    @Transactional
    public void cleanupExpiredAnonymousConversations() {
        LocalDateTime cutoff = LocalDateTime.now().minusHours(24);
        log.info("Starting anonymous conversation cleanup. Cutoff: {}", cutoff);

        // Find all anonymous conversations older than 24h
        List<Conversation> expiredConversations =
                conversationRepository.findByTypeAndCreatedAtBefore(ConversationType.ANONYMOUS, cutoff);

        if (expiredConversations.isEmpty()) {
            log.info("No expired anonymous conversations found.");
            return;
        }

        int count = expiredConversations.size();

        // Delete messages first (FK constraint), then conversations
        for (Conversation conversation : expiredConversations) {
            messageRepository.deleteByConversationId(conversation.getId());
        }
        conversationRepository.deleteByTypeAndCreatedAtBefore(ConversationType.ANONYMOUS, cutoff);

        log.info("Cleaned up {} anonymous conversations older than 24h.", count);
    }
}
