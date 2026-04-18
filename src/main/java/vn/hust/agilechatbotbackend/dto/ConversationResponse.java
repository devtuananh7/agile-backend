package vn.hust.agilechatbotbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.hust.agilechatbotbackend.entity.enums.ConversationStatus;
import vn.hust.agilechatbotbackend.entity.enums.ConversationType;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConversationResponse {

    private Long id;
    private UUID sessionId;
    private Long refId;
    private ConversationStatus status;
    private String userId;
    private String username;
    private String doctor;
    private ConversationType type;
    private String promptName;
    private String conclusion;
    private String summary;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
