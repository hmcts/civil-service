package uk.gov.hmcts.reform.civil.model.caseflags;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder(toBuilder = true)
public class PartyFlags extends Flags {

    private final String partyId;

    public static PartyFlags from(Flags flags) {
        return PartyFlags.builder()
            .partyName(flags.getPartyName())
            .details(flags.getDetails())
            .roleOnCase(flags.getRoleOnCase())
            .build();
    }
}
