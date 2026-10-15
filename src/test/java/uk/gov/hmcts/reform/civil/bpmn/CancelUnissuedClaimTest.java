package uk.gov.hmcts.reform.civil.bpmn;

import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Map;

class CancelUnissuedClaimTest extends BpmnBaseTest {

    private static final String FILE_NAME = "cancel_unissued_claim.bpmn";
    private static final String MESSAGE_NAME = "CANCEL_UNISSUED_CLAIM";
    private static final String PROCESS_ID = "CANCEL_UNISSUED_CLAIM_PROCESS_ID";
    private static final String DASHBOARD_NOTIFICATION_ACTIVITY_ID = "GenerateDashboardNotificationsCancelUnissuedClaim";

    CancelUnissuedClaimTest() {
        super(FILE_NAME, PROCESS_ID);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldSuccessfullyCompleteCancelUnissuedClaim(boolean dashboardServiceEnabled) {
        assertProcessStartedWithMessage(MESSAGE_NAME, PROCESS_ID);
        VariableMap variables = Variables.createVariables();
        variables.put(FLOW_FLAGS, Map.of(DASHBOARD_SERVICE_ENABLED, dashboardServiceEnabled));
        startBusinessProcess(variables);
        if (dashboardServiceEnabled) {
            ExternalTask dashboardNotificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
            assertCompleteExternalTask(
                dashboardNotificationTask,
                PROCESS_CASE_EVENT,
                DASHBOARD_NOTIFICATION_EVENT,
                DASHBOARD_NOTIFICATION_ACTIVITY_ID,
                variables
            );
        }
        completeBusinessProcess(assertNextExternalTask(END_BUSINESS_PROCESS));
    }
}
