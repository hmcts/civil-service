package uk.gov.hmcts.reform.civil.model.citizenui;

import uk.gov.hmcts.reform.ccd.client.model.CaseEventDetail;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class CcdDashboardClaimMatcher implements Claim {

    protected CaseData caseData;
    protected FeatureToggleService featureToggleService;
    /**
     * sorted in descending order by creation date.
     */
    protected List<CaseEventDetail> eventHistory;

    public CcdDashboardClaimMatcher(CaseData caseData,
                                    FeatureToggleService featureToggleService,
                                    List<CaseEventDetail> eventHistory) {
        this.caseData = caseData;
        this.featureToggleService = featureToggleService;
        this.eventHistory = eventHistory;
        eventHistory.sort(Comparator.comparing(
            CaseEventDetail::getCreatedDate
        ).reversed());
    }

    public boolean hasClaimantAndDefendantSignedSettlementAgreement() {
        return caseData.hasApplicant1SignedSettlementAgreement() && caseData.isRespondentSignedSettlementAgreement() && !isSettled();
    }

    public boolean hasDefendantRejectedSettlementAgreement() {
        return caseData.hasApplicant1SignedSettlementAgreement() && caseData.isRespondentRespondedToSettlementAgreement()
            && !caseData.isRespondentSignedSettlementAgreement() && !isSettled()
            && !caseData.isCcjRequestJudgmentByAdmission();
    }

    public boolean hasClaimantSignedSettlementAgreement() {
        return caseData.hasApplicant1SignedSettlementAgreement()
            && !caseData.isSettlementAgreementDeadlineExpired() && !isSettled()
            && !caseData.isCcjRequestJudgmentByAdmission();
    }

    public boolean hasClaimantSignedSettlementAgreementAndDeadlineExpired() {
        return caseData.hasApplicant1SignedSettlementAgreement()
            && caseData.isSettlementAgreementDeadlineExpired()
            && !isSettled()
            && !caseData.isCcjRequestJudgmentByAdmission();
    }

    public boolean isSettled() {
        return caseData.getCcdState() == CaseState.CASE_SETTLED;
    }

    public boolean isClaimProceedInCaseMan() {
        List<CaseState> caseMovedInCaseManStates = List.of(CaseState.AWAITING_APPLICANT_INTENTION,
                                                           CaseState.AWAITING_RESPONDENT_ACKNOWLEDGEMENT,
                                                           CaseState.IN_MEDIATION, CaseState.JUDICIAL_REFERRAL
        );

        return Objects.nonNull(caseData.getTakenOfflineDate())
            && Objects.nonNull(caseData.getPreviousCCDState())
            && (caseMovedInCaseManStates.contains(caseData.getPreviousCCDState()));
    }

    protected boolean isSDOMadeByLegalAdviser() {
        return caseData.getHearingDate() == null
            && CaseState.CASE_PROGRESSION.equals(caseData.getCcdState())
            && caseData.isSmallClaim()
            && (caseData.getTotalClaimAmount().compareTo(BigDecimal.valueOf(1000)) <= 0);
    }

    public boolean isCaseStruckOut() {
        return Objects.nonNull(caseData.getCaseDismissedHearingFeeDueDate());
    }

    public boolean hasResponseDeadlineBeenExtended() {
        return caseData.getRespondent1TimeExtensionDate() != null
            && caseData.getCcdState() == CaseState.AWAITING_RESPONDENT_ACKNOWLEDGEMENT;
    }

    @Override
    public boolean isOrderMade() {
        return (caseData.getCcdState() == CaseState.All_FINAL_ORDERS_ISSUED
            || caseData.getCcdState() == CaseState.CASE_PROGRESSION)
            && getTimeOfLastNonSDOOrder().isPresent();
    }

    @Override
    public Optional<LocalDateTime> getTimeOfLastNonSDOOrder() {
        if (caseData.getCcdState() == CaseState.CASE_PROGRESSION
            || caseData.getCcdState() == CaseState.All_FINAL_ORDERS_ISSUED) {
            return getTimeOfMostRecentEventOfType(EnumSet.of(
                CaseEvent.COURT_OFFICER_ORDER,
                CaseEvent.GENERATE_DIRECTIONS_ORDER
            ));
        } else {
            return Optional.empty();
        }
    }

    protected Optional<LocalDateTime> getSDOTime() {
        return caseData.getSDODocument().map(d -> d.getValue().getCreatedDatetime());
    }

    @Override
    public boolean isSDOOrderCreated() {
        Optional<LocalDateTime> lastNonSdoOrderTime;
        Optional<LocalDateTime> sdoTime;
        return caseData.getHearingDate() == null
            && CaseState.CASE_PROGRESSION.equals(caseData.getCcdState())
            && !isSDOOrderLegalAdviserCreated()
            && !isSDOOrderInReview()
            && !isSDOOrderInReviewOtherParty()
            && !isDecisionForReconsiderationMade()
            && (sdoTime = getSDOTime()).isPresent()
            && ((lastNonSdoOrderTime = getTimeOfLastNonSDOOrder()).isEmpty()
            || sdoTime.get().isAfter(lastNonSdoOrderTime.get()));
    }

    @Override
    public boolean isSDOOrderLegalAdviserCreated() {
        Optional<LocalDateTime> lastNonSdoOrderTime;
        Optional<LocalDateTime> sdoTime;
        return featureToggleService.isCaseProgressionEnabled()
            && caseData.getHearingDate() == null
            && isSDOMadeByLegalAdviser()
            && !isSDOOrderInReview()
            && !isSDOOrderInReviewOtherParty()
            && !isDecisionForReconsiderationMade()
            && (sdoTime = getSDOTime()).isPresent()
            && ((lastNonSdoOrderTime = getTimeOfLastNonSDOOrder()).isEmpty()
            || sdoTime.get().isAfter(lastNonSdoOrderTime.get()));
    }

    @Override
    public boolean isMoreDetailsRequired() {
        Optional<LocalDateTime> lastOrder;
        Optional<LocalDateTime> sdoTime;
        return (sdoTime = getSDOTime()).isPresent()
            && isBeforeHearing()
            && featureToggleService.isCaseProgressionEnabled()
            && ((lastOrder = getTimeOfLastNonSDOOrder()).isEmpty()
            || lastOrder.get().isBefore(sdoTime.get()));
    }

    @Override
    public boolean isHearingLessThanDaysAway(int i) {
        return caseData.getHearingDate() != null
            && LocalDate.now().plusDays(i + 1).isAfter(caseData.getHearingDate());
    }

    @Override
    public Optional<LocalDate> getHearingDate() {
        return Optional.ofNullable(caseData.getHearingDate());
    }

    @Override
    public Optional<LocalDateTime> getBundleCreationDate() {
        if (caseData.getHearingDate() != null) {
            return caseData.getCaseBundles().stream()
                .filter(b -> b.getValue().getCreatedOn().isPresent()
                    && b.getValue().getBundleHearingDate()
                    .map(d -> caseData.getHearingDate().equals(d)).orElse(Boolean.FALSE))
                .map(d -> d.getValue().getCreatedOn().orElse(null))
                .max(LocalDateTime::compareTo);
        }
        return Optional.empty();
    }

    @Override
    public Optional<LocalDateTime> getWhenWasHearingScheduled() {
        return eventHistory.stream().filter(e -> CaseEvent.HEARING_SCHEDULED.name()
            .equals(e.getEventName())).findFirst().map(CaseEventDetail::getCreatedDate);
    }

    protected Optional<LocalDateTime> getTimeOfMostRecentEventOfType(Set<CaseEvent> events) {
        return getMostRecentEventOfType(events)
            .map(CaseEventDetail::getCreatedDate);
    }

    protected Optional<CaseEventDetail> getMostRecentEventOfType(Set<CaseEvent> events) {
        Set<String> eventNames = events.stream().map(CaseEvent::name).collect(Collectors.toSet());
        return eventHistory.stream()
            .filter(e -> eventNames.contains(e.getEventName()))
            .findFirst();
    }
}
