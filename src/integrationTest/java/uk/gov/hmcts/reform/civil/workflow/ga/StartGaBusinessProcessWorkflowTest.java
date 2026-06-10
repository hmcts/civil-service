package uk.gov.hmcts.reform.civil.workflow.ga;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.BusinessProcessStatus;
import uk.gov.hmcts.reform.civil.workflow.ga.fixture.StartGaBusinessProcessFixtures;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("java:S5960")
class StartGaBusinessProcessWorkflowTest extends GAWorkflowIntegrationTest {

    @Test
    void shouldStartGaBusinessProcessAtAboutToSubmit() throws Exception {
        startWorkflow(StartGaBusinessProcessFixtures.caseData())
            .eventId(CaseEvent.START_GA_BUSINESS_PROCESS)
            .aboutToSubmit()
            .then(result -> {
                assertThat(result.response().getErrors()).isNullOrEmpty();
                assertThat(result.caseData().getBusinessProcess())
                    .extracting("status", "camundaEvent")
                    .containsExactly(BusinessProcessStatus.STARTED, CaseEvent.START_GA_BUSINESS_PROCESS.name());
            });
    }
}
