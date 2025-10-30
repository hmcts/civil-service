package uk.gov.hmcts.reform.civil.service.robotics.strategy;

import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.enums.DJPaymentTypeSelection;
import uk.gov.hmcts.reform.civil.helpers.judgmentsonline.JudgmentsOnlineHelper;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.PaymentBySetDate;
import uk.gov.hmcts.reform.civil.model.RepaymentPlanLRspec;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.ClaimantLiPResponse;
import uk.gov.hmcts.reform.civil.model.robotics.Event;
import uk.gov.hmcts.reform.civil.model.robotics.EventDetails;
import uk.gov.hmcts.reform.civil.model.robotics.EventHistory;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.robotics.mapper.ClaimFeeUtility;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsEventTextFormatter;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsPartyLookup;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsSequenceGenerator;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsTimelineHelper;
import uk.gov.hmcts.reform.civil.utils.MonetaryConversions;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

import static java.math.BigDecimal.ZERO;
import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.model.robotics.EventType.DEFAULT_JUDGMENT_GRANTED;
import static uk.gov.hmcts.reform.civil.model.robotics.EventType.MISCELLANEOUS;
import static uk.gov.hmcts.reform.civil.service.robotics.mapper.EventHistoryMapper.RECORD_JUDGMENT;

@Component
@Order(20)
@RequiredArgsConstructor
public class DefaultJudgmentEventStrategy implements EventHistoryStrategy {

    private final FeatureToggleService featureToggleService;
    private final RoboticsTimelineHelper timelineHelper;
    private final RoboticsSequenceGenerator sequenceGenerator;
    private final RoboticsPartyLookup partyLookup;
    private final RoboticsEventTextFormatter textFormatter;

    @Override
    public boolean supports(CaseData caseData) {
        return caseData != null && caseData.getDefendantDetailsSpec() != null;
    }

    @Override
    public void contribute(EventHistory.EventHistoryBuilder builder, CaseData caseData, String authToken) {
        if (!supports(caseData)) {
            return;
        }

        boolean grantedFlag = hasMultipleDefendants(caseData);
        addDefaultJudgmentEvents(builder, caseData, grantedFlag);
        addMiscellaneousEvent(builder, caseData, grantedFlag);
    }

    private void addDefaultJudgmentEvents(EventHistory.EventHistoryBuilder builder, CaseData caseData, boolean grantedFlag) {
        if (grantedFlag) {
            return;
        }

        builder.defaultJudgment(createDefaultJudgmentEvent(builder, caseData, partyLookup.respondentId(0)));
        if (caseData.getRespondent2() != null) {
            builder.defaultJudgment(createDefaultJudgmentEvent(builder, caseData, partyLookup.respondentId(1)));
        }
    }

    private void addMiscellaneousEvent(EventHistory.EventHistoryBuilder builder, CaseData caseData, boolean grantedFlag) {
        String requested = textFormatter.withRpaPrefix("Default Judgment requested and claim moved offline.");
        String granted = featureToggleService.isJOLiveFeedActive()
            ? RECORD_JUDGMENT
            : textFormatter.withRpaPrefix("Default Judgment granted and claim moved offline.");

        String text = grantedFlag ? requested : granted;
        Event event = Event.builder()
            .eventSequence(sequenceGenerator.nextSequence(builder.build()))
            .eventCode(MISCELLANEOUS.getCode())
            .dateReceived(getDateOfDjCreated(caseData))
            .eventDetailsText(text)
            .eventDetails(EventDetails.builder().miscText(text).build())
            .build();
        builder.miscellaneous(event);
    }

    private Event createDefaultJudgmentEvent(EventHistory.EventHistoryBuilder builder, CaseData caseData, String litigiousPartyId) {
        BigDecimal totalInterest = caseData.getTotalInterest() != null ? caseData.getTotalInterest() : ZERO;
        BigDecimal amountClaimedWithInterest = caseData.getTotalClaimAmount().add(totalInterest);

        BigDecimal partialPaymentPounds = Optional.ofNullable(caseData.getPartialPaymentAmount())
            .filter(value -> !value.isBlank())
            .map(BigDecimal::new)
            .map(MonetaryConversions::penniesToPounds)
            .orElse(ZERO);

        DJPaymentTypeSelection paymentType = caseData.getPaymentTypeSelection();
        boolean immediate = DJPaymentTypeSelection.IMMEDIATELY.equals(paymentType);
        boolean repaymentPlan = DJPaymentTypeSelection.REPAYMENT_PLAN.equals(paymentType);
        LocalDateTime dateOfDjCreated = getDateOfDjCreated(caseData);
        LocalDateTime paymentInFullDate = computePaymentInFullDate(caseData);

        BigDecimal amountOfCosts = (caseData.isApplicantLipOneVOne() && featureToggleService.isLipVLipEnabled())
            ? ClaimFeeUtility.getCourtFee(caseData)
            : JudgmentsOnlineHelper.getFixedCostsOfJudgmentForDJ(caseData)
            .add(JudgmentsOnlineHelper.getClaimFeeOfJudgmentForDJ(caseData));

        BigDecimal installmentAmount = repaymentPlan && caseData.getRepaymentSuggestion() != null
            ? MonetaryConversions.penniesToPounds(new BigDecimal(caseData.getRepaymentSuggestion())).setScale(2)
            : ZERO;

        boolean jointJudgment = caseData.getRespondent2() != null;

        Event event = Event.builder()
            .eventSequence(sequenceGenerator.nextSequence(builder.build()))
            .eventCode(DEFAULT_JUDGMENT_GRANTED.getCode())
            .dateReceived(dateOfDjCreated)
            .litigiousPartyID(litigiousPartyId)
            .eventDetailsText("")
            .eventDetails(EventDetails.builder()
                .miscText("")
                .amountOfJudgment(amountClaimedWithInterest.setScale(2))
                .amountOfCosts(amountOfCosts)
                .amountPaidBeforeJudgment(caseData.getPartialPayment() == YES ? partialPaymentPounds : ZERO)
                .isJudgmentForthwith(immediate)
                .paymentInFullDate(paymentInFullDate)
                .installmentAmount(installmentAmount)
                .installmentPeriod(getInstallmentPeriod(caseData))
                .firstInstallmentDate(caseData.getRepaymentDate())
                .dateOfJudgment(dateOfDjCreated)
                .jointJudgment(jointJudgment)
                .judgmentToBeRegistered(false)
                .build())
            .build();

        return event;
    }

