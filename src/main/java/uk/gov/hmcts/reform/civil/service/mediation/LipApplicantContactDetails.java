package uk.gov.hmcts.reform.civil.service.mediation;

public class LipApplicantContactDetails extends LipContactDetails implements ApplicantContactDetails {

    @Override
    public String getApplicantContactName(MediationParams params) {
        return getCsvIndividualName(params.getCaseData().getApplicant1());
    }

    @Override
    public String getApplicantContactNumber(MediationParams params) {
        return params.getCaseData().getApplicant1().getPartyPhone();
    }

    @Override
    public String getApplicantContactEmail(MediationParams params) {
        return params.getCaseData().getApplicant1Email();
    }
}
