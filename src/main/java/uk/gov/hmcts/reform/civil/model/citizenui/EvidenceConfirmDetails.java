package uk.gov.hmcts.reform.civil.model.citizenui;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder(toBuilder = true)
@AllArgsConstructor
public class EvidenceConfirmDetails {

    private final String firstName;
    private final String lastName;
    private final String email;
    private final String phone;
    private final String jobTitle;
}
