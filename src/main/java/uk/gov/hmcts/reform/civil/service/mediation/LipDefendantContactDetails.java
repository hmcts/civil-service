package uk.gov.hmcts.reform.civil.service.mediation;

public class LipDefendantContactDetails extends LipContactDetails implements DefendantContactDetails {

    @Override
    public String getDefendantContactName(MediationParams params) {
        return getCsvIndividualName(params.getCaseData().getRespondent1());
    }

    @Override
    public String getDefendantContactNumber(MediationParams params) {
        return params.getCaseData().getRespondent1().getPartyPhone();
    }

    @Override
    public String getDefendantContactEmail(MediationParams params) {
        return params.getCaseData().getRespondent1().getPartyEmail();
    }
}
