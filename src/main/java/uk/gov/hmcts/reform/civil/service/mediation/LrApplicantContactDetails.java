package uk.gov.hmcts.reform.civil.service.mediation;

import uk.gov.hmcts.reform.civil.model.CorrectEmail;
import uk.gov.hmcts.reform.civil.prd.model.Organisation;

import java.util.Optional;

public class LrApplicantContactDetails extends LrContactDetails implements ApplicantContactDetails {

    @Override
    public String getApplicantContactName(MediationParams params) {
        return params.getApplicantOrganisation()
            .map(Organisation::getName)
            .orElse(params.getCaseData().getApplicantSolicitor1ClaimStatementOfTruth().getName());
    }

    @Override
    public String getApplicantContactNumber(MediationParams params) {
        return getRepresentativeContactNumber(params.getApplicantOrganisation());
    }

    @Override
    public String getApplicantContactEmail(MediationParams params) {
        return Optional.ofNullable(params.getCaseData().getApplicantSolicitor1CheckEmail()).map(CorrectEmail::getEmail)
            .orElse(params.getCaseData().getApplicantSolicitor1UserDetails().getEmail());
    }
}
