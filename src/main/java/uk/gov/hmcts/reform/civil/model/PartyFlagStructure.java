package uk.gov.hmcts.reform.civil.model;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.civil.model.caseflags.Flags;

@Data
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
@Builder(toBuilder = true)
public class PartyFlagStructure {

    private String partyID;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private Flags flags;
}
