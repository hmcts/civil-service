package uk.gov.hmcts.reform.civil.helpers.judgmentsonline;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentDetails;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentState;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentType;
import uk.gov.hmcts.reform.civil.model.judgmentonline.PaymentFrequency;
import uk.gov.hmcts.reform.civil.model.judgmentonline.PaymentPlanSelection;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.civil.utils.InterestCalculator;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class EditJudgmentsOnlineMapperTest {

    @InjectMocks
    private EditJudgmentOnlineMapper editJudgmentOnlineMapper;
    @InjectMocks
    private RecordJudgmentOnlineMapper recordJudgmentMapper;
    @MockBean
    private Time time;
    @Mock
    private InterestCalculator interestCalculator;
    @InjectMocks
    private DefaultJudgmentOnlineMapper defaultJudgmentMapper;

    @Test
    void testIfActiveJudgmentIsnullIfnotSet() {

        CaseData caseData = CaseDataBuilder.builder().buildJudmentOnlineCaseDataWithPaymentImmediately();
        JudgmentDetails activeJudgment = editJudgmentOnlineMapper.addUpdateActiveJudgment(caseData);

        assertNull(activeJudgment);
    }

    @Test
    void testIfActiveJudgmentIsUpdated() {

        CaseData caseData = CaseDataBuilder.builder().buildJudmentOnlineCaseDataWithPaymentImmediately();
        caseData.setActiveJudgment(recordJudgmentMapper.addUpdateActiveJudgment(caseData));

        JudgmentDetails activeJudgment = editJudgmentOnlineMapper.addUpdateActiveJudgment(caseData);

        assertNotNull(activeJudgment);
        assertEquals(JudgmentState.MODIFIED, activeJudgment.getState());
        assertEquals("1200", activeJudgment.getOrderedAmount());
        assertEquals("1100", activeJudgment.getCosts());
        assertEquals("2300", activeJudgment.getTotalAmount());
        assertEquals(YesOrNo.YES, activeJudgment.getIsRegisterWithRTL());
        assertEquals(LocalDate.of(2022, 12, 12), activeJudgment.getIssueDate());
        assertEquals("0123", activeJudgment.getCourtLocation());
        assertEquals(JudgmentType.JUDGMENT_FOLLOWING_HEARING, activeJudgment.getType());
        assertEquals(YesOrNo.YES, activeJudgment.getIsJointJudgment());
        assertEquals(1, activeJudgment.getJudgmentId());
        assertEquals(caseData.getJoPaymentPlan(), activeJudgment.getPaymentPlan());
        assertEquals("Mr. Sole Trader", activeJudgment.getDefendant1Name());
        assertNotNull(activeJudgment.getDefendant1Dob());
        assertNotNull(activeJudgment.getDefendant1Address());

        assertEquals("Mr. Sole Trader", caseData.getJoDefendantName1());
        assertEquals(PaymentPlanSelection.PAY_IMMEDIATELY, caseData.getJoPaymentPlanSelected());
    }

    @Test
    void testIfActiveJudgmentIsUpdated_scenario2() {

        CaseData caseData = CaseDataBuilder.builder().buildJudmentOnlineCaseDataWithPaymentImmediately();
        caseData.setJoAmountCostOrdered(null);
        caseData.setActiveJudgment(recordJudgmentMapper.addUpdateActiveJudgment(caseData));

        JudgmentDetails activeJudgment = editJudgmentOnlineMapper.addUpdateActiveJudgment(caseData);

        assertNotNull(activeJudgment);
        assertEquals(JudgmentState.MODIFIED, activeJudgment.getState());
        assertEquals("1200", activeJudgment.getOrderedAmount());
        assertEquals("0", activeJudgment.getCosts());
        assertEquals("1200", activeJudgment.getTotalAmount());
        assertEquals(YesOrNo.YES, activeJudgment.getIsRegisterWithRTL());
        assertEquals(LocalDate.of(2022, 12, 12), activeJudgment.getIssueDate());
        assertEquals("0123", activeJudgment.getCourtLocation());
        assertEquals(JudgmentType.JUDGMENT_FOLLOWING_HEARING, activeJudgment.getType());
        assertEquals(YesOrNo.YES, activeJudgment.getIsJointJudgment());
        assertEquals(1, activeJudgment.getJudgmentId());
        assertEquals(caseData.getJoPaymentPlan(), activeJudgment.getPaymentPlan());
        assertEquals("Mr. Sole Trader", activeJudgment.getDefendant1Name());
        assertNotNull(activeJudgment.getDefendant1Dob());
        assertNotNull(activeJudgment.getDefendant1Address());
    }

    @Test
    void testIfActiveJudgmentIsUpdated_PayInstallments() {

        CaseData caseData = CaseDataBuilder.builder().buildJudmentOnlineCaseDataWithPaymentByInstalment();
        caseData.setJoAmountCostOrdered(null);
        caseData.setActiveJudgment(recordJudgmentMapper.addUpdateActiveJudgment(caseData));

        JudgmentDetails activeJudgment = editJudgmentOnlineMapper.addUpdateActiveJudgment(caseData);

        assertNotNull(activeJudgment);
        assertEquals(JudgmentState.MODIFIED, activeJudgment.getState());
        assertEquals("1200", activeJudgment.getOrderedAmount());
        assertEquals("0", activeJudgment.getCosts());
        assertEquals("1200", activeJudgment.getTotalAmount());
        assertEquals(YesOrNo.YES, activeJudgment.getIsRegisterWithRTL());
        assertEquals(LocalDate.of(2022, 12, 12), activeJudgment.getIssueDate());
        assertEquals("0123", activeJudgment.getCourtLocation());
        assertEquals(JudgmentType.JUDGMENT_FOLLOWING_HEARING, activeJudgment.getType());
        assertEquals(YesOrNo.YES, activeJudgment.getIsJointJudgment());
        assertEquals(1, activeJudgment.getJudgmentId());
        assertEquals(caseData.getJoPaymentPlan(), activeJudgment.getPaymentPlan());
        assertEquals("Mr. John Rambo", activeJudgment.getDefendant1Name());
        assertNotNull(activeJudgment.getDefendant1Dob());
        assertNotNull(activeJudgment.getDefendant1Address());

        assertEquals("Mr. John Rambo", caseData.getJoDefendantName1());
        assertEquals(PaymentPlanSelection.PAY_IN_INSTALMENTS, caseData.getJoPaymentPlanSelected());
        assertEquals("120", caseData.getJoRepaymentAmount());
        assertNotNull(caseData.getJoRepaymentStartDate());
        assertEquals(PaymentFrequency.MONTHLY, caseData.getJoRepaymentFrequency());
    }

    @Test
    void testIfActiveJudgmentIsUpdated_PayByDate() {

        CaseData caseData = CaseDataBuilder.builder().buildJudgmentOnlineCaseDataWithPaymentByDate();
        caseData.setJoAmountCostOrdered(null);
        caseData.setActiveJudgment(recordJudgmentMapper.addUpdateActiveJudgment(caseData));

        JudgmentDetails activeJudgment = editJudgmentOnlineMapper.addUpdateActiveJudgment(caseData);

        assertNotNull(activeJudgment);
        assertEquals(JudgmentState.MODIFIED, activeJudgment.getState());
        assertEquals("1200", activeJudgment.getOrderedAmount());
        assertEquals("0", activeJudgment.getCosts());
        assertEquals("1200", activeJudgment.getTotalAmount());
        assertEquals(YesOrNo.YES, activeJudgment.getIsRegisterWithRTL());
        assertEquals(LocalDate.of(2022, 12, 12), activeJudgment.getIssueDate());
        assertEquals("0123", activeJudgment.getCourtLocation());
        assertEquals(JudgmentType.JUDGMENT_FOLLOWING_HEARING, activeJudgment.getType());
        assertEquals(YesOrNo.YES, activeJudgment.getIsJointJudgment());
        assertEquals(1, activeJudgment.getJudgmentId());
        assertEquals(caseData.getJoPaymentPlan(), activeJudgment.getPaymentPlan());
        assertEquals("The Organisation", activeJudgment.getDefendant1Name());
        assertNotNull(activeJudgment.getDefendant1Address());

        assertEquals("The Organisation", caseData.getJoDefendantName1());
        assertEquals(PaymentPlanSelection.PAY_BY_DATE, caseData.getJoPaymentPlanSelected());
    }

    @Test
    void testIfDefaultActiveJudgmentIsUpdated_scenario2() {

        when(interestCalculator.calculateInterest(any(CaseData.class))).thenReturn(BigDecimal.ZERO);
        CaseData caseData = CaseDataBuilder.builder().getDefaultJudgment1v1Case();
        caseData.setJoAmountCostOrdered(null);
        caseData.setActiveJudgment(defaultJudgmentMapper.addUpdateActiveJudgment(caseData));

        caseData.setJoOrderMadeDate(caseData.getActiveJudgment().getIssueDate());
        caseData.setJoPaymentPlan(caseData.getActiveJudgment().getPaymentPlan());
        caseData.setJoInstalmentDetails(caseData.getActiveJudgment().getInstalmentDetails());
        caseData.setJoJudgmentRecordReason(null);
        caseData.setJoAmountOrdered(caseData.getActiveJudgment().getOrderedAmount());
        caseData.setJoAmountCostOrdered(caseData.getActiveJudgment().getCosts());
        caseData.setJoIsRegisteredWithRTL(caseData.getActiveJudgment().getIsRegisterWithRTL());
        caseData.setJoIssuedDate(caseData.getActiveJudgment().getIssueDate());

        JudgmentDetails activeJudgment = editJudgmentOnlineMapper.addUpdateActiveJudgment(caseData);

        assertNotNull(activeJudgment);
        assertEquals(JudgmentState.MODIFIED, activeJudgment.getState());
        assertEquals("100990", activeJudgment.getOrderedAmount());
        assertEquals("0", activeJudgment.getCosts());
        assertEquals("100990", activeJudgment.getTotalAmount());
        assertEquals(YesOrNo.YES, activeJudgment.getIsRegisterWithRTL());
        assertEquals(LocalDate.now(), activeJudgment.getIssueDate());
        assertEquals("0123", activeJudgment.getCourtLocation());
        assertEquals(JudgmentType.DEFAULT_JUDGMENT, activeJudgment.getType());
        assertEquals(YesOrNo.YES, activeJudgment.getIsJointJudgment());
        assertEquals(1, activeJudgment.getJudgmentId());
        assertEquals("Mr. Sole Trader", activeJudgment.getDefendant1Name());
        assertNotNull(activeJudgment.getDefendant1Dob());
        assertNotNull(activeJudgment.getDefendant1Address());
    }

}
