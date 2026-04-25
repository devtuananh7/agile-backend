package vn.hust.agilechatbotbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response DTO for bulk import operations.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BulkImportResponse {

    private int imported;
    private int failed;
    private List<ImportError> errors;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ImportError {
        private String title;
        private String reason;
    }
}
