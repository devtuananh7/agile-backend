package vn.hust.agilechatbotbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatRequest {

    /**
     * Session ID for conversation continuity.
     * If provided, the system will try to find an existing ACTIVE conversation.
     * If null, a new conversation will be created with a generated UUID.
     */
    private UUID sessionId;

    /**
     * The user's message content.
     */
    private String message;

    /**
     * User ID of the sender.
     */
    private String userId;

    /**
     * Username of the sender.
     */
    private String username;
}
