package vn.hust.agilechatbotbackend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for semantic knowledge search.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgeSearchRequest {

    @NotBlank(message = "Query is required")
    private String query;

    /** Optional category filter. */
    private String category;

    /** Minimum cosine similarity threshold (default 0.75). */
    @Builder.Default
    private Double threshold = 0.75;

    /** Maximum number of results (default 3). */
    @Builder.Default
    private Integer topK = 3;
}
