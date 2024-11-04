package uk.gov.hmcts.reform.civil.service.robotics.builders;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.enums.DJPaymentTypeSelection;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.helpers.judgmentsonline.JudgmentsOnlineHelper;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.robotics.Event;
import uk.gov.hmcts.reform.civil.model.robotics.EventDetails;
import uk.gov.hmcts.reform.civil.model.robotics.EventHistory;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.robotics.utils.EventHistoryUtil;
import uk.gov.hmcts.reform.civil.utils.MonetaryConversions;
import uk.gov.hmcts.reform.civil.utils.PredicateUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static java.math.BigDecimal.ZERO;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.civil.model.robotics.EventType.DEFAULT_JUDGMENT_GRANTED;
import static uk.gov.hmcts.reform.civil.service.robotics.utils.RoboticsDataUtil.RESPONDENT2_ID;
import static uk.gov.hmcts.reform.civil.service.robotics.utils.RoboticsDataUtil.RESPONDENT_ID;

@Slf4j
@Component
@RequiredArgsConstructor
public class DefaultJudgmentBuilder {

    private final FeatureToggleService featureToggleService;

    public void buildDefaultJudgment(EventHistory.EventHistoryBuilder builder, CaseData caseData) {
        List<Event> events = new ArrayList<>();
        boolean grantedFlag = PredicateUtils.grantedFlagDefendantSpecPredicate.test(caseData);

        if (!grantedFlag && null != caseData.getDefendantDetailsSpec()) {
            events.add(prepareDefaultJudgment(builder, caseData, RESPONDENT_ID));

            if (null != caseData.getRespondent2()) {
                events.add(prepareDefaultJudgment(builder, caseData, RESPONDENT2_ID));
            }
            builder.defaultJudgment(events);
        }
    }

    private Event prepareDefaultJudgment(EventHistory.EventHistoryBuilder builder, CaseData caseData,
                                         String litigiousPartyID) {

        BigDecimal claimInterest = caseData.getTotalInterest() != null
            ? caseData.getTotalInterest() : ZERO;
        BigDecimal amountClaimedWithInterest = caseData.getTotalClaimAmount().add(claimInterest);
        var partialPaymentPennies = isNotEmpty(caseData.getPartialPaymentAmount())
            ? new BigDecimal(caseData.getPartialPaymentAmount()) : null;
        var partialPaymentPounds = isNotEmpty(partialPaymentPennies)
            ? MonetaryConversions.penniesToPounds(partialPaymentPennies) : null;

        LocalDateTime paymentInFullDate;
        if (caseData.getPaymentTypeSelection().equals(DJPaymentTypeSelection.IMMEDIATELY)) {
            paymentInFullDate = LocalDateTime.now();
        } else if (caseData.getPaymentTypeSelection().equals(DJPaymentTypeSelection.SET_DATE)) {
            paymentInFullDate = caseData.getPaymentSetDate().atStartOfDay();
        } else {
            paymentInFullDate = null;
        }

        return Event.builder()
            .eventSequence(EventHistoryUtil.prepareEventSequence(builder.build()))
            .eventCode(DEFAULT_JUDGMENT_GRANTED.getCode())
            .dateReceived(LocalDateTime.now())
            .litigiousPartyID(litigiousPartyID)
            .eventDetailsText("")
            .eventDetails(EventDetails.builder()
                .miscText("")
                .amountOfJudgment(amountClaimedWithInterest.setScale(2))
                .amountOfCosts((caseData.isLipvLipOneVOne() && featureToggleService.isLipVLipEnabled())
                    ? MonetaryConversions.penniesToPounds(caseData.getClaimFee().getCalculatedAmountInPence())
                    : JudgmentsOnlineHelper.getCostOfJudgmentForDJ(caseData))
                .amountPaidBeforeJudgment((caseData.getPartialPayment() == YesOrNo.YES) ? partialPaymentPounds : ZERO)
                .isJudgmentForthwith(caseData.getPaymentTypeSelection().equals(DJPaymentTypeSelection.IMMEDIATELY))
                .paymentInFullDate(paymentInFullDate)
                .installmentAmount(caseData.getPaymentTypeSelection().equals(DJPaymentTypeSelection.REPAYMENT_PLAN)
                    ? EventHistoryUtil.getInstallmentAmount(caseData.getRepaymentSuggestion()).setScale(
                    2)
                    : ZERO)
                .installmentPeriod(EventHistoryUtil.getInstallmentPeriod(caseData))
                .firstInstallmentDate(caseData.getRepaymentDate())
                .dateOfJudgment(LocalDateTime.now())
                .jointJudgment(caseData.getRespondent2() != null)
                .judgmentToBeRegistered(false)
                .build())
            .build();
    }
}
