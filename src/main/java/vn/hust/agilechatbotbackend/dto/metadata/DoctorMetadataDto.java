package vn.hust.agilechatbotbackend.dto.metadata;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DoctorMetadataDto implements UserMetadata {
    private String fullName;
    private String dob;
    private String gender;
    private String specialty;
    private String licenseNumber;
    private Integer experienceYears;
    private String biography;
}
