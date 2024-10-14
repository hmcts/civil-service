package uk.gov.hmcts.reform.civil.service.robotics.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.enums.DJPaymentTypeSelection;
import uk.gov.hmcts.reform.civil.enums.PaymentFrequencyLRspec;
import uk.gov.hmcts.reform.civil.enums.RepaymentFrequencyDJ;
import uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.RepaymentPlanLRspec;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

class EventHistoryUtilTest {

    @Test
    void getInstallmentPeriod_Once_Per_Week() {
        LocalDate whenWillPay = LocalDate.now().plusDays(5);
        RepaymentPlanLRspec respondent1RepaymentPlan = RepaymentPlanLRspec.builder()
            .firstRepaymentDate(whenWillPay)
            .paymentAmount(BigDecimal.valueOf(10000))
            .repaymentFrequency(PaymentFrequencyLRspec.ONCE_ONE_WEEK)
            .build();

        CaseData caseData = CaseDataBuilder.builder()
            .setClaimTypeToSpecClaim()
            .atStateSpec1v1ClaimSubmitted()
            .atStateRespondent1v1FullAdmissionSpec().build().toBuilder()
            .defenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.SUGGESTION_OF_REPAYMENT_PLAN)
            .respondent1RepaymentPlan(respondent1RepaymentPlan)
            .paymentTypeSelection(DJPaymentTypeSelection.REPAYMENT_PLAN)
            .repaymentFrequency(RepaymentFrequencyDJ.ONCE_ONE_WEEK)
            .totalInterest(BigDecimal.ZERO)
            .build();

