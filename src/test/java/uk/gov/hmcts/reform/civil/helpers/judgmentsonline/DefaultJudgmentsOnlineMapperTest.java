package uk.gov.hmcts.reform.civil.helpers.judgmentsonline;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.DJPaymentTypeSelection;
import uk.gov.hmcts.reform.civil.enums.RepaymentFrequencyDJ;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.CaseLocationCivil;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentDetails;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentRTLStatus;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentState;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentType;
import uk.gov.hmcts.reform.civil.model.judgmentonline.PaymentFrequency;
import uk.gov.hmcts.reform.civil.model.judgmentonline.PaymentPlanSelection;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.robotics.mapper.AddressLinesMapper;
import uk.gov.hmcts.reform.civil.service.robotics.mapper.RoboticsAddressMapper;
import uk.gov.hmcts.reform.civil.utils.InterestCalculator;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
class DefaultJudgmentsOnlineMapperTest {

    private InterestCalculator interestCalculator;
    private RoboticsAddressMapper addressMapper = new RoboticsAddressMapper(new AddressLinesMapper());
    private DefaultJudgmentOnlineMapper defaultJudgmentOnlineMapper;

    @BeforeEach
    public void setUp() {
        interestCalculator = new InterestCalculator();
        defaultJudgmentOnlineMapper = new DefaultJudgmentOnlineMapper(interestCalculator, addressMapper);
    }

    @Test
    void testIfDefaultJudgmentIsMarkedActive_1v1() {

        CaseData caseData = CaseDataBuilder.builder().getDefaultJudgment1v1Case();
        JudgmentDetails activeJudgment = defaultJudgmentOnlineMapper.addUpdateActiveJudgment(caseData);

        assertNotNull(activeJudgment);
        assertEquals(JudgmentState.ISSUED, activeJudgment.getState());
        assertEquals("100990", activeJudgment.getOrderedAmount());
        assertEquals("0", activeJudgment.getCosts());
        assertEquals("100990", activeJudgment.getTotalAmount());
        assertEquals(YesOrNo.YES, activeJudgment.getIsRegisterWithRTL());
        assertEquals(JudgmentRTLStatus.ISSUED.getRtlState(), activeJudgment.getRtlState());
        assertEquals(LocalDate.now(), activeJudgment.getIssueDate());
        assertEquals("0123", activeJudgment.getCourtLocation());
        assertEquals(JudgmentType.DEFAULT_JUDGMENT, activeJudgment.getType());
        assertEquals(YesOrNo.YES, activeJudgment.getIsJointJudgment());
        assertEquals(1, activeJudgment.getJudgmentId());
        assertEquals("Mr. Sole Trader", activeJudgment.getDefendant1Name());
        assertNotNull(activeJudgment.getDefendant1Address());
        assertNotNull(activeJudgment.getDefendant1Dob());
    }

    @Test
    void testIfDefaultJudgmentIsMarkedActive_1v2_Divergent() {

        CaseData caseData = CaseDataBuilder.builder().getDefaultJudgment1v2DivergentCase();
        JudgmentDetails activeJudgment = defaultJudgmentOnlineMapper.addUpdateActiveJudgment(caseData);

        assertNotNull(activeJudgment);
        assertEquals(JudgmentState.REQUESTED, activeJudgment.getState());
        assertEquals("100990", activeJudgment.getOrderedAmount());
        assertEquals("0", activeJudgment.getCosts());
        assertEquals("100990", activeJudgment.getTotalAmount());
        assertEquals(YesOrNo.NO, activeJudgment.getIsRegisterWithRTL());
        assertEquals(LocalDate.now(), activeJudgment.getIssueDate());
        assertEquals("0123", activeJudgment.getCourtLocation());
        assertEquals(JudgmentType.DEFAULT_JUDGMENT, activeJudgment.getType());
        assertEquals(YesOrNo.YES, activeJudgment.getIsJointJudgment());
        assertEquals(1, activeJudgment.getJudgmentId());
        assertEquals("Mr. John Rambo", activeJudgment.getDefendant1Name());
        assertEquals("Mr. John Rambo", activeJudgment.getDefendant2Name());
        assertNotNull(activeJudgment.getDefendant1Address());
        assertNotNull(activeJudgment.getDefendant2Address());
        assertNotNull(activeJudgment.getDefendant1Dob());
        assertNotNull(activeJudgment.getDefendant2Dob());

    }

