package uk.gov.hmcts.reform.civil.service.robotics.strategy;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CCJPaymentDetails;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.PaymentBySetDate;
import uk.gov.hmcts.reform.civil.model.RepaymentPlanLRspec;
import uk.gov.hmcts.reform.civil.model.RespondToClaimAdmitPartLRspec;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.ClaimantLiPResponse;
import uk.gov.hmcts.reform.civil.model.robotics.Event;
import uk.gov.hmcts.reform.civil.model.robotics.EventDetails;
import uk.gov.hmcts.reform.civil.model.robotics.EventHistory;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.robotics.mapper.EventHistoryMapper;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsEventTextFormatter;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsSequenceGenerator;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsTimelineHelper;
import uk.gov.hmcts.reform.civil.service.robotics.mapper.ClaimFeeUtility;
import uk.gov.hmcts.reform.civil.utils.MonetaryConversions;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static java.math.BigDecimal.ZERO;
import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.civil.model.robotics.EventType.JUDGEMENT_BY_ADMISSION;
import static uk.gov.hmcts.reform.civil.model.robotics.EventType.MISCELLANEOUS;
import static uk.gov.hmcts.reform.civil.service.robotics.utils.RoboticsDataUtil.APPLICANT_ID;
import static uk.gov.hmcts.reform.civil.service.robotics.utils.RoboticsDataUtil.RESPONDENT_ID;

@Component
@RequiredArgsConstructor
public class JudgmentByAdmissionStrategy implements EventHistoryStrategy {

    private final FeatureToggleService featureToggleService;
    private final RoboticsEventTextFormatter textFormatter;
    private final RoboticsSequenceGenerator sequenceGenerator;
    private final RoboticsTimelineHelper timelineHelper;

    @Override
    public boolean supports(CaseData caseData) {
        return caseData != null && caseData.isCcjRequestJudgmentByAdmission();
    }

    @Override
    public void contribute(EventHistory.EventHistoryBuilder builder, CaseData caseData, String authToken) {
        if (!supports(caseData)) {
            return;
        }

        builder.miscellaneous(buildMiscellaneousEvent(builder, caseData));
        builder.judgmentByAdmission(buildJudgmentByAdmissionEvent(builder, caseData));
    }

    private Event buildMiscellaneousEvent(EventHistory.EventHistoryBuilder builder, CaseData caseData) {
        boolean joLiveFeedActive = featureToggleService.isJOLiveFeedActive();
        String miscText = textFormatter.judgmentByAdmissionOffline();
        String detailsText = miscText;
        if (joLiveFeedActive) {
            miscText = EventHistoryMapper.RECORD_JUDGMENT;
            detailsText = textFormatter.judgmentRecorded();
        }

        return Event.builder()
            .eventSequence(sequenceGenerator.nextSequence(builder.build()))
            .eventCode(MISCELLANEOUS.getCode())
            .dateReceived(resolveJudgmentDate(caseData))
            .eventDetailsText(detailsText)
            .eventDetails(EventDetails.builder().miscText(miscText).build())
            .build();
    }

    private Event buildJudgmentByAdmissionEvent(EventHistory.EventHistoryBuilder builder, CaseData caseData) {
        boolean joLiveFeedActive = featureToggleService.isJOLiveFeedActive();
        return Event.builder()
            .eventSequence(sequenceGenerator.nextSequence(builder.build()))
            .eventCode(JUDGEMENT_BY_ADMISSION.getCode())
            .litigiousPartyID(joLiveFeedActive ? RESPONDENT_ID : APPLICANT_ID)
            .dateReceived(resolveJudgmentDate(caseData))
            .eventDetails(buildJudgmentDetails(caseData))
            .eventDetailsText("")
            .build();
    }

