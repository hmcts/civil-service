package uk.gov.hmcts.reform.civil.model.citizenui;

import lombok.AllArgsConstructor;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.util.List;
import java.util.Objects;

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

    public boolean isClaimProceedInCaseMan() {
        List<CaseState> caseMovedInCaseManStates = List.of(CaseState.AWAITING_APPLICANT_INTENTION,
                CaseState.AWAITING_RESPONDENT_ACKNOWLEDGEMENT,
                CaseState.IN_MEDIATION, CaseState.JUDICIAL_REFERRAL);

        return  Objects.nonNull(caseData.getTakenOfflineDate())
                && Objects.nonNull(caseData.getPreviousCCDState())
                && (caseMovedInCaseManStates.contains(caseData.getPreviousCCDState()));
    }
}
