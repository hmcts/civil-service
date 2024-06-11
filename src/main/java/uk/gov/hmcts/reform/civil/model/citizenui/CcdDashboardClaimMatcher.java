package uk.gov.hmcts.reform.civil.model.citizenui;

import lombok.AllArgsConstructor;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.math.BigDecimal;

@AllArgsConstructor
public abstract class CcdDashboardClaimMatcher {

    protected CaseData caseData;

    public boolean hasClaimantAndDefendantSignedSettlementAgreement() {
        return caseData.hasApplicant1SignedSettlementAgreement() && caseData.isRespondentSignedSettlementAgreement() && !isSettled();
    }

    public boolean hasDefendantRejectedSettlementAgreement() {
        return caseData.hasApplicant1SignedSettlementAgreement() && caseData.isRespondentRespondedToSettlementAgreement()
            && !caseData.isRespondentSignedSettlementAgreement() && !isSettled();
    }

    public boolean hasClaimantSignedSettlementAgreement() {
        return caseData.hasApplicant1SignedSettlementAgreement() && !caseData.isSettlementAgreementDeadlineExpired() && !isSettled();
    }

    public boolean hasClaimantSignedSettlementAgreementAndDeadlineExpired() {
        return caseData.hasApplicant1SignedSettlementAgreement() && caseData.isSettlementAgreementDeadlineExpired() && !isSettled();
    }

    public boolean isSettled() {
        return caseData.getCcdState() == CaseState.CASE_SETTLED;
    }

    protected boolean isSDOMadeByLegalAdviser() {
        return caseData.getHearingDate() == null
            && CaseState.CASE_PROGRESSION.equals(caseData.getCcdState())
            && caseData.isSmallClaim()
            && caseData.getTotalClaimAmount().intValue() <= BigDecimal.valueOf(10000).intValue();
    }
}
