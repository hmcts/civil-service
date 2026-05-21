package uk.gov.hmcts.reform.civil.workflow.ccd;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.workflow.WorkflowIntegrationTest;
import uk.gov.hmcts.reform.civil.workflow.ccd.fixture.NoticeOfChangeRequestFixtures;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("java:S5960")
class NoticeOfChangeRequestWorkflowTest extends WorkflowIntegrationTest {

    @Test
    void shouldAllowNoticeOfChangeForAValidCaseState() throws Exception {
        startWorkflow(NoticeOfChangeRequestFixtures.validCaseData())
            .eventId(CaseEvent.NOC_REQUEST)
            .aboutToSubmit()
            .then(result -> assertThat(result.response().getErrors()).isNullOrEmpty());
    }

    @Test
    void shouldRejectNoticeOfChangeForAnInvalidCaseState() throws Exception {
        startWorkflow(NoticeOfChangeRequestFixtures.invalidCaseData())
            .eventId(CaseEvent.NOC_REQUEST)
            .aboutToSubmit()
            .then(result -> assertThat(result.response().getErrors())
                .containsExactly("Invalid case state for NoC"));
    }
}
