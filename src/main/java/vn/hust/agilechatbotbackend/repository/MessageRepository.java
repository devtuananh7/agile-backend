package vn.hust.agilechatbotbackend.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.hust.agilechatbotbackend.entity.Message;
import vn.hust.agilechatbotbackend.entity.enums.SenderRole;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    Page<Message> findByConversationIdOrderByCreatedAtAsc(Long conversationId, Pageable pageable);

    List<Message> findTop20ByConversationIdOrderByCreatedAtAsc(Long conversationId);

    long countByConversationId(Long conversationId);

    long countByConversationIdAndSenderRole(Long conversationId, SenderRole senderRole);

    @Query("SELECT m FROM Message m WHERE m.conversation.id = :conversationId AND m.id > :afterId ORDER BY m.createdAt ASC")
    List<Message> findMessagesAfterSummary(@Param("conversationId") Long conversationId,
                                           @Param("afterId") Long afterId);

    @Query("SELECT m FROM Message m WHERE m.conversation.id = :conversationId ORDER BY m.createdAt ASC")
    List<Message> findAllByConversationIdOrderByCreatedAtAsc(@Param("conversationId") Long conversationId);

    void deleteByConversationId(Long conversationId);
}
