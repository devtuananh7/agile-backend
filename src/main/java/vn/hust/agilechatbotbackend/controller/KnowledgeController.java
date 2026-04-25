package vn.hust.agilechatbotbackend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.hust.agilechatbotbackend.dto.*;
import vn.hust.agilechatbotbackend.entity.KnowledgeDocument;
import vn.hust.agilechatbotbackend.service.knowledge.KnowledgeService;

import java.util.List;
import java.util.Map;

/**
 * REST controller for admin knowledge document management.
 * All endpoints are protected by ADMIN role via SecurityConfig pattern:
 * .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
 */
@RestController
@RequestMapping("/api/v1/admin/knowledge")
@RequiredArgsConstructor
@Slf4j
public class KnowledgeController {

    private final KnowledgeService knowledgeService;

    /**
     * POST / — Create a single knowledge document.
     */
    @PostMapping
    public ResponseEntity<?> createDocument(@Valid @RequestBody KnowledgeDocumentRequest request) {
        try {
            List<KnowledgeDocument> saved = knowledgeService.saveDocument(
                    request.getTitle(),
                    request.getContent(),
                    request.getCategory(),
                    request.getTags()
            );
            // Return first document's response (or list if chunked)
            if (saved.size() == 1) {
                return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(saved.get(0)));
            }
            // Chunked document — return list
            List<KnowledgeDocumentResponse> responses = saved.stream().map(this::toResponse).toList();
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "chunked", true,
                    "totalParts", responses.size(),
                    "documents", responses
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * POST /bulk — Bulk import knowledge documents.
     */
    @PostMapping("/bulk")
    public ResponseEntity<?> bulkImport(@Valid @RequestBody List<KnowledgeDocumentRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Request body must be a non-empty array of documents"));
        }

        BulkImportResponse response = knowledgeService.saveDocumentsBulk(requests);
        return ResponseEntity.ok(response);
    }

    /**
     * GET / — List knowledge documents with pagination and optional category filter.
     */
    @GetMapping
    public ResponseEntity<Page<KnowledgeDocumentResponse>> listDocuments(
            @RequestParam(required = false) String category,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<KnowledgeDocument> page;
        if (category != null && !category.isBlank()) {
            page = knowledgeService.listDocumentsByCategory(category, pageable);
        } else {
            page = knowledgeService.listDocuments(pageable);
        }

        Page<KnowledgeDocumentResponse> responsePage = page.map(this::toResponse);
        return ResponseEntity.ok(responsePage);
    }

    /**
     * GET /{id} — Get a single knowledge document by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getDocument(@PathVariable Long id) {
        try {
            KnowledgeDocument document = knowledgeService.findById(id);
            return ResponseEntity.ok(toResponse(document));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * PUT /{id} — Update a knowledge document.
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateDocument(@PathVariable Long id,
                                             @Valid @RequestBody KnowledgeDocumentRequest request) {
        try {
            KnowledgeDocument updated = knowledgeService.updateDocument(
                    id,
                    request.getTitle(),
                    request.getContent(),
                    request.getCategory(),
                    request.getTags()
            );
            return ResponseEntity.ok(toResponse(updated));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * DELETE /{id} — Soft delete a knowledge document.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDocument(@PathVariable Long id) {
        try {
            knowledgeService.softDelete(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * POST /search — Semantic search with optional category filter.
     */
    @PostMapping("/search")
    public ResponseEntity<List<KnowledgeDocumentResponse>> searchDocuments(
            @Valid @RequestBody KnowledgeSearchRequest request) {

        double threshold = request.getThreshold() != null ? request.getThreshold() : 0.75;
        int topK = request.getTopK() != null ? request.getTopK() : 3;

        List<KnowledgeDocument> results;
        if (request.getCategory() != null && !request.getCategory().isBlank()) {
            results = knowledgeService.searchByTextAndCategory(
                    request.getQuery(), request.getCategory(), threshold, topK);
        } else {
            results = knowledgeService.searchByText(request.getQuery(), threshold, topK);
        }

        List<KnowledgeDocumentResponse> responses = results.stream().map(this::toResponse).toList();
        return ResponseEntity.ok(responses);
    }

    /**
     * Convert entity to response DTO (excludes embedding vector).
     */
    private KnowledgeDocumentResponse toResponse(KnowledgeDocument doc) {
        return KnowledgeDocumentResponse.builder()
                .id(doc.getId())
                .title(doc.getTitle())
                .content(doc.getContent())
                .category(doc.getCategory())
                .tags(doc.getTags())
                .isActive(doc.getIsActive())
                .createdAt(doc.getCreatedAt())
                .updatedAt(doc.getUpdatedAt())
                .build();
    }
}