    @Test
    void testIfDefaultJudgmentIsMarkedActive_1v1_scenario1() {

        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
            .respondent1ResponseDeadline(LocalDateTime.now().minusDays(15))
            .partialPayment(YesOrNo.YES)
            .partialPaymentAmount("10")
            .totalClaimAmount(BigDecimal.valueOf(1010))
            .paymentConfirmationDecisionSpec(YesOrNo.YES)
            .partialPayment(YesOrNo.YES)
            .caseManagementLocation(CaseLocationCivil.builder().baseLocation("0123").region("0321").build())
            .defendantDetailsSpec(DynamicList.builder()
                .value(DynamicListElement.builder()
                    .label("Test User")
                    .build())
                .build())
            .paymentTypeSelection(DJPaymentTypeSelection.REPAYMENT_PLAN)
            .repaymentFrequency(RepaymentFrequencyDJ.ONCE_ONE_WEEK)
            .repaymentSuggestion("100")
            .repaymentDate(LocalDate.now().plusDays(10))
            .build();

        JudgmentDetails activeJudgment = defaultJudgmentOnlineMapper.addUpdateActiveJudgment(caseData);
        assertNotNull(activeJudgment);
        assertEquals(JudgmentState.ISSUED, activeJudgment.getState());
        assertEquals("100990", activeJudgment.getOrderedAmount());
        assertEquals("0", activeJudgment.getCosts());
        assertEquals("100990", activeJudgment.getTotalAmount());
        assertEquals(YesOrNo.YES, activeJudgment.getIsRegisterWithRTL());
        assertEquals(LocalDate.now(), activeJudgment.getIssueDate());
        assertEquals("0123", activeJudgment.getCourtLocation());
        assertEquals(JudgmentType.DEFAULT_JUDGMENT, activeJudgment.getType());
        assertEquals(PaymentPlanSelection.PAY_IN_INSTALMENTS, activeJudgment.getPaymentPlan().getType());
        assertEquals(PaymentFrequency.WEEKLY, activeJudgment.getInstalmentDetails().getPaymentFrequency());
        assertEquals("100", activeJudgment.getInstalmentDetails().getAmount());
        assertEquals(YesOrNo.YES, activeJudgment.getIsJointJudgment());
        assertEquals(1, activeJudgment.getJudgmentId());
        assertEquals(activeJudgment.getDefendant1Name(), "Mr. Sole Trader");
        assertNotNull(activeJudgment.getDefendant1Address());
        assertNotNull(activeJudgment.getDefendant1Dob());

    }

    @Test
    void testIfDefaultJudgmentIsMarkedActive_1v1_scenario2() {

        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
            .respondent1ResponseDeadline(LocalDateTime.now().minusDays(15))
            .partialPayment(YesOrNo.YES)
            .partialPaymentAmount("10")
            .totalClaimAmount(BigDecimal.valueOf(1010))
            .paymentConfirmationDecisionSpec(YesOrNo.YES)
            .partialPayment(YesOrNo.YES)
            .caseManagementLocation(CaseLocationCivil.builder().baseLocation("0123").region("0321").build())
            .defendantDetailsSpec(DynamicList.builder()
                .value(DynamicListElement.builder()
                    .label("Test User")
                    .build())
                .build())
            .paymentTypeSelection(DJPaymentTypeSelection.SET_DATE)
            .paymentSetDate(LocalDate.now().plusDays(10))
            .build();

        JudgmentDetails activeJudgment = defaultJudgmentOnlineMapper.addUpdateActiveJudgment(caseData);
        assertNotNull(activeJudgment);
        assertEquals(JudgmentState.ISSUED, activeJudgment.getState());
        assertEquals("100990", activeJudgment.getOrderedAmount());
        assertEquals("0", activeJudgment.getCosts());
        assertEquals("100990", activeJudgment.getTotalAmount());
        assertEquals(YesOrNo.YES, activeJudgment.getIsRegisterWithRTL());
        assertEquals(LocalDate.now(), activeJudgment.getIssueDate());
        assertEquals("0123", activeJudgment.getCourtLocation());
        assertEquals(JudgmentType.DEFAULT_JUDGMENT, activeJudgment.getType());
        assertEquals(PaymentPlanSelection.PAY_BY_DATE, activeJudgment.getPaymentPlan().getType());
        assertEquals(LocalDate.now().plusDays(10), activeJudgment.getPaymentPlan().getPaymentDeadlineDate());
        assertEquals(YesOrNo.YES, activeJudgment.getIsJointJudgment());
        assertEquals(1, activeJudgment.getJudgmentId());
        assertEquals(activeJudgment.getDefendant1Name(), "Mr. Sole Trader");
        assertNotNull(activeJudgment.getDefendant1Address());
        assertNotNull(activeJudgment.getDefendant1Dob());

    }
}
