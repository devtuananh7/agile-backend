package vn.hust.agilechatbotbackend.dto.metadata;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminMetadataDto implements UserMetadata {
    private String fullName;
    private String department;
}
