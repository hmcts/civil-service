package uk.gov.hmcts.reform.civil.workflow.ccd;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.workflow.WorkflowIntegrationTest;
import uk.gov.hmcts.reform.civil.workflow.ccd.fixture.JudgmentPaidInFullFixtures;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.JUDGMENT_PAID_IN_FULL;
import static uk.gov.hmcts.reform.civil.enums.BusinessProcessStatus.READY;

@SuppressWarnings({"java:S5960", "java:S6813"})
class JudgmentPaidInFullWorkflowTest extends WorkflowIntegrationTest {

    @Test
    void shouldMarkJudgmentAsPaidInFull() throws Exception {
        CaseData fixture = JudgmentPaidInFullFixtures.markPaidInFull();

        startWorkflow(fixture)
            .eventId(JUDGMENT_PAID_IN_FULL)
            .aboutToSubmit()
            .then(result -> {
                assertThat(result.response().getErrors()).isNullOrEmpty();

                CaseData updated = result.caseData();
                assertThat(updated.getJoIsLiveJudgmentExists()).isEqualTo(YesOrNo.YES);
                assertThat(updated.getActiveJudgment()).isNotNull();
                assertThat(updated.getActiveJudgment().getFullyPaymentMadeDate()).isNotNull();

                assertThat(updated.getBusinessProcess().getStatus()).isEqualTo(READY);
                assertThat(updated.getBusinessProcess().getCamundaEvent())
                    .isEqualTo(JUDGMENT_PAID_IN_FULL.name());
                assertThat(updated.getJoCoscRpaStatus()).isNotNull();
                assertThat(updated.getJoMarkedPaidInFullIssueDate()).isNotNull();
            })
            .submitted()
            .then(result -> assertThat(result.submittedResponse()).isNotNull());
    }

    @Test
    void shouldReturnErrorWhenPaymentDateIsInFuture() throws Exception {
        CaseData fixture = JudgmentPaidInFullFixtures.markPaidInFullWithFutureDate();

        startWorkflow(fixture)
            .eventId(JUDGMENT_PAID_IN_FULL)
            .mid("validate-payment-date")
            .then(result -> {
                assertThat(result.response().getErrors()).isNotEmpty();
                assertThat(result.response().getErrors())
                    .contains("Date must be in past");
            });
    }

    @Test
    void shouldReturnErrorWhenPaymentDateBeforeJudgmentDate() throws Exception {
        CaseData fixture = JudgmentPaidInFullFixtures.markPaidInFullBeforeJudgmentDate();

        startWorkflow(fixture)
            .eventId(JUDGMENT_PAID_IN_FULL)
            .mid("validate-payment-date")
            .then(result -> {
                assertThat(result.response().getErrors()).isNotEmpty();
                assertThat(result.response().getErrors())
                    .contains("Paid in full date must be on or after the date of the judgment");
            });
    }
}