        String frequency = EventHistoryUtil.getInstallmentPeriod(caseData);
        Assertions.assertEquals("WK", frequency);
    }

    @Test
    void getInstallmentPeriod_Once_Two_Weeks() {
        LocalDate whenWillPay = LocalDate.now().plusDays(5);
        RepaymentPlanLRspec respondent1RepaymentPlan = RepaymentPlanLRspec.builder()
            .firstRepaymentDate(whenWillPay)
            .paymentAmount(BigDecimal.valueOf(10000))
            .repaymentFrequency(PaymentFrequencyLRspec.ONCE_TWO_WEEKS)
            .build();

        CaseData caseData = CaseDataBuilder.builder()
            .setClaimTypeToSpecClaim()
            .atStateSpec1v1ClaimSubmitted()
            .atStateRespondent1v1FullAdmissionSpec().build().toBuilder()
            .defenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.SUGGESTION_OF_REPAYMENT_PLAN)
            .respondent1RepaymentPlan(respondent1RepaymentPlan)
            .paymentTypeSelection(DJPaymentTypeSelection.REPAYMENT_PLAN)
            .repaymentFrequency(RepaymentFrequencyDJ.ONCE_TWO_WEEKS)
            .totalInterest(BigDecimal.ZERO)
            .build();

        String frequency = EventHistoryUtil.getInstallmentPeriod(caseData);
        Assertions.assertEquals("FOR", frequency);
    }

    @Test
    void getInstallmentPeriod_Once_One_Month() {
        LocalDate whenWillPay = LocalDate.now().plusDays(5);
        RepaymentPlanLRspec respondent1RepaymentPlan = RepaymentPlanLRspec.builder()
            .firstRepaymentDate(whenWillPay)
            .paymentAmount(BigDecimal.valueOf(10000))
            .repaymentFrequency(PaymentFrequencyLRspec.ONCE_ONE_MONTH)
            .build();

        CaseData caseData = CaseDataBuilder.builder()
            .setClaimTypeToSpecClaim()
            .atStateSpec1v1ClaimSubmitted()
            .atStateRespondent1v1FullAdmissionSpec().build().toBuilder()
            .defenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.SUGGESTION_OF_REPAYMENT_PLAN)
            .respondent1RepaymentPlan(respondent1RepaymentPlan)
            .paymentTypeSelection(DJPaymentTypeSelection.REPAYMENT_PLAN)
            .repaymentFrequency(RepaymentFrequencyDJ.ONCE_ONE_MONTH)
            .totalInterest(BigDecimal.ZERO)
            .build();

        String frequency = EventHistoryUtil.getInstallmentPeriod(caseData);
        Assertions.assertEquals("MTH", frequency);
    }

    @Test
    void getInstallmentPeriod_Default() {
        LocalDate whenWillPay = LocalDate.now().plusDays(5);
        RepaymentPlanLRspec respondent1RepaymentPlan = RepaymentPlanLRspec.builder()
            .firstRepaymentDate(whenWillPay)
            .paymentAmount(BigDecimal.valueOf(10000))
            .repaymentFrequency(PaymentFrequencyLRspec.ONCE_THREE_WEEKS)
            .build();

        CaseData caseData = CaseDataBuilder.builder()
            .setClaimTypeToSpecClaim()
            .atStateSpec1v1ClaimSubmitted()
            .atStateRespondent1v1FullAdmissionSpec().build().toBuilder()
            .defenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.SUGGESTION_OF_REPAYMENT_PLAN)
            .respondent1RepaymentPlan(respondent1RepaymentPlan)
            .paymentTypeSelection(DJPaymentTypeSelection.SET_DATE)
            .totalInterest(BigDecimal.ZERO)
            .build();

        String frequency = EventHistoryUtil.getInstallmentPeriod(caseData);
        Assertions.assertEquals("FUL", frequency);
    }

    @Test
    void getInstallmentPeriod_ImmediatePayment() {
        LocalDate whenWillPay = LocalDate.now().plusDays(5);
        RepaymentPlanLRspec respondent1RepaymentPlan = RepaymentPlanLRspec.builder()
            .firstRepaymentDate(whenWillPay)
            .paymentAmount(BigDecimal.valueOf(10000))
            .repaymentFrequency(PaymentFrequencyLRspec.ONCE_THREE_WEEKS)
            .build();

        CaseData caseData = CaseDataBuilder.builder()
            .setClaimTypeToSpecClaim()
            .atStateSpec1v1ClaimSubmitted()
            .atStateRespondent1v1FullAdmissionSpec().build().toBuilder()
            .defenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.SUGGESTION_OF_REPAYMENT_PLAN)
            .respondent1RepaymentPlan(respondent1RepaymentPlan)
            .paymentTypeSelection(DJPaymentTypeSelection.IMMEDIATELY)
            .totalInterest(BigDecimal.ZERO)
            .build();

        String frequency = EventHistoryUtil.getInstallmentPeriod(caseData);
        Assertions.assertEquals("FW", frequency);
    }

    @Test
    void getInstallmentPeriodForRequestJudgmentByAdmission_Once_One_Week() {
        LocalDate whenWillPay = LocalDate.now().plusDays(5);
        RepaymentPlanLRspec respondent1RepaymentPlan = RepaymentPlanLRspec.builder()
            .firstRepaymentDate(whenWillPay)
            .paymentAmount(BigDecimal.valueOf(10000))
            .repaymentFrequency(PaymentFrequencyLRspec.ONCE_ONE_WEEK)
            .build();

        String frequency = EventHistoryUtil.getInstallmentPeriodForRequestJudgmentByAdmission(Optional.of(respondent1RepaymentPlan));
        Assertions.assertEquals("WK", frequency);
    }

    @Test
    void getInstallmentPeriodForRequestJudgmentByAdmission_Once_Two_Weeks() {
        LocalDate whenWillPay = LocalDate.now().plusDays(5);
        RepaymentPlanLRspec respondent1RepaymentPlan = RepaymentPlanLRspec.builder()
            .firstRepaymentDate(whenWillPay)
            .paymentAmount(BigDecimal.valueOf(10000))
            .repaymentFrequency(PaymentFrequencyLRspec.ONCE_TWO_WEEKS)
            .build();

        String frequency = EventHistoryUtil.getInstallmentPeriodForRequestJudgmentByAdmission(Optional.of(respondent1RepaymentPlan));
        Assertions.assertEquals("FOR", frequency);
    }

    @Test
    void getInstallmentPeriodForRequestJudgmentByAdmission_Once_One_Month() {
        LocalDate whenWillPay = LocalDate.now().plusDays(5);
        RepaymentPlanLRspec respondent1RepaymentPlan = RepaymentPlanLRspec.builder()
            .firstRepaymentDate(whenWillPay)
            .paymentAmount(BigDecimal.valueOf(10000))
            .repaymentFrequency(PaymentFrequencyLRspec.ONCE_ONE_MONTH)
            .build();

        String frequency = EventHistoryUtil.getInstallmentPeriodForRequestJudgmentByAdmission(Optional.of(respondent1RepaymentPlan));
        Assertions.assertEquals("MTH", frequency);
    }

    @Test
    void getInstallmentPeriodForRequestJudgmentByAdmission_Default() {
        LocalDate whenWillPay = LocalDate.now().plusDays(5);
        RepaymentPlanLRspec respondent1RepaymentPlan = RepaymentPlanLRspec.builder()
            .firstRepaymentDate(whenWillPay)
            .paymentAmount(BigDecimal.valueOf(10000))
            .repaymentFrequency(PaymentFrequencyLRspec.ONCE_THREE_WEEKS)
            .build();

        String frequency = EventHistoryUtil.getInstallmentPeriodForRequestJudgmentByAdmission(Optional.of(respondent1RepaymentPlan));
        Assertions.assertNull(frequency);
    }
}
