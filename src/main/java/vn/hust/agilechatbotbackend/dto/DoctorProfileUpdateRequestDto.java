package vn.hust.agilechatbotbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.hust.agilechatbotbackend.dto.metadata.DoctorMetadataDto;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DoctorProfileUpdateRequestDto {
    private String newPassword;
    private DoctorMetadataDto metadata;
}
