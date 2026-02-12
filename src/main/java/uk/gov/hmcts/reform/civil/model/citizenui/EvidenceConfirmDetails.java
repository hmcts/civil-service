package uk.gov.hmcts.reform.civil.model.citizenui;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class EvidenceConfirmDetails {

    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String jobTitle;
}
