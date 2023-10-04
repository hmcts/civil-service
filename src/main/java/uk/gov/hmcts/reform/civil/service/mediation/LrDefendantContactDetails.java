package uk.gov.hmcts.reform.civil.service.mediation;

import uk.gov.hmcts.reform.civil.prd.model.Organisation;

public class LrDefendantContactDetails extends LrContactDetails implements DefendantContactDetails {

    @Override
    public String getDefendantContactName(MediationParams params) {
        return params.getDefendantOrganisation()
            .map(Organisation::getName)
            .orElse("");
    }

    @Override
    public String getDefendantContactNumber(MediationParams params) {
        return getRepresentativeContactNumber(params.getDefendantOrganisation());
    }

    @Override
    public String getDefendantContactEmail(MediationParams params) {
        return params.getCaseData().getRespondentSolicitor1EmailAddress();
    }
}
