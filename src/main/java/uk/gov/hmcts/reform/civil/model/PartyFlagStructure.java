package uk.gov.hmcts.reform.civil.model;

import lombok.Data;
import uk.gov.hmcts.reform.civil.model.caseflags.Flags;
import lombok.experimental.Accessors;

@Data
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
@Accessors(chain = true)
public class PartyFlagStructure {

    private String partyID;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private Flags flags;

    public PartyFlagStructure copy() {
        return new PartyFlagStructure()
            .setPartyID(partyID)
            .setFirstName(firstName)
            .setLastName(lastName)
            .setEmail(email)
            .setPhone(phone)
            .setFlags(flags);
    }
}
