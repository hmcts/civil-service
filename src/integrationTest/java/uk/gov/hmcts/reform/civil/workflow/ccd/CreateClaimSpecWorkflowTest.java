package uk.gov.hmcts.reform.civil.workflow.ccd;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.workflow.WorkflowIntegrationTest;
import uk.gov.hmcts.reform.civil.workflow.ccd.fixture.CreateClaimSpecFixtures;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("java:S5960")
class CreateClaimSpecWorkflowTest extends WorkflowIntegrationTest {

    @Test
    void shouldExecuteCreateClaimSpecAboutToStartThenEligibilityCheckWorkflow() throws Exception {
        startWorkflow(CreateClaimSpecFixtures.caseData())
            .eventId(CaseEvent.CREATE_CLAIM_SPEC)
            .aboutToStart()
            .then(result -> assertThat(result.response().getErrors()).isNullOrEmpty())
            .mid("eligibilityCheck")
            .then(result -> assertThat(result.response().getErrors()).isNullOrEmpty());
    }
}
