package uk.gov.hmcts.reform.civil.model.docmosis.dq;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import uk.gov.hmcts.reform.civil.model.citizenui.EvidenceConfirmDetails;

import java.util.Optional;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
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
            return new LipExtraDQEvidenceConfirmDetails()
                .setFirstName(confirmDetails.getFirstName())
                .setLastName(confirmDetails.getLastName())
                .setEmail(confirmDetails.getEmail())
                .setPhone(confirmDetails.getPhone())
                .setJobTitle(confirmDetails.getJobTitle());
        }
        return null;
    }
}