    private EventDetails buildJudgmentDetails(CaseData caseData) {
        Optional<CCJPaymentDetails> ccjPaymentDetails = ofNullable(caseData.getCcjPaymentDetails());
        BigDecimal amountOfCosts = resolveAmountOfCosts(caseData, ccjPaymentDetails);

        return EventDetails.builder()
            .amountOfJudgment(resolveJudgmentAmount(caseData))
            .amountOfCosts(amountOfCosts)
            .amountPaidBeforeJudgment(ccjPaymentDetails
                .map(CCJPaymentDetails::getCcjPaymentPaidSomeAmountInPounds)
                .map(amountPaid -> amountPaid.setScale(2)).orElse(ZERO))
            .isJudgmentForthwith(hasCourtDecisionInFavourOfClaimant(caseData)
                ? caseData.applicant1SuggestedPayImmediately()
                : caseData.isPayImmediately())
            .paymentInFullDate(resolvePaymentInFullDate(caseData))
            .installmentAmount(resolveInstallmentAmount(caseData))
            .installmentPeriod(resolveInstallmentPeriod(caseData))
            .firstInstallmentDate(resolveFirstInstallmentDate(caseData))
            .dateOfJudgment(resolveJudgmentDate(caseData))
            .jointJudgment(false)
            .judgmentToBeRegistered(true)
            .miscText("")
            .build();
    }

    private BigDecimal resolveAmountOfCosts(CaseData caseData, Optional<CCJPaymentDetails> ccjPaymentDetails) {
        BigDecimal fixedCost = ccjPaymentDetails
            .map(CCJPaymentDetails::getCcjJudgmentFixedCostAmount)
            .orElse(ZERO);
        BigDecimal claimFee = ccjPaymentDetails
            .map(CCJPaymentDetails::getCcjJudgmentAmountClaimFee)
            .map(amount -> amount.setScale(2))
            .orElse(ZERO);

        if (caseData.isApplicantLipOneVOne() && featureToggleService.isLipVLipEnabled()) {
            fixedCost = ClaimFeeUtility.getCourtFee(caseData);
            claimFee = ZERO;
        }
        return fixedCost.add(claimFee);
    }

    private BigDecimal resolveJudgmentAmount(CaseData caseData) {
        Optional<CCJPaymentDetails> ccjPaymentDetails = ofNullable(caseData.getCcjPaymentDetails());
        BigDecimal base = ccjPaymentDetails
            .map(CCJPaymentDetails::getCcjJudgmentAmountClaimAmount)
            .orElse(ZERO);
        BigDecimal interest = caseData.isLipvLipOneVOne() && !caseData.isPartAdmitClaimSpec()
            ? ccjPaymentDetails.map(CCJPaymentDetails::getCcjJudgmentLipInterest).orElse(ZERO)
            : totalInterestForLrClaim(caseData);
        return base.add(interest).setScale(2);
    }

    private BigDecimal totalInterestForLrClaim(CaseData caseData) {
        return featureToggleService.isLrAdmissionBulkEnabled()
            ? ZERO
            : ofNullable(caseData.getTotalInterest()).orElse(ZERO);
    }

    private LocalDateTime resolveJudgmentDate(CaseData caseData) {
        return featureToggleService.isJOLiveFeedActive()
            ? caseData.getJoJudgementByAdmissionIssueDate()
            : timelineHelper.ensurePresentOrNow(caseData.getApplicant1ResponseDate());
    }

    private LocalDateTime resolvePaymentInFullDate(CaseData caseData) {
        RespondToClaimAdmitPartLRspec admitPart = caseData.getRespondToClaimAdmitPartLRspec();
        if (hasCourtDecisionInFavourOfClaimant(caseData)) {
            if (caseData.applicant1SuggestedPayBySetDate()) {
                return ofNullable(caseData.getApplicant1RequestedPaymentDateForDefendantSpec())
                    .map(PaymentBySetDate::getPaymentSetDate)
                    .map(LocalDate::atStartOfDay)
                    .orElse(null);
            }
            return null;
        }
        return caseData.isPayBySetDate()
            ? ofNullable(admitPart)
                .map(RespondToClaimAdmitPartLRspec::getWhenWillThisAmountBePaid)
                .map(LocalDate::atStartOfDay)
                .orElse(null)
            : null;
    }

