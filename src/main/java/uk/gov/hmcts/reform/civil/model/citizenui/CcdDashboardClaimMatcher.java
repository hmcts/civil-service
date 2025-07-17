package uk.gov.hmcts.reform.civil.model.citizenui;

import uk.gov.hmcts.reform.ccd.client.model.CaseEventDetail;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.DecisionOnRequestReconsiderationOptions;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.Element;
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
    protected static final int DAY_LIMIT = 42;

    protected CcdDashboardClaimMatcher(CaseData caseData,
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
    public boolean isHearingScheduled() {
        return caseData.getHearingDate() != null;
    }

    public boolean isSDOOrderCreatedPreCP() {
        String baseLocation = Optional.ofNullable(caseData.getCaseManagementLocation())
            .map(location -> location.getBaseLocation())
            .orElse(null);

        boolean isCaseProgression = CaseState.CASE_PROGRESSION.equals(caseData.getCcdState());
        boolean isBaseLocationValid = baseLocation != null;
        boolean isFeatureToggleEnabled = featureToggleService.isCaseProgressionEnabledAndLocationWhiteListed(
            baseLocation);
        Optional<LocalDateTime> sdoTime = getSDOTime();
        boolean isSDOTimeBeforeCPRelease = sdoTime.isPresent()
            && sdoTime.get().isBefore(LocalDateTime.of(2024, 12, 5, 0, 0));
        Optional<LocalDateTime> lastNonSdoOrderTime = getTimeOfLastNonSDOOrder();

        boolean isSDOTimeAfterLastNonSdoOrder = lastNonSdoOrderTime.isEmpty()
            || (sdoTime.isPresent() && sdoTime.get().isAfter(lastNonSdoOrderTime.get()));

        return isCaseProgression && isBaseLocationValid && (!isFeatureToggleEnabled
            || (isSDOTimeBeforeCPRelease && isSDOTimeAfterLastNonSdoOrder));
    }

    @Override
    public boolean decisionMadeDocumentsAreInTranslation() {
        return (featureToggleService.isWelshEnabledForMainCase() && caseData.getPreTranslationDocuments() != null
            && caseData.getPreTranslationDocuments().stream().map(
                    Element::getValue)
                .map(CaseDocument::getDocumentType)
            .anyMatch(type -> DocumentType.DECISION_MADE_ON_APPLICATIONS.equals(type))) && !isSettled()
            && !CaseState.CASE_DISCONTINUED.equals(caseData.getCcdState());
    }

    @Override
    public boolean sdoDocumentsAreInTranslation() {
        return (featureToggleService.isWelshEnabledForMainCase() && caseData.getPreTranslationDocuments() != null
            && caseData.getPreTranslationDocuments().stream().map(
                    Element::getValue)
            .map(CaseDocument::getDocumentType).anyMatch(type -> DocumentType.SDO_ORDER.equals(type))
            && !isSettled() && !CaseState.CASE_DISCONTINUED.equals(caseData.getCcdState()));
    }

    @Override
    public boolean isSDOOrderCreatedCP() {
        String baseLocation = Optional.ofNullable(caseData.getCaseManagementLocation())
            .map(location -> location.getBaseLocation())
            .orElse(null);
        Optional<LocalDateTime> lastNonSdoOrderTime = getTimeOfLastNonSDOOrder();
        Optional<LocalDateTime> sdoTime = getSDOTime();
        return CaseState.CASE_PROGRESSION.equals(caseData.getCcdState())
            && baseLocation != null
            && featureToggleService.isCaseProgressionEnabledAndLocationWhiteListed(caseData.getCaseManagementLocation().getBaseLocation())
            && !isSDOOrderLegalAdviserCreated()
            && !isSDOOrderInReview()
            && !isSDOOrderInReviewOtherParty()
            && !isDecisionForReconsiderationMade()
            && sdoTime.isPresent()
            && (lastNonSdoOrderTime.isEmpty()
            || sdoTime.get().isAfter(lastNonSdoOrderTime.get()));
    }

    @Override
    public boolean isSDOOrderLegalAdviserCreated() {
        Optional<LocalDateTime> lastNonSdoOrderTime = getTimeOfLastNonSDOOrder();
        Optional<LocalDateTime> sdoTime = getSDOTime();
        return featureToggleService.isCaseProgressionEnabled()
            && CaseState.CASE_PROGRESSION.equals(caseData.getCcdState())
            && isSDOMadeByLegalAdviser()
            && !isSDOOrderInReview()
            && !isSDOOrderInReviewOtherParty()
            && !isDecisionForReconsiderationMade()
            && sdoTime.isPresent()
            && !isSDODoneAfterDecisionForReconsiderationMade()
            && !isGeneralOrderAfterDecisionForReconsiderationMade()
            && (lastNonSdoOrderTime.isEmpty()
            || sdoTime.get().isAfter(lastNonSdoOrderTime.get()));
    }

    protected boolean isSDODoneAfterDecisionForReconsiderationMade() {
        return caseData.getDecisionOnRequestReconsiderationOptions() == DecisionOnRequestReconsiderationOptions.CREATE_SDO
            && caseData.getDocumentListByType(caseData.getSystemGeneratedCaseDocuments(), DocumentType.SDO_ORDER).map(List::size).orElse(0) == 2;
    }

    protected boolean isGeneralOrderAfterDecisionForReconsiderationMade() {
        return caseData.getDecisionOnRequestReconsiderationOptions() == DecisionOnRequestReconsiderationOptions.CREATE_GENERAL_ORDER
            && caseData.getDocumentListByType(caseData.getFinalOrderDocumentCollection(), DocumentType.JUDGE_FINAL_ORDER).isPresent();
    }

    @Override
    public boolean isHearingLessThanDaysAway(int days) {
        return caseData.getHearingDate() != null
            && LocalDate.now().plusDays(days + 1L).isAfter(caseData.getHearingDate());
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
        return getMostRecentEventOfType(EnumSet.of(CaseEvent.HEARING_SCHEDULED, CaseEvent.GENERATE_HEARING_NOTICE_HMC))
            .map(CaseEventDetail::getCreatedDate);
    }

    protected Optional<LocalDateTime> getTimeOfMostRecentEventOfType(Set<CaseEvent> events) {
        return getMostRecentEventOfType(events)
            .map(CaseEventDetail::getCreatedDate);
    }

    protected Optional<CaseEventDetail> getMostRecentEventOfType(Set<CaseEvent> events) {
        Set<String> eventNames = events.stream().map(CaseEvent::name).collect(Collectors.toSet());
        return eventHistory.stream()
            .filter(e -> eventNames.contains(e.getId()))
            .findFirst();
    }

    @Override
    public boolean isTrialScheduledNoPaymentStatusActive() {
        Optional<LocalDateTime> hearingScheduledDate = getWhenWasHearingScheduled();
        Optional<LocalDateTime> orderDate = getTimeOfLastNonSDOOrder();
        return CaseState.HEARING_READINESS.equals(caseData.getCcdState())
            && (hearingScheduledDate.isPresent())
            && !isTrialArrangementStatusActive()
            && !isBundleCreatedStatusActive()
            && (orderDate.isEmpty()
            || orderDate.get().isBefore(hearingScheduledDate.get()));
    }

    @Override
    public boolean isTrialScheduledPaymentPaidStatusActive() {
        Optional<LocalDateTime> hearingScheduledDate = getWhenWasHearingScheduled();
        Optional<LocalDateTime> orderDate = getTimeOfLastNonSDOOrder();
        return CaseState.PREPARE_FOR_HEARING_CONDUCT_HEARING.equals(caseData.getCcdState())
            && (hearingScheduledDate.isPresent())
            && !isTrialArrangementStatusActive()
            && !isBundleCreatedStatusActive()
            && (orderDate.isEmpty()
            || orderDate.get().isBefore(hearingScheduledDate.get()));
    }

    @Override
    public boolean isBundleCreatedStatusActive() {
        Optional<LocalDateTime> bundleDate = getBundleCreationDate();
        Optional<LocalDateTime> lastOrderDate = getTimeOfLastNonSDOOrder();
        return isHearingScheduled()
            && (CaseState.HEARING_READINESS.equals(caseData.getCcdState())
                || caseData.getCcdState() == CaseState.PREPARE_FOR_HEARING_CONDUCT_HEARING)
            && isHearingLessThanDaysAway(3 * 7)
            && bundleDate.isPresent()
            && (lastOrderDate.isEmpty()
                || lastOrderDate.get().isBefore(bundleDate.get()));
    }

    @Override
    public boolean isWaitingForClaimantIntentDocUploadPreDefendantNocOnline() {
        return isWaitingForClaimantIntentDocUpload()
            && !featureToggleService.isDefendantNoCOnlineForCase(caseData);
    }

    @Override
    public boolean isWaitingForClaimantIntentDocUploadPostDefendantNocOnline() {
        return isWaitingForClaimantIntentDocUpload()
            && featureToggleService.isDefendantNoCOnlineForCase(caseData);
    }

    private boolean isWaitingForClaimantIntentDocUpload() {
        return caseData.isRespondentResponseFullDefence() && caseData.getApplicant1ResponseDate() != null
            && caseData.getCcdState() == CaseState.AWAITING_APPLICANT_INTENTION && caseData.isClaimantBilingual();
    }

    @Override
    public boolean awaitingHearingNoticeTranslationNotSettledOrDiscontinued() {
        return awaitingHearingNoticeTranslation() && !isSettled() && !isCasedDiscontinued();
    }

    private boolean awaitingHearingNoticeTranslation() {
        return caseData.getPreTranslationDocuments() != null
            && caseData.getPreTranslationDocuments().stream().anyMatch(
                element -> element.getValue().getDocumentType() == DocumentType.HEARING_FORM
        );
    }
}
