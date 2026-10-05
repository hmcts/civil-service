package uk.gov.hmcts.reform.civil.workflow.ccd;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentRTLStatus;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentState;
import uk.gov.hmcts.reform.civil.workflow.WorkflowIntegrationTest;
import uk.gov.hmcts.reform.civil.workflow.ccd.fixture.SetAsideJudgmentFixtures;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.SET_ASIDE_JUDGMENT;
import static uk.gov.hmcts.reform.civil.enums.BusinessProcessStatus.READY;

@SuppressWarnings({"java:S5960", "java:S6813"})
class SetAsideJudgmentWorkflowTest extends WorkflowIntegrationTest {

    @Test
    void shouldSetAsideJudgmentForJudgmentError() throws Exception {
        CaseData fixture = SetAsideJudgmentFixtures.setAsideJudgmentError();

        startWorkflow(fixture)
            .eventId(SET_ASIDE_JUDGMENT)
            .aboutToSubmit()
            .then(result -> {
                assertThat(result.response().getErrors()).isNullOrEmpty();

                CaseData updated = result.caseData();
                assertThat(updated.getJoIsLiveJudgmentExists()).isEqualTo(YesOrNo.NO);
                assertThat(updated.getHistoricJudgment()).isNotEmpty();
                assertThat(updated.getHistoricJudgment().get(0).getValue().getState())
                    .isEqualTo(JudgmentState.SET_ASIDE_ERROR);
                assertThat(updated.getHistoricJudgment().get(0).getValue().getRtlState())
                    .isEqualTo(JudgmentRTLStatus.CANCELLED.getRtlState());

                assertThat(updated.getBusinessProcess().getStatus()).isEqualTo(READY);
                assertThat(updated.getBusinessProcess().getCamundaEvent())
                    .isEqualTo(SET_ASIDE_JUDGMENT.name());
                assertThat(updated.getJoSetAsideCreatedDate()).isNotNull();
            })
            .submitted()
            .then(result -> assertThat(result.submittedResponse()).isNotNull());
    }

    @Test
    void shouldSetAsideJudgmentForJudgeOrderAfterApplication() throws Exception {
        CaseData fixture = SetAsideJudgmentFixtures.setAsideJudgeOrderAfterApplication();

        startWorkflow(fixture)
            .eventId(SET_ASIDE_JUDGMENT)
            .aboutToSubmit()
            .then(result -> {
                assertThat(result.response().getErrors()).isNullOrEmpty();

                CaseData updated = result.caseData();
                assertThat(updated.getJoIsLiveJudgmentExists()).isEqualTo(YesOrNo.NO);
                assertThat(updated.getHistoricJudgment()).isNotEmpty();
                assertThat(updated.getHistoricJudgment().get(0).getValue().getState())
                    .isEqualTo(JudgmentState.SET_ASIDE);
                assertThat(updated.getHistoricJudgment().get(0).getValue().getSetAsideDate())
                    .isNotNull();
                assertThat(updated.getHistoricJudgment().get(0).getValue().getSetAsideApplicationDate())
                    .isNotNull();

                assertThat(updated.getBusinessProcess().getCamundaEvent())
                    .isEqualTo(SET_ASIDE_JUDGMENT.name());
            });
    }

    @Test
    void shouldSetAsideJudgmentForJudgeOrderAfterDefence() throws Exception {
        CaseData fixture = SetAsideJudgmentFixtures.setAsideJudgeOrderAfterDefence();

        startWorkflow(fixture)
            .eventId(SET_ASIDE_JUDGMENT)
            .aboutToSubmit()
            .then(result -> {
                assertThat(result.response().getErrors()).isNullOrEmpty();

                CaseData updated = result.caseData();
                assertThat(updated.getHistoricJudgment()).isNotEmpty();
                assertThat(updated.getHistoricJudgment().get(0).getValue().getState())
                    .isEqualTo(JudgmentState.SET_ASIDE);
            });
    }

    @Test
    void shouldReturnErrorWhenOrderDateIsInFuture() throws Exception {
        CaseData fixture = SetAsideJudgmentFixtures.setAsideWithFutureOrderDate();

        startWorkflow(fixture)
            .eventId(SET_ASIDE_JUDGMENT)
            .mid("validate-set-aside-dates")
            .then(result -> {
                assertThat(result.response().getErrors()).isNotEmpty();
                assertThat(result.response().getErrors())
                    .contains("Date must be in the past");
            });
    }

    @Test
    void shouldReturnErrorWhenApplicationDateAfterOrderDate() throws Exception {
        CaseData fixture = SetAsideJudgmentFixtures.setAsideApplicationDateAfterOrderDate();

        startWorkflow(fixture)
            .eventId(SET_ASIDE_JUDGMENT)
            .mid("validate-set-aside-dates")
            .then(result -> {
                assertThat(result.response().getErrors()).isNotEmpty();
                assertThat(result.response().getErrors())
                    .contains("Application date to set aside judgment must be on or before the date of the order setting aside judgment");
            });
    }
}
