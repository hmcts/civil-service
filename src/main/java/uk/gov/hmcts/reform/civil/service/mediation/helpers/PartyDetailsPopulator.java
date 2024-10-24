package uk.gov.hmcts.reform.civil.service.mediation.helpers;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.service.mediation.MediationLitigant;

@Component
public class PartyDetailsPopulator {

    private static final String PAPER_RESPONSE = "N";

    public MediationLitigant.MediationLitigantBuilder populator(MediationLitigant.MediationLitigantBuilder builder,
                                                                Party party) {
        String partyRole = party.getFlags() != null ? party.getFlags().getRoleOnCase() : null;
        return builder.partyID(party.getPartyID())
            .partyRole(partyRole)
            .partyType(party.getType())
            .partyName(party.getPartyName())
            .paperResponse(PAPER_RESPONSE);
    }

}
