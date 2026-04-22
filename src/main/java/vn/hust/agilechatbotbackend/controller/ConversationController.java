package vn.hust.agilechatbotbackend.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import vn.hust.agilechatbotbackend.dto.ConversationResponse;
import vn.hust.agilechatbotbackend.dto.EscalationRequest;
import vn.hust.agilechatbotbackend.dto.MessageResponse;
import vn.hust.agilechatbotbackend.entity.Conversation;
import vn.hust.agilechatbotbackend.security.CustomUserDetails;
import vn.hust.agilechatbotbackend.service.ConversationService;
import vn.hust.agilechatbotbackend.service.MessageService;

/**
 * REST API for conversation management.
 * Provides listing, detail, messages retrieval, and escalation endpoints.
 */
@RestController
@RequestMapping("/api/v1/conversations")
@RequiredArgsConstructor
@Slf4j
public class ConversationController {

    private final ConversationService conversationService;
    private final MessageService messageService;

    /**
     * List conversations for the authenticated user with pagination.
     * GET /api/v1/conversations?page=0&size=10
     */
    @GetMapping
    public ResponseEntity<Page<ConversationResponse>> listConversations(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        String userId = userDetails.getFirebaseUid();
        Pageable pageable = PageRequest.of(page, size);
        Page<ConversationResponse> conversations = conversationService.listByUserId(userId, pageable);
        return ResponseEntity.ok(conversations);
    }

    /**
     * Get conversation detail by ID.
     * GET /api/v1/conversations/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ConversationResponse> getConversation(@PathVariable Long id) {
        return conversationService.findById(id)
                .map(conversationService::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get messages for a conversation with pagination.
     * GET /api/v1/conversations/{id}/messages?page=0&size=20
     */
    @GetMapping("/{id}/messages")
    public ResponseEntity<Page<MessageResponse>> getMessages(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        // Verify conversation exists
        if (conversationService.findById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<MessageResponse> messages = messageService.getMessages(id, pageable);
        return ResponseEntity.ok(messages);
    }

    /**
     * Escalate a BOT conversation to a DOCTOR consultation.
     * PUT /api/v1/conversations/{id}/escalate
     */
    @PutMapping("/{id}/escalate")
    public ResponseEntity<ConversationResponse> escalate(
            @PathVariable Long id,
            @RequestBody EscalationRequest request) {

        try {
            Conversation escalated = conversationService.escalateToDoctor(id, request.getDoctorId());

            // Save system message about escalation
            conversationService.findById(id).ifPresent(oldConv ->
                    messageService.saveSystemMessage(oldConv,
                            "Cuộc hội thoại đã được chuyển sang bác sĩ tư vấn."));

            return ResponseEntity.ok(conversationService.toResponse(escalated));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Close a conversation.
     * PUT /api/v1/conversations/{id}/close
     */
    @PutMapping("/{id}/close")
    public ResponseEntity<ConversationResponse> closeConversation(@PathVariable Long id) {
        try {
            Conversation closed = conversationService.closeConversation(id);
            return ResponseEntity.ok(conversationService.toResponse(closed));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
