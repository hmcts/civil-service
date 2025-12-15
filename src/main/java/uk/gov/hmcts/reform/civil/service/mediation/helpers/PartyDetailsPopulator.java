package uk.gov.hmcts.reform.civil.service.mediation.helpers;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.service.mediation.MediationLitigant;

@Component
public class PartyDetailsPopulator {

    private static final String PAPER_RESPONSE = "N";

    public MediationLitigant populator(MediationLitigant builder,
                                                                Party party) {
        String partyRole = party.getFlags() != null ? party.getFlags().getRoleOnCase() : null;
        return builder.setPartyID(party.getPartyID())
            .setPartyRole(partyRole)
            .setPartyType(party.getType())
            .setPartyName(party.getPartyName())
            .setPaperResponse(PAPER_RESPONSE);
    }

}
