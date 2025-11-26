package uk.gov.hmcts.reform.civil.service.robotics.strategy;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.enums.DJPaymentTypeSelection;
import uk.gov.hmcts.reform.civil.enums.RepaymentFrequencyDJ;
import uk.gov.hmcts.reform.civil.helpers.judgmentsonline.JudgmentsOnlineHelper;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.PaymentBySetDate;
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
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.model.robotics.EventType.DEFAULT_JUDGMENT_GRANTED;
import static uk.gov.hmcts.reform.civil.service.robotics.mapper.EventHistoryMapper.RECORD_JUDGMENT;
import static uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsEventSupport.buildMiscEvent;

@Slf4j
@Component
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
        log.info("Building default judgment robotics events for caseId {}", caseData.getCcdCaseReference());

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
        String requested = textFormatter.defaultJudgmentRequestedOffline();
        String granted = featureToggleService.isJOLiveFeedActive()
            ? RECORD_JUDGMENT
            : textFormatter.defaultJudgmentGrantedOffline();

        String text = grantedFlag ? requested : granted;
        builder.miscellaneous(buildMiscEvent(builder, sequenceGenerator, text, getDateOfDjCreated(caseData)));
    }

    private Event createDefaultJudgmentEvent(EventHistory.EventHistoryBuilder builder, CaseData caseData, String litigiousPartyId) {
        BigDecimal totalInterest = caseData.getTotalInterest() != null ? caseData.getTotalInterest() : ZERO;
        BigDecimal amountClaimedWithInterest = caseData.getTotalClaimAmount().add(totalInterest);

        BigDecimal partialPaymentPounds = Optional.ofNullable(caseData.getPartialPaymentAmount())
            .filter(value -> !value.isBlank())
            .map(BigDecimal::new)
            .map(MonetaryConversions::penniesToPounds)
            .orElse(null);

        DJPaymentTypeSelection paymentType = caseData.getPaymentTypeSelection();
        boolean immediate = DJPaymentTypeSelection.IMMEDIATELY.equals(paymentType);
        LocalDateTime dateOfDjCreated = getDateOfDjCreated(caseData);
        LocalDateTime paymentInFullDate = computePaymentInFullDate(caseData);

        BigDecimal amountOfCosts = (caseData.isApplicantLipOneVOne() && featureToggleService.isLipVLipEnabled())
            ? ClaimFeeUtility.getCourtFee(caseData)
            : JudgmentsOnlineHelper.getFixedCostsOfJudgmentForDJ(caseData)
            .add(JudgmentsOnlineHelper.getClaimFeeOfJudgmentForDJ(caseData));

        BigDecimal installmentAmount = DJPaymentTypeSelection.REPAYMENT_PLAN.equals(paymentType)
            ? getInstallmentAmountFromRepaymentSuggestion(caseData)
            : ZERO;

        boolean jointJudgment = caseData.getRespondent2() != null;

        return Event.builder()
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

    private BigDecimal getInstallmentAmountFromRepaymentSuggestion(CaseData caseData) {
        String suggestion = caseData.getRepaymentSuggestion();
        return MonetaryConversions.penniesToPounds(new BigDecimal(suggestion));
    }

    private String getInstallmentPeriod(CaseData data) {
        if (data.getPaymentTypeSelection() == DJPaymentTypeSelection.REPAYMENT_PLAN) {
            if (data.getRepaymentFrequency().equals(RepaymentFrequencyDJ.ONCE_ONE_WEEK)) {
                return "WK";
            } else if (data.getRepaymentFrequency().equals(RepaymentFrequencyDJ.ONCE_TWO_WEEKS)) {
                return "FOR";
            } else if (data.getRepaymentFrequency().equals(RepaymentFrequencyDJ.ONCE_ONE_MONTH)) {
                return "MTH";
            }

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
