package uk.gov.hmcts.reform.civil.service.robotics.builders;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.RepaymentPlanLRspec;
import uk.gov.hmcts.reform.civil.model.robotics.Event;
import uk.gov.hmcts.reform.civil.model.robotics.EventDetails;
import uk.gov.hmcts.reform.civil.model.robotics.EventHistory;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.robotics.utils.EventHistoryUtil;

import java.util.Optional;

import static java.math.BigDecimal.ZERO;
import static uk.gov.hmcts.reform.civil.model.robotics.EventType.JUDGEMENT_BY_ADMISSION;
import static uk.gov.hmcts.reform.civil.model.robotics.EventType.MISCELLANEOUS;
import static uk.gov.hmcts.reform.civil.service.robotics.utils.EventHistoryUtil.RPA_REASON_JUDGMENT_BY_ADMISSION;
import static uk.gov.hmcts.reform.civil.service.robotics.utils.EventHistoryUtil.RPA_RECORD_JUDGMENT;
import static uk.gov.hmcts.reform.civil.service.robotics.utils.EventHistoryUtil.getInstallmentAmount;
import static uk.gov.hmcts.reform.civil.service.robotics.utils.EventHistoryUtil.prepareEventSequence;
import static uk.gov.hmcts.reform.civil.service.robotics.utils.EventHistoryUtil.setApplicant1ResponseDate;
import static uk.gov.hmcts.reform.civil.service.robotics.utils.RoboticsDataUtil.APPLICANT_ID;

@Slf4j
@Component
@RequiredArgsConstructor
public class CcjEventBuilder {

    private final FeatureToggleService featureToggleService;

    public void buildCcjEvent(EventHistory.EventHistoryBuilder builder, CaseData caseData) {
        if (caseData.isCcjRequestJudgmentByAdmission()) {
            String miscTextRequested = RPA_REASON_JUDGMENT_BY_ADMISSION;
            if (featureToggleService.isJOLiveFeedActive()) {
                miscTextRequested = RPA_RECORD_JUDGMENT;
            }
            log.info("Building event: {} for case id: {} ", "JUDGMENT_BY_ADMISSION", caseData.getCcdCaseReference());
            buildJudgmentByAdmissionEventDetails(builder, caseData);
            builder.miscellaneous((Event.builder()
                .eventSequence(prepareEventSequence(builder.build()))
                .eventCode(MISCELLANEOUS.getCode())
                .dateReceived(setApplicant1ResponseDate(caseData))
                .eventDetailsText(miscTextRequested)
                .eventDetails(EventDetails.builder()
                    .miscText(miscTextRequested)
                    .build())
                .build()));
        }
    }

    private void buildJudgmentByAdmissionEventDetails(EventHistory.EventHistoryBuilder builder, CaseData caseData) {
        boolean isResponsePayByInstallment = caseData.isPayByInstallment();
        Optional<RepaymentPlanLRspec> repaymentPlan = Optional.ofNullable(caseData.getRespondent1RepaymentPlan());
        EventDetails judgmentByAdmissionEvent = EventDetails.builder()
            .amountOfJudgment(caseData.getCcjPaymentDetails().getCcjJudgmentAmountClaimAmount()
                .add(caseData.isLipvLipOneVOne() && featureToggleService.isLipVLipEnabled()
                    ? caseData.getCcjPaymentDetails().getCcjJudgmentLipInterest() :
                    Optional.ofNullable(caseData.getTotalInterest()).orElse(ZERO))
                .setScale(2))
            .amountOfCosts(caseData.getCcjPaymentDetails().getCcjJudgmentFixedCostAmount()
                .add(caseData.getCcjPaymentDetails().getCcjJudgmentAmountClaimFee()).setScale(2))
            .amountPaidBeforeJudgment(caseData.getCcjPaymentDetails().getCcjPaymentPaidSomeAmountInPounds().setScale(2))
            .isJudgmentForthwith(caseData.isPayImmediately())
            .paymentInFullDate(caseData.isPayBySetDate()
                ? caseData.getRespondToClaimAdmitPartLRspec().getWhenWillThisAmountBePaid().atStartOfDay()
                : null)
            .installmentAmount(getInstallmentAmount(isResponsePayByInstallment, repaymentPlan))
            .installmentPeriod(isResponsePayByInstallment
                ? EventHistoryUtil.getInstallmentPeriodForRequestJudgmentByAdmission(repaymentPlan)
                : null)
            .firstInstallmentDate(EventHistoryUtil.getFirstInstallmentDate(isResponsePayByInstallment, repaymentPlan))
            .dateOfJudgment(setApplicant1ResponseDate(caseData))
            .jointJudgment(false)
            .judgmentToBeRegistered(true)
            .miscText("")
            .build();

        builder.judgmentByAdmission((Event.builder()
            .eventSequence(prepareEventSequence(builder.build()))
            .eventCode(JUDGEMENT_BY_ADMISSION.getCode())
            .litigiousPartyID(APPLICANT_ID)
            .dateReceived(setApplicant1ResponseDate(caseData))
            .eventDetails(judgmentByAdmissionEvent)
            .eventDetailsText("")
            .build()));
    }
}
