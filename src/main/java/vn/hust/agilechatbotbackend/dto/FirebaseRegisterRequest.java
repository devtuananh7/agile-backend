package vn.hust.agilechatbotbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.hust.agilechatbotbackend.dto.metadata.UserMetadata;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FirebaseRegisterRequest {

    private String phoneNumber;
    private UserMetadata metadata;
}
