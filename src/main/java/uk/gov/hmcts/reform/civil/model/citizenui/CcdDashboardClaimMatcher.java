package uk.gov.hmcts.reform.civil.model.citizenui;

import lombok.AllArgsConstructor;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

@AllArgsConstructor
public abstract class CcdDashboardClaimMatcher implements Claim {

    protected CaseData caseData;
    protected FeatureToggleService featureToggleService;

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
    public boolean isOrderMadeLast() {
        return caseData.getCcdState() == CaseState.All_FINAL_ORDERS_ISSUED
            || (caseData.getCcdState() == CaseState.CASE_PROGRESSION
            && getTimeOfLastNonSDOOrder().isPresent()
        );
    }

    @Override
    public Optional<LocalDateTime> getTimeOfLastNonSDOOrder() {
        return Stream.concat(
                Stream.ofNullable(caseData.getPreviewCourtOfficerOrder()),
                caseData.getFinalOrderDocumentCollection().stream()
                    .map(Element::getValue)
            ).filter(Objects::nonNull).map(CaseDocument::getCreatedDatetime)
            .max(LocalDateTime::compareTo);
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
}
