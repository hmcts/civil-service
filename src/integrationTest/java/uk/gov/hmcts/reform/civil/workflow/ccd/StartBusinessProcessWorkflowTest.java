package uk.gov.hmcts.reform.civil.workflow.ccd;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.BusinessProcessStatus;
import uk.gov.hmcts.reform.civil.model.StateFlowDTO;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.flowstate.IStateFlowEngine;
import uk.gov.hmcts.reform.civil.stateflow.model.State;
import uk.gov.hmcts.reform.civil.workflow.WorkflowIntegrationTest;
import uk.gov.hmcts.reform.civil.workflow.ccd.fixture.StartBusinessProcessFixtures;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings({"java:S5960", "java:S6813"})
class StartBusinessProcessWorkflowTest extends WorkflowIntegrationTest {

    @Autowired
    private IStateFlowEngine stateFlowEngine;

    @MockBean
    private FeatureToggleService featureToggleService;

    @Test
    void shouldStartBusinessProcessAndResolveStateFlowForProceedsInHeritageSystemCaseData() throws Exception {
        startWorkflow(StartBusinessProcessFixtures.proceedsInHeritageSystemCaseData())
            .eventId(CaseEvent.START_BUSINESS_PROCESS)
            .aboutToSubmit()
            .then(result -> {
                assertThat(result.response().getErrors()).isNullOrEmpty();
                assertThat(result.caseData().getBusinessProcess())
                    .extracting("status", "camundaEvent")
                    .containsExactly(BusinessProcessStatus.STARTED, CaseEvent.START_BUSINESS_PROCESS.name());

                StateFlowDTO stateFlow = stateFlowEngine.getStateFlow(result.caseData());

                assertThat(stateFlow.getState().getName())
                    .as("state history: %s", stateFlow.getStateHistory())
                    .isNotEqualTo(State.ERROR_STATE);
            });
    }
}
