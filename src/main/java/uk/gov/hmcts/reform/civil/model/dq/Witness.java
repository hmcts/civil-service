package uk.gov.hmcts.reform.civil.model.dq;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Witness {

    // CIV-5557 to be removed
    @Deprecated
    private final String name;
    private final String firstName;
    private final String lastName;
    private final String emailAddress;
    private final String phoneNumber;
    private final String reasonForWitness;
}
