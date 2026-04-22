package vn.hust.agilechatbotbackend.dto.metadata;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PatientMetadataDto implements UserMetadata {
    private String fullName;
    private String dob;
    private String gender; // MALE, FEMALE, OTHER
    private String bloodType;
    private String medicalHistory;
}
