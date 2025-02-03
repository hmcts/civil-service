package uk.gov.hmcts.reform.civil.model.docmosis.dq;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.civil.model.citizenui.EvidenceConfirmDetails;

import java.util.Optional;

@Data
@Builder
public class LipExtraDQEvidenceConfirmDetails {

    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String jobTitle;

    @JsonIgnore
    public static LipExtraDQEvidenceConfirmDetails toLipExtraDQEvidenceConfirmDetails(
        Optional<EvidenceConfirmDetails> evidenceConfirmDetails) {
        if (evidenceConfirmDetails.isPresent()) {
            EvidenceConfirmDetails confirmDetails = evidenceConfirmDetails.get();
            return LipExtraDQEvidenceConfirmDetails.builder()
                .firstName(confirmDetails.getFirstName())
                .lastName(confirmDetails.getLastName())
                .email(confirmDetails.getEmail())
                .phone(confirmDetails.getPhone())
                .jobTitle(confirmDetails.getJobTitle())
                .build();
        }
        return null;
    }
}
