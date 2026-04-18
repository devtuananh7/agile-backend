package vn.hust.agilechatbotbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.hust.agilechatbotbackend.entity.enums.ContentType;
import vn.hust.agilechatbotbackend.entity.enums.SenderRole;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageResponse {

    private Long id;
    private Long conversationId;
    private String senderId;
    private SenderRole senderRole;
    private String content;
    private ContentType contentType;
    private Map<String, Object> metadata;
    private LocalDateTime createdAt;
}