    private BigDecimal resolveInstallmentAmount(CaseData caseData) {
        boolean claimantFavoursInstalments = hasCourtDecisionInFavourOfClaimant(caseData)
            ? caseData.applicant1SuggestedPayByInstalments()
            : caseData.isPayByInstallment();

        if (!claimantFavoursInstalments) {
            return null;
        }

        BigDecimal repaymentAmount = hasCourtDecisionInFavourOfClaimant(caseData)
            ? caseData.getApplicant1SuggestInstalmentsPaymentAmountForDefendantSpec()
            : ofNullable(caseData.getRespondent1RepaymentPlan())
                .map(RepaymentPlanLRspec::getPaymentAmount)
                .orElse(ZERO);

        return MonetaryConversions.penniesToPounds(
            ofNullable(repaymentAmount).map(amount -> amount.setScale(2, RoundingMode.HALF_UP)).orElse(ZERO)
        );
    }

    private LocalDate resolveFirstInstallmentDate(CaseData caseData) {
        if (hasCourtDecisionInFavourOfClaimant(caseData)) {
            return caseData.applicant1SuggestedPayByInstalments()
                ? caseData.getApplicant1SuggestInstalmentsFirstRepaymentDateForDefendantSpec()
                : null;
        }
        return caseData.isPayByInstallment()
            ? ofNullable(caseData.getRespondent1RepaymentPlan())
                .map(RepaymentPlanLRspec::getFirstRepaymentDate)
                .orElse(null)
            : null;
    }

    private String resolveInstallmentPeriod(CaseData caseData) {
        boolean joLiveFeedActive = featureToggleService.isJOLiveFeedActive();
        boolean payByInstallment = hasCourtDecisionInFavourOfClaimant(caseData)
            ? caseData.applicant1SuggestedPayByInstalments()
            : caseData.isPayByInstallment();
        if (payByInstallment) {
            return mapFrequency(caseData, hasCourtDecisionInFavourOfClaimant(caseData));
        }
        boolean payBySetDate = hasCourtDecisionInFavourOfClaimant(caseData)
            ? caseData.applicant1SuggestedPayBySetDate()
            : caseData.isPayBySetDate();
        boolean payImmediately = hasCourtDecisionInFavourOfClaimant(caseData)
            ? caseData.applicant1SuggestedPayImmediately()
            : caseData.isPayImmediately();
        if (joLiveFeedActive && payBySetDate) {
            return "FUL";
        } else if (joLiveFeedActive && payImmediately) {
            return "FW";
        }
        return null;
    }

    private String mapFrequency(CaseData caseData, boolean applicantDecision) {
        String frequency;
        if (applicantDecision) {
            frequency = ofNullable(caseData.getApplicant1SuggestInstalmentsRepaymentFrequencyForDefendantSpec())
                .map(Enum::name)
                .orElse("");
        } else {
            frequency = ofNullable(caseData.getRespondent1RepaymentPlan())
                .map(RepaymentPlanLRspec::getRepaymentFrequency)
                .map(Enum::name)
                .orElse("");
        }
        return switch (frequency) {
            case "ONCE_ONE_WEEK" -> "WK";
            case "ONCE_TWO_WEEKS" -> "FOR";
            case "ONCE_ONE_MONTH" -> "MTH";
            default -> null;
        };
    }

    private boolean hasCourtDecisionInFavourOfClaimant(CaseData caseData) {
        ClaimantLiPResponse applicant1Response = Optional.ofNullable(caseData.getCaseDataLiP())
            .map(CaseDataLiP::getApplicant1LiPResponse)
            .orElse(null);
        return applicant1Response != null && applicant1Response.hasCourtDecisionInFavourOfClaimant();
    }
}
