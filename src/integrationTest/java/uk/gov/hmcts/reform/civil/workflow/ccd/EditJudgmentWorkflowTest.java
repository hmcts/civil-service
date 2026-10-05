package uk.gov.hmcts.reform.civil.workflow.ccd;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentType;
import uk.gov.hmcts.reform.civil.workflow.WorkflowIntegrationTest;
import uk.gov.hmcts.reform.civil.workflow.ccd.fixture.EditJudgmentFixtures;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.EDIT_JUDGMENT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_JUDGMENT_VARIED_DETERMINATION_OF_MEANS;

@SuppressWarnings({"java:S5960", "java:S6813"})
class EditJudgmentWorkflowTest extends WorkflowIntegrationTest {

    @Test
    void shouldPopulateFieldsFromActiveDefaultJudgmentOnAboutToStart() throws Exception {
        CaseData fixture = EditJudgmentFixtures.editDefaultJudgment();

        startWorkflow(fixture)
            .eventId(EDIT_JUDGMENT)
            .aboutToStart()
            .then(result -> {
                assertThat(result.response().getErrors()).isNullOrEmpty();

                CaseData updated = result.caseData();
                assertThat(updated.getJoOrderMadeDate()).isNotNull();
                assertThat(updated.getJoAmountOrdered()).isEqualTo("100000");
                assertThat(updated.getJoAmountCostOrdered()).isEqualTo("10200");
                assertThat(updated.getJoShowRegisteredWithRTLOption()).isEqualTo(YesOrNo.NO);
            });
    }

    @Test
    void shouldEditJudgmentSuccessfully() throws Exception {
        CaseData fixture = EditJudgmentFixtures.editDefaultJudgment();

        startWorkflow(fixture)
            .eventId(EDIT_JUDGMENT)
            .aboutToSubmit()
            .then(result -> {
                assertThat(result.response().getErrors()).isNullOrEmpty();

                CaseData updated = result.caseData();
                assertThat(updated.getActiveJudgment()).isNotNull();
                assertThat(updated.getActiveJudgment().getType()).isEqualTo(JudgmentType.DEFAULT_JUDGMENT);
                assertThat(updated.getJoRepaymentSummaryObject()).isNotNull();
            })
            .submitted()
            .then(result -> assertThat(result.submittedResponse()).isNotNull());
    }

    @Test
    void shouldTriggerVariedDeterminationNotificationWhenReasonIsDeterminationOfMeans() throws Exception {
        CaseData fixture = EditJudgmentFixtures.editJudgmentDeterminationOfMeans();

        startWorkflow(fixture)
            .eventId(EDIT_JUDGMENT)
            .aboutToSubmit()
            .then(result -> {
                assertThat(result.response().getErrors()).isNullOrEmpty();

                CaseData updated = result.caseData();
                assertThat(updated.getBusinessProcess()).isNotNull();
                assertThat(updated.getBusinessProcess().getCamundaEvent())
                    .isEqualTo(NOTIFY_JUDGMENT_VARIED_DETERMINATION_OF_MEANS.name());
            });
    }

    @Test
    void shouldReturnErrorWhenNoActiveJudgmentToEdit() throws Exception {
        CaseData fixture = EditJudgmentFixtures.editJudgmentNoActiveJudgment();

        startWorkflow(fixture)
            .eventId(EDIT_JUDGMENT)
            .aboutToSubmit()
            .then(result -> {
                assertThat(result.response().getErrors()).isNotEmpty();
                assertThat(result.response().getErrors())
                    .contains("There is no active judgment to edit");
            });
    }
}
