package uk.gov.hmcts.reform.civil.service.mediation;

import uk.gov.hmcts.reform.civil.model.Party;

public abstract class LipContactDetails {

    protected String getCsvIndividualName(Party party) {
        return (party.isIndividual() || party.isSoleTrader()) ? party.getPartyName() : null;
    }

}
