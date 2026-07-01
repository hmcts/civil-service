package uk.gov.hmcts.reform.civil.workflow.ccd;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.BusinessProcessStatus;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.workflow.WorkflowIntegrationTest;
import uk.gov.hmcts.reform.civil.workflow.ccd.fixture.ManageStayFixtures;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("java:S5960")
class ManageStayWorkflowTest extends WorkflowIntegrationTest {

    @Test
    void shouldLiftStayAndMoveCaseBackIntoWorkflowAtAboutToSubmit() throws Exception {
        startWorkflow(ManageStayFixtures.caseData())
            .eventId(CaseEvent.MANAGE_STAY)
            .aboutToSubmit()
            .then(result -> {
                assertThat(result.response().getErrors()).isNullOrEmpty();
                assertThat(result.response().getState()).isEqualTo(CaseState.CASE_PROGRESSION.name());
                assertThat(result.response().getData()).doesNotContainKeys(
                    "caseStayDate",
                    "manageStayUpdateRequestDate"
                );
                assertThat(result.caseData().getBusinessProcess())
                    .extracting("status", "camundaEvent")
                    .containsExactly(BusinessProcessStatus.READY, CaseEvent.STAY_LIFTED.name());
            });
    }

    @Test
    void shouldExposeSubmittedConfirmationAsSubmittedResponse() throws Exception {
        startWorkflow(ManageStayFixtures.caseData())
            .eventId(CaseEvent.MANAGE_STAY)
            .aboutToSubmit()
            .submitted()
            .then(result -> {
                assertThat(result.response()).isNull();
                assertThat(result.submittedResponse()).isNotNull();
                assertThat(result.submittedResponse().path("confirmation_header").asText())
                    .isEqualTo("# You have lifted the stay from this \n\n # case \n\n ## All parties have been notified");
                assertThat(result.submittedResponse().path("confirmation_body").asText()).isEqualTo("&nbsp;");
                assertThat(result.caseData().getBusinessProcess())
                    .extracting("status", "camundaEvent")
                    .containsExactly(BusinessProcessStatus.READY, CaseEvent.STAY_LIFTED.name());
            });
    }
}
