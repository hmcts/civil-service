package uk.gov.hmcts.reform.civil.helpers.judgmentsonline;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.PaymentFrequencyLRspec;
import uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CCJPaymentDetails;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.RepaymentPlanLRspec;
import uk.gov.hmcts.reform.civil.model.RespondToClaimAdmitPartLRspec;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.CaseLocationCivil;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentDetails;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentState;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentType;
import uk.gov.hmcts.reform.civil.model.judgmentonline.PaymentFrequency;
import uk.gov.hmcts.reform.civil.model.judgmentonline.PaymentPlanSelection;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;

@ExtendWith(MockitoExtension.class)
public class JudgmentByAdmissionMapperTest {

    @InjectMocks
    private JudgmentByAdmissionOnlineMapper judgmentByAdmissionOnlineMapper;


    @Test
    void testIfJudgmentByAdmission() {

        CCJPaymentDetails ccjPaymentDetails = CCJPaymentDetails.builder()
            .ccjPaymentPaidSomeOption(YesOrNo.YES)
            .ccjJudgmentFixedCostAmount(BigDecimal.valueOf(10))
            .ccjJudgmentTotalStillOwed(BigDecimal.valueOf(150))
            .build();

        CaseData caseData = CaseDataBuilder.builder().build().toBuilder()
            .respondent1Represented(YES)
            .specRespondent1Represented(YES)
            .applicant1Represented(YES)
            .defenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY)
            .defendantDetailsSpec(DynamicList.builder()
                                      .value(DynamicListElement.builder()
                                                 .label("John Doe")
                                                 .build())
                                      .build())
            .caseManagementLocation(CaseLocationCivil.builder().baseLocation("0123").region("0321").build())
            .ccjPaymentDetails(ccjPaymentDetails)
            .build();
        JudgmentDetails activeJudgment = judgmentByAdmissionOnlineMapper.addUpdateActiveJudgment(caseData);

