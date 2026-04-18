package vn.hust.agilechatbotbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EscalationRequest {

    /**
     * Doctor ID to assign for the escalated conversation.
     */
    private String doctorId;
}