    private boolean hasMultipleDefendants(CaseData caseData) {
        return caseData.getRespondent2() != null
            && caseData.getDefendantDetailsSpec() != null
            && !caseData.getDefendantDetailsSpec().getValue().getLabel().startsWith("Both");
    }

    private LocalDateTime computePaymentInFullDate(CaseData caseData) {
        DJPaymentTypeSelection paymentTypeSelection = caseData.getPaymentTypeSelection();
        boolean claimantFavouredImmediate = hasCourtDecisionInFavourOfClaimant(caseData)
            && caseData.applicant1SuggestedPayImmediately();

        if (paymentTypeSelection == DJPaymentTypeSelection.IMMEDIATELY) {
            return claimantFavouredImmediate
                ? Optional.ofNullable(caseData.getApplicant1SuggestPayImmediatelyPaymentDateForDefendantSpec())
                .map(LocalDate::atStartOfDay)
                .orElse(null)
                : timelineHelper.now();
        }

        if (paymentTypeSelection == DJPaymentTypeSelection.SET_DATE) {
            return claimantFavouredImmediate
                ? Optional.ofNullable(caseData.getApplicant1RequestedPaymentDateForDefendantSpec())
                .map(PaymentBySetDate::getPaymentSetDate)
                .map(LocalDate::atStartOfDay)
                .orElse(null)
                : caseData.getPaymentSetDate().atStartOfDay();
        }

        return null;
    }

    private BigDecimal getInstallmentAmount(CaseData caseData) {
        boolean payByInstallment = hasCourtDecisionInFavourOfClaimant(caseData)
            ? caseData.applicant1SuggestedPayByInstalments()
            : caseData.isPayByInstallment();

        if (!payByInstallment) {
            return null;
        }

        Optional<RepaymentPlanLRspec> plan = ofNullable(caseData.getRespondent1RepaymentPlan());
        BigDecimal amount = hasCourtDecisionInFavourOfClaimant(caseData)
            ? caseData.getApplicant1SuggestInstalmentsPaymentAmountForDefendantSpec()
            : plan.map(RepaymentPlanLRspec::getPaymentAmount).orElse(ZERO);

        return MonetaryConversions.penniesToPounds(
            Optional.ofNullable(amount)
                .map(value -> value.setScale(2))
                .orElse(ZERO)
        );
    }

    private String getInstallmentPeriod(CaseData data) {
        if (data.getPaymentTypeSelection() == DJPaymentTypeSelection.REPAYMENT_PLAN
            && data.getRepaymentFrequency() != null) {
            return switch (data.getRepaymentFrequency()) {
                case ONCE_ONE_WEEK -> "WK";
                case ONCE_TWO_WEEKS -> "FOR";
                case ONCE_ONE_MONTH -> "MTH";
                default -> null;
            };
        } else if (data.getPaymentTypeSelection() == DJPaymentTypeSelection.IMMEDIATELY) {
            return "FW";
        }
        return "FUL";
    }

    private boolean hasCourtDecisionInFavourOfClaimant(CaseData caseData) {
        ClaimantLiPResponse applicant1Response = Optional.ofNullable(caseData.getCaseDataLiP())
            .map(CaseDataLiP::getApplicant1LiPResponse)
            .orElse(null);
        return applicant1Response != null && applicant1Response.hasCourtDecisionInFavourOfClaimant();
    }

    private LocalDateTime getDateOfDjCreated(CaseData caseData) {
        return featureToggleService.isJOLiveFeedActive() && Objects.nonNull(caseData.getJoDJCreatedDate())
            ? caseData.getJoDJCreatedDate()
            : timelineHelper.now();
    }
}
