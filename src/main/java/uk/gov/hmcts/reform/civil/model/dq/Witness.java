package uk.gov.hmcts.reform.civil.model.dq;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder (toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class Witness {

    private String partyID;
    @Deprecated
    private String name;
    private String firstName;
    private String lastName;
    private String emailAddress;
    private String phoneNumber;
    private String reasonForWitness;
    private String eventAdded;
    private LocalDate dateAdded;
}
