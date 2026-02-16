package uk.gov.hmcts.reform.civil.service.robotics.strategy;

import static java.math.BigDecimal.ZERO;
import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.civil.model.robotics.EventType.JUDGEMENT_BY_ADMISSION;
import static uk.gov.hmcts.reform.civil.model.robotics.EventType.MISCELLANEOUS;
import static uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsEventSupport.createEvent;
import static uk.gov.hmcts.reform.civil.service.robotics.utils.RoboticsDataUtil.APPLICANT_ID;
import static uk.gov.hmcts.reform.civil.service.robotics.utils.RoboticsDataUtil.RESPONDENT_ID;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import uk.gov.hmcts.reform.civil.service.robotics.mapper.ClaimFeeUtility;
import uk.gov.hmcts.reform.civil.service.robotics.mapper.EventHistoryMapper;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsEventTextFormatter;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsSequenceGenerator;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsTimelineHelper;
import uk.gov.hmcts.reform.civil.utils.MonetaryConversions;

@Slf4j
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
    public void contribute(EventHistory eventHistory, CaseData caseData, String authToken) {
        if (!supports(caseData)) {
            return;
        }
        log.info(
                "Building JudgmentByAdmissionStrategy events for caseId {}",
                caseData.getCcdCaseReference());

        List<Event> updatedMiscellaneousEvents1 =
                eventHistory.getMiscellaneous() == null
                        ? new ArrayList<>()
                        : new ArrayList<>(eventHistory.getMiscellaneous());
        updatedMiscellaneousEvents1.add(buildMiscellaneousEvent(eventHistory, caseData));
        eventHistory.setMiscellaneous(updatedMiscellaneousEvents1);
        List<Event> updatedJudgmentByAdmissionEvents2 =
                eventHistory.getJudgmentByAdmission() == null
                        ? new ArrayList<>()
                        : new ArrayList<>(eventHistory.getJudgmentByAdmission());
        updatedJudgmentByAdmissionEvents2.add(buildJudgmentByAdmissionEvent(eventHistory, caseData));
        eventHistory.setJudgmentByAdmission(updatedJudgmentByAdmissionEvents2);
    }

    private Event buildMiscellaneousEvent(EventHistory builder, CaseData caseData) {
        boolean joLiveFeedActive = featureToggleService.isJOLiveFeedActive();
        String miscText = textFormatter.judgmentByAdmissionOffline();
        String detailsText = miscText;
        if (joLiveFeedActive) {
            miscText = EventHistoryMapper.RECORD_JUDGMENT;
            detailsText = textFormatter.judgmentRecorded();
        }

        return createEvent(
                sequenceGenerator.nextSequence(builder),
                MISCELLANEOUS.getCode(),
                resolveJudgmentDate(caseData),
                null,
                detailsText,
                new EventDetails().setMiscText(miscText));
    }

    private Event buildJudgmentByAdmissionEvent(EventHistory builder, CaseData caseData) {
        boolean joLiveFeedActive = featureToggleService.isJOLiveFeedActive();
        return createEvent(
                sequenceGenerator.nextSequence(builder),
                JUDGEMENT_BY_ADMISSION.getCode(),
                resolveJudgmentDate(caseData),
                joLiveFeedActive ? RESPONDENT_ID : APPLICANT_ID,
                "",
                buildJudgmentDetails(caseData));
    }

    private EventDetails buildJudgmentDetails(CaseData caseData) {
        Optional<CCJPaymentDetails> ccjPaymentDetails = ofNullable(caseData.getCcjPaymentDetails());
        BigDecimal amountOfCosts = resolveAmountOfCosts(caseData, ccjPaymentDetails);

        return new EventDetails()
                .setAmountOfJudgment(resolveJudgmentAmount(caseData))
                .setAmountOfCosts(amountOfCosts)
                .setAmountPaidBeforeJudgment(
                        ccjPaymentDetails
                                .map(CCJPaymentDetails::getCcjPaymentPaidSomeAmountInPounds)
                                .map(amountPaid -> amountPaid.setScale(2))
                                .orElse(ZERO))
                .setIsJudgmentForthwith(
                        hasCourtDecisionInFavourOfClaimant(caseData)
                                ? caseData.applicant1SuggestedPayImmediately()
                                : caseData.isPayImmediately())
                .setPaymentInFullDate(resolvePaymentInFullDate(caseData))
                .setInstallmentAmount(resolveInstallmentAmount(caseData))
                .setInstallmentPeriod(resolveInstallmentPeriod(caseData))
                .setFirstInstallmentDate(resolveFirstInstallmentDate(caseData))
                .setDateOfJudgment(resolveJudgmentDate(caseData))
                .setJointJudgment(false)
                .setJudgmentToBeRegistered(true)
                .setMiscText("");
    }

    private BigDecimal resolveAmountOfCosts(
            CaseData caseData, Optional<CCJPaymentDetails> ccjPaymentDetails) {
        BigDecimal fixedCost =
                ccjPaymentDetails.map(CCJPaymentDetails::getCcjJudgmentFixedCostAmount).orElse(ZERO);
        BigDecimal claimFee =
                ccjPaymentDetails
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
        BigDecimal base =
                ccjPaymentDetails.map(CCJPaymentDetails::getCcjJudgmentAmountClaimAmount).orElse(ZERO);
        BigDecimal interest =
                caseData.isLipvLipOneVOne() && !caseData.isPartAdmitClaimSpec()
                        ? ccjPaymentDetails.map(CCJPaymentDetails::getCcjJudgmentLipInterest).orElse(ZERO)
                        : ofNullable(caseData.getTotalInterest()).orElse(ZERO);
        return base.add(interest).setScale(2);
    }

    private LocalDateTime resolveJudgmentDate(CaseData caseData) {
        return featureToggleService.isJOLiveFeedActive()
                ? caseData.getJoJudgementByAdmissionIssueDate()
                : resolveApplicant1ResponseDate(caseData);
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
        boolean claimantFavoursInstalments =
                hasCourtDecisionInFavourOfClaimant(caseData)
                        ? caseData.applicant1SuggestedPayByInstalments()
                        : caseData.isPayByInstallment();

        if (!claimantFavoursInstalments) {
            return null;
        }

        BigDecimal repaymentAmount =
                hasCourtDecisionInFavourOfClaimant(caseData)
                        ? caseData.getApplicant1SuggestInstalmentsPaymentAmountForDefendantSpec()
                        : ofNullable(caseData.getRespondent1RepaymentPlan())
                                .map(RepaymentPlanLRspec::getPaymentAmount)
                                .orElse(ZERO);

        return MonetaryConversions.penniesToPounds(
                ofNullable(repaymentAmount).map(amount -> amount.setScale(2)).orElse(ZERO));
    }

    private LocalDateTime resolveApplicant1ResponseDate(CaseData caseData) {
        LocalDateTime applicant1ResponseDate = caseData.getApplicant1ResponseDate();
        LocalDateTime now = LocalDateTime.now();
        if (applicant1ResponseDate == null || applicant1ResponseDate.isBefore(now)) {
            return now;
        }
        return applicant1ResponseDate;
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
        boolean payByInstallment =
                hasCourtDecisionInFavourOfClaimant(caseData)
                        ? caseData.applicant1SuggestedPayByInstalments()
                        : caseData.isPayByInstallment();
        if (payByInstallment) {
            return mapFrequency(caseData, hasCourtDecisionInFavourOfClaimant(caseData));
        }
        boolean payBySetDate =
                hasCourtDecisionInFavourOfClaimant(caseData)
                        ? caseData.applicant1SuggestedPayBySetDate()
                        : caseData.isPayBySetDate();
        boolean payImmediately =
                hasCourtDecisionInFavourOfClaimant(caseData)
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
            frequency =
                    ofNullable(caseData.getApplicant1SuggestInstalmentsRepaymentFrequencyForDefendantSpec())
                            .map(Enum::name)
                            .orElse("");
        } else {
            frequency =
                    ofNullable(caseData.getRespondent1RepaymentPlan())
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
        ClaimantLiPResponse applicant1Response =
                Optional.ofNullable(caseData.getCaseDataLiP())
                        .map(CaseDataLiP::getApplicant1LiPResponse)
                        .orElse(null);
        return applicant1Response != null && applicant1Response.hasCourtDecisionInFavourOfClaimant();
    }
}
