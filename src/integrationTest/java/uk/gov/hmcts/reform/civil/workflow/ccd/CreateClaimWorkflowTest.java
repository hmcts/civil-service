package uk.gov.hmcts.reform.civil.workflow.ccd;

import com.microsoft.applicationinsights.TelemetryClient;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.workflow.WorkflowIntegrationTest;
import uk.gov.hmcts.reform.civil.workflow.ccd.fixture.CreateClaimFixtures;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;

@SuppressWarnings("java:S5960")
class CreateClaimWorkflowTest extends WorkflowIntegrationTest {

    @MockBean
    private TelemetryClient telemetryClient;

    @Test
    void shouldExecuteCreateClaimAboutToStartThenStartClaimMidWorkflow() throws Exception {
        startWorkflow(CreateClaimFixtures.caseData())
            .eventId(CaseEvent.CREATE_CLAIM)
            .aboutToStart()
            .then(result -> assertThat(result.response().getErrors()).isNullOrEmpty())
            .mid("start-claim")
            .then(result -> {
                assertThat(result.response().getErrors()).isNullOrEmpty();
                assertThat(result.caseData().getClaimStarted()).isEqualTo(YES);
                assertThat(result.response().getData()).containsKey("claimStarted");
            });
    }
}
