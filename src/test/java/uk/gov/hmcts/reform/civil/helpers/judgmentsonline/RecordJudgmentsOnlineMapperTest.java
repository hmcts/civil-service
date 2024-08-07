package uk.gov.hmcts.reform.civil.helpers.judgmentsonline;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentDetails;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentState;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentType;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class RecordJudgmentsOnlineMapperTest {

    private RecordJudgmentOnlineMapper judgmentOnlineMapper = new RecordJudgmentOnlineMapper();

    @Test
    void testIfActiveJudgmentIsAddedPayDate() {

        CaseData caseData = CaseDataBuilder.builder().buildJudgmentOnlineCaseDataWithPaymentByDate();
        JudgmentDetails activeJudgment = judgmentOnlineMapper.addUpdateActiveJudgment(caseData);

        assertNotNull(activeJudgment);
        assertEquals(JudgmentState.ISSUED, activeJudgment.getState());
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
        assertEquals("The Organisation", activeJudgment.getDefendant1Name());
        assertNotNull(activeJudgment.getDefendant1Address());
    }

    @Test
    void testIfActiveJudgmentIsAddedPayInstallments() {

        CaseData caseData = CaseDataBuilder.builder().buildJudmentOnlineCaseDataWithPaymentByInstalment();
        JudgmentDetails activeJudgment = judgmentOnlineMapper.addUpdateActiveJudgment(caseData);

        assertNotNull(activeJudgment);
        assertEquals(JudgmentState.ISSUED, activeJudgment.getState());
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
        assertEquals(caseData.getJoInstalmentDetails(), activeJudgment.getInstalmentDetails());
        assertEquals("Mr. John Rambo", activeJudgment.getDefendant1Name());
        assertNotNull(activeJudgment.getDefendant1Address());
        assertNotNull(activeJudgment.getDefendant1Dob());


    }

    @Test
    void testIfActiveJudgmentIsAddedPayImmediately() {

        CaseData caseData = CaseDataBuilder.builder().buildJudmentOnlineCaseDataWithPaymentImmediately();
        JudgmentDetails activeJudgment = judgmentOnlineMapper.addUpdateActiveJudgment(caseData);

        assertNotNull(activeJudgment);
        assertEquals(JudgmentState.ISSUED, activeJudgment.getState());
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
        assertEquals(caseData.getJoInstalmentDetails(), activeJudgment.getInstalmentDetails());
        assertEquals("Mr. Sole Trader", activeJudgment.getDefendant1Name());
        assertNotNull(activeJudgment.getDefendant1Address());
        assertNotNull(activeJudgment.getDefendant1Dob());
    }

    @Test
    void testIfActiveJudgmentIsAddedPayImmediately_Multi_party() {

        CaseData caseData = CaseDataBuilder.builder().buildJudgmentOnlineCaseDataWithPaymentByDate_Multi_party();
        JudgmentDetails activeJudgment = judgmentOnlineMapper.addUpdateActiveJudgment(caseData);

        assertNotNull(activeJudgment);
        assertEquals(JudgmentState.ISSUED, activeJudgment.getState());
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
        assertEquals("The Organisation", activeJudgment.getDefendant1Name());
        assertNotNull(activeJudgment.getDefendant1Address());
        assertEquals("Mr. John Rambo", activeJudgment.getDefendant2Name());
        assertNotNull(activeJudgment.getDefendant2Address());
        assertNotNull(activeJudgment.getDefendant2Dob());
    }
}
