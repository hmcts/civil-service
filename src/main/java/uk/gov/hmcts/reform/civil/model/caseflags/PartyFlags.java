package uk.gov.hmcts.reform.civil.model.caseflags;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class PartyFlags extends Flags {

    private String partyId;

    public static PartyFlags from(Flags flags) {
        PartyFlags partyFlags = new PartyFlags();
        partyFlags.setPartyName(flags.getPartyName());
        partyFlags.setDetails(flags.getDetails());
        partyFlags.setRoleOnCase(flags.getRoleOnCase());
        return partyFlags;
    }
}
