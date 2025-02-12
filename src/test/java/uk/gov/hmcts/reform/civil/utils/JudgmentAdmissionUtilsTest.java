package uk.gov.hmcts.reform.civil.utils;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CCJPaymentDetails;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec.BY_SET_DATE;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;

public class JudgmentAdmissionUtilsTest {

    @Test
    public void shouldReturnTrue_whenFullAdmitRepaymentAcceptedWithCCJ() {
        CCJPaymentDetails ccjPaymentDetails = CCJPaymentDetails.builder()
                .ccjPaymentPaidSomeOption(YES)
                .ccjPaymentPaidSomeAmount(BigDecimal.valueOf(600.0))
                .ccjJudgmentLipInterest(BigDecimal.valueOf(300))
                .ccjJudgmentAmountClaimFee(BigDecimal.valueOf(0))
                .build();
        CaseData caseData = CaseData.builder()
                .respondent1Represented(YesOrNo.NO)
                .specRespondent1Represented(YesOrNo.NO)
                .applicant1Represented(YesOrNo.NO)
                .defenceAdmitPartPaymentTimeRouteRequired(BY_SET_DATE)
                .applicant1AcceptFullAdmitPaymentPlanSpec(YesOrNo.YES)
                .ccjPaymentDetails(ccjPaymentDetails)
                .build();

        boolean isCCJRequested = JudgmentAdmissionUtils.getLIPJudgmentAdmission(caseData);
        assertTrue(isCCJRequested);
    }

    @Test
    public void shouldReturnTrue_whenPartAdmitRepaymentAcceptedWithCCJ() {
        CCJPaymentDetails ccjPaymentDetails = CCJPaymentDetails.builder()
                .ccjPaymentPaidSomeOption(YES)
                .ccjPaymentPaidSomeAmount(BigDecimal.valueOf(600.0))
                .ccjJudgmentLipInterest(BigDecimal.valueOf(300))
                .ccjJudgmentAmountClaimFee(BigDecimal.valueOf(0))
                .build();
        CaseData caseData = CaseData.builder()
                .respondent1Represented(YesOrNo.NO)
                .specRespondent1Represented(YesOrNo.NO)
                .applicant1Represented(YesOrNo.NO)
                .defenceAdmitPartPaymentTimeRouteRequired(BY_SET_DATE)
                .ccjPaymentDetails(ccjPaymentDetails)
                .applicant1AcceptPartAdmitPaymentPlanSpec(YesOrNo.YES)
                .build();

        boolean isCCJRequested = JudgmentAdmissionUtils.getLIPJudgmentAdmission(caseData);
        assertTrue(isCCJRequested);
    }

    @Test
    public void shouldReturnFalse_whenPartAdmitRepaymentRejected() {
        CaseData caseData = CaseData.builder()
                .respondent1Represented(YesOrNo.NO)
                .specRespondent1Represented(YesOrNo.NO)
                .applicant1Represented(YesOrNo.NO)
                .defenceAdmitPartPaymentTimeRouteRequired(BY_SET_DATE)
                .applicant1AcceptPartAdmitPaymentPlanSpec(YesOrNo.NO)
                .build();

        boolean isCCJRequested = JudgmentAdmissionUtils.getLIPJudgmentAdmission(caseData);
        assertFalse(isCCJRequested);
    }
}
