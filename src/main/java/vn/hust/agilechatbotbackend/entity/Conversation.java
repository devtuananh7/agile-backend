package vn.hust.agilechatbotbackend.entity;

import jakarta.persistence.*;
import lombok.*;
import vn.hust.agilechatbotbackend.entity.enums.ConversationStatus;
import vn.hust.agilechatbotbackend.entity.enums.ConversationType;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "conversations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Conversation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "session_id", nullable = false, unique = true)
    private UUID sessionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ref_id")
    private Conversation ref;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ConversationStatus status;

    @Column(name = "user_id", nullable = false, length = 100)
    private String userId;

    @Column(name = "username", nullable = false)
    private String username;

    @Column(name = "doctor", nullable = false, length = 100)
    private String doctor;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private ConversationType type;

    @Column(name = "prompt_name", nullable = false, length = 100)
    private String promptName;

    @Column(name = "conclusion", columnDefinition = "TEXT")
    private String conclusion;

    @Column(name = "summary", columnDefinition = "TEXT")
    private String summary;

    @Column(name = "summary_until_id")
    private Long summaryUntilId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (sessionId == null) {
            sessionId = UUID.randomUUID();
        }
        if (status == null) {
            status = ConversationStatus.ACTIVE;
        }
        if (type == null) {
            type = ConversationType.BOT;
        }
        if (doctor == null) {
            doctor = "BOT";
        }
        if (promptName == null) {
            promptName = "medical_general";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