        assertNotNull(activeJudgment);
        assertEquals(JudgmentState.ISSUED, activeJudgment.getState());
        assertEquals("140", activeJudgment.getOrderedAmount());
        assertEquals("10", activeJudgment.getCosts());
        assertEquals("150", activeJudgment.getTotalAmount());
        assertEquals(YesOrNo.YES, activeJudgment.getIsRegisterWithRTL());
        assertEquals(LocalDate.now(), activeJudgment.getIssueDate());
        assertEquals("0123", activeJudgment.getCourtLocation());
        assertEquals(JudgmentType.JUDGMENT_BY_ADMISSION, activeJudgment.getType());
        assertEquals(YesOrNo.YES, activeJudgment.getIsJointJudgment());
        assertEquals(1, activeJudgment.getJudgmentId());

    }

    @Test
    void testIfJudgmentByAdmission_scenario2() {

        CCJPaymentDetails ccjPaymentDetails = CCJPaymentDetails.builder()
            .ccjPaymentPaidSomeOption(YesOrNo.YES)
            .ccjJudgmentFixedCostAmount(BigDecimal.valueOf(10))
            .ccjJudgmentTotalStillOwed(BigDecimal.valueOf(150))
            .build();

        CaseData caseData = CaseDataBuilder.builder().build().toBuilder()
            .respondent1Represented(YES)
            .specRespondent1Represented(YES)
            .applicant1Represented(YES)
            .defenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.SUGGESTION_OF_REPAYMENT_PLAN)
            .defendantDetailsSpec(DynamicList.builder()
                                      .value(DynamicListElement.builder()
                                                 .label("John Doe")
                                                 .build())
                                      .build())
            .respondent1RepaymentPlan(RepaymentPlanLRspec.builder()
                                          .firstRepaymentDate(LocalDate.now().plusDays(10))
                                          .paymentAmount(new BigDecimal(1000))
                                          .repaymentFrequency(PaymentFrequencyLRspec.ONCE_ONE_WEEK)
                                          .build())
            .caseManagementLocation(CaseLocationCivil.builder().baseLocation("0123").region("0321").build())
            .ccjPaymentDetails(ccjPaymentDetails)
            .build();
        JudgmentDetails activeJudgment = judgmentByAdmissionOnlineMapper.addUpdateActiveJudgment(caseData);

        assertNotNull(activeJudgment);
        assertEquals(JudgmentState.ISSUED, activeJudgment.getState());
        assertEquals("140", activeJudgment.getOrderedAmount());
        assertEquals("10", activeJudgment.getCosts());
        assertEquals("150", activeJudgment.getTotalAmount());
        assertEquals(YesOrNo.YES, activeJudgment.getIsRegisterWithRTL());
        assertEquals(LocalDate.now(), activeJudgment.getIssueDate());
        assertEquals("0123", activeJudgment.getCourtLocation());
        assertEquals(JudgmentType.JUDGMENT_BY_ADMISSION, activeJudgment.getType());
        assertEquals(YesOrNo.YES, activeJudgment.getIsJointJudgment());
        assertEquals(1, activeJudgment.getJudgmentId());
        assertEquals(activeJudgment.getPaymentPlan().getType(),PaymentPlanSelection.PAY_IN_INSTALMENTS);
        assertEquals("1000", activeJudgment.getInstalmentDetails().getAmount());
        assertEquals(PaymentFrequency.WEEKLY, activeJudgment.getInstalmentDetails().getPaymentFrequency());
        assertEquals(LocalDate.now().plusDays(10), activeJudgment.getInstalmentDetails().getStartDate());

    }

    @Test
    void testIfJudgmentByAdmission_scenario3() {

        CCJPaymentDetails ccjPaymentDetails = CCJPaymentDetails.builder()
            .ccjPaymentPaidSomeOption(YesOrNo.YES)
            .ccjJudgmentFixedCostAmount(BigDecimal.valueOf(10))
            .ccjJudgmentTotalStillOwed(BigDecimal.valueOf(150))
            .build();

        CaseData caseData = CaseDataBuilder.builder().build().toBuilder()
            .respondent1Represented(YES)
            .specRespondent1Represented(YES)
            .applicant1Represented(YES)
            .defenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.BY_SET_DATE)
            .defendantDetailsSpec(DynamicList.builder()
                                      .value(DynamicListElement.builder()
                                                 .label("John Doe")
                                                 .build())
                                      .build())
            .respondToClaimAdmitPartLRspec(RespondToClaimAdmitPartLRspec.builder()
                                               .whenWillThisAmountBePaid(LocalDate.now().plusDays(5)).build())
            .caseManagementLocation(CaseLocationCivil.builder().baseLocation("0123").region("0321").build())
            .ccjPaymentDetails(ccjPaymentDetails)
            .build();
        JudgmentDetails activeJudgment = judgmentByAdmissionOnlineMapper.addUpdateActiveJudgment(caseData);

        assertNotNull(activeJudgment);
        assertEquals(JudgmentState.ISSUED, activeJudgment.getState());
        assertEquals("140", activeJudgment.getOrderedAmount());
        assertEquals("10", activeJudgment.getCosts());
        assertEquals("150", activeJudgment.getTotalAmount());
        assertEquals(YesOrNo.YES, activeJudgment.getIsRegisterWithRTL());
        assertEquals(LocalDate.now(), activeJudgment.getIssueDate());
        assertEquals("0123", activeJudgment.getCourtLocation());
        assertEquals(JudgmentType.JUDGMENT_BY_ADMISSION, activeJudgment.getType());
        assertEquals(YesOrNo.YES, activeJudgment.getIsJointJudgment());
        assertEquals(1, activeJudgment.getJudgmentId());
        assertEquals(activeJudgment.getPaymentPlan().getType(),PaymentPlanSelection.PAY_BY_DATE);
        assertEquals(activeJudgment.getPaymentPlan().getPaymentDeadlineDate(),LocalDate.now().plusDays(5));
        assertEquals(null, activeJudgment.getInstalmentDetails());
    }
}
