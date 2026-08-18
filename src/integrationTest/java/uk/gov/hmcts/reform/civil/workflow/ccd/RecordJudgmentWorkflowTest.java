package uk.gov.hmcts.reform.civil.workflow.ccd;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentState;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentType;
import uk.gov.hmcts.reform.civil.model.judgmentonline.PaymentPlanSelection;
import uk.gov.hmcts.reform.civil.workflow.WorkflowIntegrationTest;
import uk.gov.hmcts.reform.civil.workflow.ccd.fixture.RecordJudgmentFixtures;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.RECORD_JUDGMENT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.RECORD_JUDGMENT_NOTIFICATION;

@SuppressWarnings({"java:S5960", "java:S6813"})
class RecordJudgmentWorkflowTest extends WorkflowIntegrationTest {

    @Test
    void shouldRecordJudgmentWithPayImmediately() throws Exception {
        CaseData fixture = RecordJudgmentFixtures.recordJudgmentPayImmediately();

        startWorkflow(fixture)
            .eventId(RECORD_JUDGMENT)
            .aboutToSubmit()
            .then(result -> {
                assertThat(result.response().getErrors()).isNullOrEmpty();

                CaseData updated = result.caseData();
                assertThat(updated.getJoIsLiveJudgmentExists()).isEqualTo(YesOrNo.YES);
                assertThat(updated.getActiveJudgment()).isNotNull();
                assertThat(updated.getActiveJudgment().getState()).isEqualTo(JudgmentState.ISSUED);
                assertThat(updated.getActiveJudgment().getType()).isEqualTo(JudgmentType.JUDGMENT_FOLLOWING_HEARING);
                assertThat(updated.getActiveJudgment().getPaymentPlan().getType())
                    .isEqualTo(PaymentPlanSelection.PAY_IMMEDIATELY);
                assertThat(updated.getActiveJudgment().getOrderedAmount()).isEqualTo("100000");
                assertThat(updated.getActiveJudgment().getCosts()).isEqualTo("10200");
                assertThat(updated.getActiveJudgment().getIssueDate()).isNotNull();
                assertThat(updated.getJoRepaymentSummaryObject()).isNotNull();
            })
            .submitted()
            .then(result -> assertThat(result.submittedResponse()).isNotNull());
    }

    @Test
    void shouldRecordJudgmentWithPayByDate() throws Exception {
        CaseData fixture = RecordJudgmentFixtures.recordJudgmentPayByDate();

        startWorkflow(fixture)
            .eventId(RECORD_JUDGMENT)
            .aboutToSubmit()
            .then(result -> {
                assertThat(result.response().getErrors()).isNullOrEmpty();

                CaseData updated = result.caseData();
                assertThat(updated.getActiveJudgment().getPaymentPlan().getType())
                    .isEqualTo(PaymentPlanSelection.PAY_BY_DATE);
                assertThat(updated.getActiveJudgment().getPaymentPlan().getPaymentDeadlineDate())
                    .isNotNull();
            });
    }

    @Test
    void shouldRecordJudgmentWithInstalments() throws Exception {
        CaseData fixture = RecordJudgmentFixtures.recordJudgmentPayByInstalments();

        startWorkflow(fixture)
            .eventId(RECORD_JUDGMENT)
            .aboutToSubmit()
            .then(result -> {
                assertThat(result.response().getErrors()).isNullOrEmpty();

                CaseData updated = result.caseData();
                assertThat(updated.getActiveJudgment().getPaymentPlan().getType())
                    .isEqualTo(PaymentPlanSelection.PAY_IN_INSTALMENTS);
                assertThat(updated.getActiveJudgment().getInstalmentDetails()).isNotNull();
                assertThat(updated.getActiveJudgment().getInstalmentDetails().getAmount())
                    .isEqualTo("25000");
            });
    }

    @Test
    void shouldTriggerNotificationForDeterminationOfMeans() throws Exception {
        CaseData fixture = RecordJudgmentFixtures.recordJudgmentDeterminationOfMeans();

        startWorkflow(fixture)
            .eventId(RECORD_JUDGMENT)
            .aboutToSubmit()
            .then(result -> {
                assertThat(result.response().getErrors()).isNullOrEmpty();

                CaseData updated = result.caseData();
                assertThat(updated.getBusinessProcess()).isNotNull();
                assertThat(updated.getBusinessProcess().getCamundaEvent())
                    .isEqualTo(RECORD_JUDGMENT_NOTIFICATION.name());
            });
    }

    @Test
    void shouldClearFieldsOnAboutToStartWhenLiveJudgmentExists() throws Exception {
        CaseData fixture = RecordJudgmentFixtures.recordJudgmentWithExistingLiveJudgment();

        startWorkflow(fixture)
            .eventId(RECORD_JUDGMENT)
            .aboutToStart()
            .then(result -> {
                assertThat(result.response().getErrors()).isNullOrEmpty();

                CaseData updated = result.caseData();
                assertThat(updated.getJoOrderMadeDate()).isNull();
                assertThat(updated.getJoPaymentPlan()).isNull();
                assertThat(updated.getJoInstalmentDetails()).isNull();
                assertThat(updated.getJoJudgmentRecordReason()).isNull();
                assertThat(updated.getJoAmountOrdered()).isNull();
                assertThat(updated.getJoAmountCostOrdered()).isNull();
                assertThat(updated.getJoIsRegisteredWithRTL()).isNull();
                assertThat(updated.getJoIssuedDate()).isNull();
            });
    }

    @Test
    void shouldReturnErrorOnMidWhenOrderDateIsInFuture() throws Exception {
        CaseData fixture = RecordJudgmentFixtures.recordJudgmentWithFutureOrderDate();

        startWorkflow(fixture)
            .eventId(RECORD_JUDGMENT)
            .mid("validateDates")
            .then(result -> {
                assertThat(result.response().getErrors()).isNotEmpty();
                assertThat(result.response().getErrors())
                    .contains("Date judge made the order must be in the past");
            });
    }
}
