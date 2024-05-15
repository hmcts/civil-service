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
import static org.junit.jupiter.api.Assertions.assertNull;

public class EditJudgmentsOnlineMapperTest {

    private EditJudgmentOnlineMapper judgmentOnlineMapper = new EditJudgmentOnlineMapper();

    @Test
    void testIfActiveJudgmentIsnullIfnotSet() {

        CaseData caseData = CaseDataBuilder.builder().buildJudmentOnlineCaseDataWithPaymentImmediately();
        JudgmentDetails activeJudgment = judgmentOnlineMapper.addUpdateActiveJudgment(caseData);

        assertNull(activeJudgment);
    }

    @Test
    void testIfActiveJudgmentIsUpdated() {

        CaseData caseData = CaseDataBuilder.builder().buildJudmentOnlineCaseDataWithPaymentImmediately();
        RecordJudgmentOnlineMapper recordMapper = new RecordJudgmentOnlineMapper();
        caseData.setActiveJudgment(recordMapper.addUpdateActiveJudgment(caseData));

        JudgmentDetails activeJudgment = judgmentOnlineMapper.addUpdateActiveJudgment(caseData);

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
    }

    @Test
    void testIfActiveJudgmentIsUpdated_scenario2() {

        CaseData caseData = CaseDataBuilder.builder().buildJudmentOnlineCaseDataWithPaymentImmediately();
        RecordJudgmentOnlineMapper recordMapper = new RecordJudgmentOnlineMapper();
        caseData.setJoAmountCostOrdered(null);
        caseData.setActiveJudgment(recordMapper.addUpdateActiveJudgment(caseData));

        JudgmentDetails activeJudgment = judgmentOnlineMapper.addUpdateActiveJudgment(caseData);

        assertNotNull(activeJudgment);
        assertEquals(JudgmentState.MODIFIED, activeJudgment.getState());
        assertEquals("1200", activeJudgment.getOrderedAmount());
        assertEquals(null, activeJudgment.getCosts());
        assertEquals("1200", activeJudgment.getTotalAmount());
        assertEquals(YesOrNo.YES, activeJudgment.getIsRegisterWithRTL());
        assertEquals(LocalDate.of(2022, 12, 12), activeJudgment.getIssueDate());
        assertEquals("0123", activeJudgment.getCourtLocation());
        assertEquals(JudgmentType.JUDGMENT_FOLLOWING_HEARING, activeJudgment.getType());
        assertEquals(YesOrNo.YES, activeJudgment.getIsJointJudgment());
        assertEquals(1, activeJudgment.getJudgmentId());
        assertEquals(caseData.getJoPaymentPlan(), activeJudgment.getPaymentPlan());
    }
}
