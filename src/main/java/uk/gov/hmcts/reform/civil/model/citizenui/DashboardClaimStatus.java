package uk.gov.hmcts.reform.civil.model.citizenui;

import lombok.Getter;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.cmc.model.CmcClaim;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.function.Predicate;

public enum DashboardClaimStatus {
    RESPONSE_OVERDUE(
        claim -> claim.getRespondent1ResponseDeadline() != null && claim.getRespondent1ResponseDeadline()
            .isBefore(LocalDate.now().atTime(16, 1, 0)) && claim.hasBreathingSpace(),
        CmcClaim::isResponseDeadlineOnTime
    ),
    ELIGIBLE_FOR_CCJ(
        claim -> claim.getRespondent1ResponseDeadline() != null
            && claim.getRespondent1ResponseDeadline().isBefore(LocalDate.now().atTime(16, 1, 0)),
        CmcClaim::isEligibleForCCJ
    ),
    RESPONSE_DUE_NOW(
        claim -> claim.getRespondent1ResponseDeadline() != null && claim.getRespondent1ResponseDeadline().isEqual(
            LocalDateTime.now()) && claim.getRespondent1ResponseDeadline().isBefore(LocalDate.now().atTime(16, 1, 0)),
        CmcClaim::isResponseDeadlineToday
    ),
    MORE_TIME_REQUESTED(
        claim -> claim.getRespondent1TimeExtensionDate() != null,
        CmcClaim::isMoreTimeRequested
    ),
    NO_RESPONSE(
        claim -> claim.getRespondent1ResponseDate() == null,
        CmcClaim::isResponseDeadlineOnTime
    ),
    ADMIT_PAY_IMMEDIATELY(
        CaseData::isResponseFullAdmitAndPayImmediately,
        CmcClaim::responseIsFullAdmitAndPayImmediately
    ),
    ADMIT_PAY_BY_SET_DATE(
        CaseData::isResponseFullAdmitAndPayByInstallments,
        CmcClaim::responseIsFullAdmitAndPayBySetDate
    ),
    ADMIT_PAY_INSTALLMENTS(
        CaseData::isResponseFullAdmitAndPayByInstallments,
        CmcClaim::responseIsFullAdmitAndPayByInstallments
    ),
    CLAIMANT_ACCEPTED_STATES_PAID(
        claim -> claim.getRespondent1CourtOrderPayment() != null && claim.respondent1PaidInFull(),
        claim -> claim.getMoneyReceivedOn() != null || claim.isCCJSatisfied()
    ),
    TRANSFERRED(
        claim -> claim.getCcdState() == CaseState.JUDICIAL_REFERRAL,
        CmcClaim::isTransferred
    ),
    REQUESTED_COUNTRY_COURT_JUDGEMENT(
        claim -> claim.getApplicant1DQ().getApplicant1DQRequestedCourt() != null,
        claim -> claim.getClaimantResponse() != null && claim.getCountyCourtJudgmentRequestedAt() != null
    ),
    SETTLED(
        claim -> claim.respondent1PaidInFull() || claim.isRepsonseAcceptedByClaimant(),
        CmcClaim::claimantAcceptedDefendantResponse
    ),
    NO_STATUS;

    @Getter
    private final Predicate<CaseData> ccdClaimMatcher;
    @Getter
    private final Predicate<CmcClaim> cmcClaimMatcher;

    DashboardClaimStatus() {
        ccdClaimMatcher = c -> false;
        cmcClaimMatcher = c -> false;
    }
    DashboardClaimStatus(Predicate<CaseData> ccdClaimMatcher, Predicate<CmcClaim> cmcClaimMatcher) {
        this.ccdClaimMatcher = ccdClaimMatcher;
        this.cmcClaimMatcher = cmcClaimMatcher;
    }
}
