package uk.gov.hmcts.reform.civil.model.citizenui;

import lombok.AllArgsConstructor;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.util.Objects;

@AllArgsConstructor
public class CcdDashboardClaimMatcher {

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
        return !caseData.isRespondentResponseFullDefence()
            && (caseData.respondent1PaidInFull()
            || caseData.isResponseAcceptedByClaimant())
            && Objects.isNull(caseData.getCcjPaymentDetails())
            && !caseData.hasApplicantRejectedRepaymentPlan()
            || caseData.isPartAdmitClaimSettled();
    }
}
