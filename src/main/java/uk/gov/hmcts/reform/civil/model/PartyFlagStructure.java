package uk.gov.hmcts.reform.civil.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uk.gov.hmcts.reform.civil.model.caseflags.Flags;

@Data
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class PartyFlagStructure {

    private String partyID;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private Flags flags;
}
