package uk.gov.hmcts.reform.civil.model.citizenui;

import lombok.AllArgsConstructor;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

@AllArgsConstructor
public class CcdDashboardClaimMatcher {

    protected CaseData caseData;

    public boolean hasClaimantAndDefendantSignedSettlementAgreement() {
        return caseData.hasApplicant1SignedSettlementAgreement() && caseData.isRespondentSignedSettlementAgreement();
    }

    public boolean hasDefendantRejectedSettlementAgreement() {
        return caseData.hasApplicant1SignedSettlementAgreement() && caseData.isRespondentRespondedToSettlementAgreement()
            && !caseData.isRespondentSignedSettlementAgreement();
    }

    public boolean hasClaimantSignedSettlementAgreement() {
        return caseData.hasApplicant1SignedSettlementAgreement() && !caseData.isSettlementAgreementDeadlineExpired();
    }

    public boolean hasClaimantSignedSettlementAgreementAndDeadlineExpired() {
        return caseData.hasApplicant1SignedSettlementAgreement() && caseData.isSettlementAgreementDeadlineExpired();
    }


}
