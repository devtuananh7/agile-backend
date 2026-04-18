package vn.hust.agilechatbotbackend.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.hust.agilechatbotbackend.entity.Conversation;
import vn.hust.agilechatbotbackend.entity.enums.ConversationStatus;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    Optional<Conversation> findBySessionIdAndStatus(UUID sessionId, ConversationStatus status);

    Optional<Conversation> findBySessionId(UUID sessionId);

    Page<Conversation> findByUserIdOrderByUpdatedAtDesc(String userId, Pageable pageable);

    long countByRefId(Long refId);
}
