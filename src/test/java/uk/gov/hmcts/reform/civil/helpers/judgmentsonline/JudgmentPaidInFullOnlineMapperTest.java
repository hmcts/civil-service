package uk.gov.hmcts.reform.civil.helpers.judgmentsonline;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentDetails;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentPaidInFull;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentPaymentPlan;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentState;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.reform.civil.model.judgmentonline.PaymentPlanSelection.PAY_IMMEDIATELY;

class JudgmentPaidInFullOnlineMapperTest {

    private JudgmentPaidInFullOnlineMapper judgmentPaidInFullOnlineMapper;

    @BeforeEach
    void setUp() {
        judgmentPaidInFullOnlineMapper = new JudgmentPaidInFullOnlineMapper();
    }

    @Test
    void shouldUpdateJudgmentToSatisfied_whenPaymentDateProvided() {
        LocalDate paymentDate = LocalDate.now();
        LocalDate issueDate = LocalDate.now().minusDays(40);

        JudgmentDetails activeJudgment = JudgmentDetails.builder()
            .issueDate(issueDate)
            .paymentPlan(JudgmentPaymentPlan.builder().type(PAY_IMMEDIATELY).build())
            .build();

        CaseData caseData = CaseData.builder().activeJudgment(activeJudgment).build();

        JudgmentDetails updatedJudgment = judgmentPaidInFullOnlineMapper.addUpdateActiveJudgment(caseData, paymentDate);

        assertEquals(JudgmentState.SATISFIED, updatedJudgment.getState());
        assertEquals(paymentDate, updatedJudgment.getFullyPaymentMadeDate());
        assertEquals(LocalDateTime.now().getDayOfMonth(), updatedJudgment.getLastUpdateTimeStamp().getDayOfMonth());
    }

    @Test
    void shouldUpdateJudgmentToSatisfied_whenNoPaymentDateProvided() {
        LocalDate defaultPaymentDate = LocalDate.now();
        LocalDate issueDate = LocalDate.now().minusDays(40);

        JudgmentDetails activeJudgment = JudgmentDetails.builder()
            .issueDate(issueDate)
            .paymentPlan(JudgmentPaymentPlan.builder().type(PAY_IMMEDIATELY).build())
            .build();

        CaseData caseData = CaseData.builder()
            .activeJudgment(activeJudgment)
            .joJudgmentPaidInFull(JudgmentPaidInFull.builder().dateOfFullPaymentMade(defaultPaymentDate).build())
            .build();

        JudgmentDetails updatedJudgment = judgmentPaidInFullOnlineMapper.addUpdateActiveJudgment(caseData);

        assertEquals(JudgmentState.SATISFIED, updatedJudgment.getState());
        assertEquals(defaultPaymentDate, updatedJudgment.getFullyPaymentMadeDate());
        assertEquals(LocalDateTime.now().getDayOfMonth(), updatedJudgment.getLastUpdateTimeStamp().getDayOfMonth());
    }

    @Test
    void shouldSetJudgmentToCancelled_whenPaidWithin31Days() {
        LocalDate paymentDate = LocalDate.now().minusDays(20);
        LocalDate issueDate = LocalDate.now().minusDays(40);

        JudgmentDetails activeJudgment = JudgmentDetails.builder()
            .issueDate(issueDate)
            .paymentPlan(JudgmentPaymentPlan.builder().type(PAY_IMMEDIATELY).build())
            .build();

        CaseData caseData = CaseData.builder().activeJudgment(activeJudgment).build();

        JudgmentDetails updatedJudgment = judgmentPaidInFullOnlineMapper.addUpdateActiveJudgment(caseData, paymentDate);

        assertEquals(JudgmentState.CANCELLED, updatedJudgment.getState());
        assertEquals(paymentDate, updatedJudgment.getFullyPaymentMadeDate());
        assertEquals(LocalDate.now(), updatedJudgment.getCancelDate());
        assertEquals(LocalDateTime.now().getDayOfMonth(), updatedJudgment.getCancelledTimeStamp().getDayOfMonth());
    }

    @Test
    void shouldUpdateCancelDateAndCancelledTimeStamp_whenJudgmentCancelled() {
        LocalDate paymentDate = LocalDate.now().minusDays(20);
        LocalDate issueDate = LocalDate.now().minusDays(40);

        JudgmentDetails activeJudgment = JudgmentDetails.builder()
            .issueDate(issueDate)
            .paymentPlan(JudgmentPaymentPlan.builder().type(PAY_IMMEDIATELY).build())
            .build();

        CaseData caseData = CaseData.builder().activeJudgment(activeJudgment).build();

        JudgmentDetails updatedJudgment = judgmentPaidInFullOnlineMapper.addUpdateActiveJudgment(caseData, paymentDate);

        assertEquals(JudgmentState.CANCELLED, updatedJudgment.getState());
        assertEquals(LocalDate.now(), updatedJudgment.getCancelDate());
        assertEquals(LocalDateTime.now().getDayOfMonth(), updatedJudgment.getCancelledTimeStamp().getDayOfMonth());
    }

    @Test
    void shouldUpdateJudgmentToSatisfied_whenPaidAfter31Days() {
        LocalDate paymentDate = LocalDate.now().minusDays(35);
        LocalDate issueDate = LocalDate.now().minusDays(70);

        JudgmentDetails activeJudgment = JudgmentDetails.builder()
            .issueDate(issueDate)
            .paymentPlan(JudgmentPaymentPlan.builder().type(PAY_IMMEDIATELY).build())
            .build();

        CaseData caseData = CaseData.builder().activeJudgment(activeJudgment).build();

        JudgmentDetails updatedJudgment = judgmentPaidInFullOnlineMapper.addUpdateActiveJudgment(caseData, paymentDate);

        assertEquals(JudgmentState.SATISFIED, updatedJudgment.getState());
    }

    @Test
    void shouldSetJudgmentToCancelled_whenPaymentDateIsBeforeIssueDate() {
        LocalDate paymentDate = LocalDate.now().minusDays(80);
        LocalDate issueDate = LocalDate.now().minusDays(40);

        JudgmentDetails activeJudgment = JudgmentDetails.builder()
            .issueDate(issueDate)
            .paymentPlan(JudgmentPaymentPlan.builder().type(PAY_IMMEDIATELY).build())
            .build();

        CaseData caseData = CaseData.builder().activeJudgment(activeJudgment).build();

        JudgmentDetails updatedJudgment = judgmentPaidInFullOnlineMapper.addUpdateActiveJudgment(caseData, paymentDate);

        assertEquals(JudgmentState.CANCELLED, updatedJudgment.getState());
    }
}
