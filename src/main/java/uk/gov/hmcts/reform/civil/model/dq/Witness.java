package uk.gov.hmcts.reform.civil.model.dq;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder (toBuilder = true)
public class Witness {

    private final String partyID;
    @Deprecated
    private final String name;
    private final String firstName;
    private final String lastName;
    private final String emailAddress;
    private final String phoneNumber;
    private final String reasonForWitness;
    private final String eventAdded;
    private final LocalDate dateAdded;
}
