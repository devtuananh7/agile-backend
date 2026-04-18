package vn.hust.agilechatbotbackend.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import vn.hust.agilechatbotbackend.entity.Conversation;
import vn.hust.agilechatbotbackend.entity.Message;
import vn.hust.agilechatbotbackend.service.ConversationService;
import vn.hust.agilechatbotbackend.service.MessageService;

import java.util.Map;
import java.util.UUID;

/**
 * WebSocket/STOMP controller for doctor-patient consultation.
 * Messages are routed to private session-based topics.
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class ConsultationWsController {

    private final ConversationService conversationService;
    private final MessageService messageService;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Handle messages sent to a specific consultation session.
     * Client sends to: /app/chat.sendMessage/{sessionId}
     * Response broadcast to: /topic/cases/{sessionId}
     */
    @MessageMapping("/chat.sendMessage/{sessionId}")
    public void sendMessage(
            @DestinationVariable String sessionId,
            @Payload Map<String, String> payload) {

        String senderId = payload.get("senderId");
        String senderRole = payload.get("senderRole");
        String content = payload.get("content");

        log.debug("WS message received: session={}, sender={}, role={}",
                sessionId, senderId, senderRole);

        try {
            // Find the conversation by session ID
            UUID uuid = UUID.fromString(sessionId);
            Conversation conversation = conversationService.findActiveBySessionId(uuid)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "No active conversation for session: " + sessionId));

            // Save message based on role
            Message saved;
            if ("DOCTOR".equalsIgnoreCase(senderRole)) {
                saved = messageService.saveDoctorMessage(conversation, senderId, content);
            } else {
                saved = messageService.saveUserMessage(conversation, senderId, content);
            }

            // Broadcast to the private session topic
            Map<String, Object> response = Map.of(
                    "messageId", saved.getId(),
                    "senderId", senderId,
                    "senderRole", saved.getSenderRole().name(),
                    "content", content,
                    "createdAt", saved.getCreatedAt().toString()
            );

            messagingTemplate.convertAndSend(
                    "/topic/cases/" + sessionId, (Object) response);

        } catch (Exception e) {
            log.error("Failed to process WS message for session {}: {}", sessionId, e.getMessage());
            messagingTemplate.convertAndSend(
                    "/topic/cases/" + sessionId,
                    (Object) Map.of("error", e.getMessage()));
        }
    }

    /**
     * Handle doctor submitting a conclusion for a consultation.
     * Client sends to: /app/chat.conclude/{sessionId}
     */
    @MessageMapping("/chat.conclude/{sessionId}")
    public void concludeConsultation(
            @DestinationVariable String sessionId,
            @Payload Map<String, String> payload) {

        String conclusion = payload.get("conclusion");

        try {
            UUID uuid = UUID.fromString(sessionId);
            Conversation conversation = conversationService.findActiveBySessionId(uuid)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "No active conversation for session: " + sessionId));

            conversationService.concludeConversation(conversation.getId(), conclusion);

            // Save system message about conclusion
            messageService.saveSystemMessage(conversation,
                    "Bác sĩ đã kết luận cuộc tư vấn.");

            // Notify all participants
            messagingTemplate.convertAndSend(
                    "/topic/cases/" + sessionId,
                    (Object) Map.of(
                            "type", "CONCLUDED",
                            "conclusion", conclusion
                    ));

            log.info("Consultation concluded for session {}", sessionId);

        } catch (Exception e) {
            log.error("Failed to conclude session {}: {}", sessionId, e.getMessage());
            messagingTemplate.convertAndSend(
                    "/topic/cases/" + sessionId,
                    (Object) Map.of("error", e.getMessage()));
        }
    }
}
