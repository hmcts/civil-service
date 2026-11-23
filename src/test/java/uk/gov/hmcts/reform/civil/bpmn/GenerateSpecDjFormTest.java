package uk.gov.hmcts.reform.civil.bpmn;

import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskIds;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

class GenerateSpecDjFormTest extends BpmnBaseTest {

    private static final String MESSAGE_NAME = "DEFAULT_JUDGEMENT_SPEC";
    private static final String PROCESS_ID = "GENERATE_DJ_FORM_SPEC";

    private static final String GENERATE_DJ_FORM_SPEC_EVENT = "GENERATE_DJ_FORM_SPEC";
    private static final String GENERATE_DJ_FORM_SPEC_ACTIVITY_ID = "GenerateDJFormSpec";
    private static final String NOTIFY_EVENT = "NOTIFY_EVENT";
    private static final String NOTIFY_EVENT_ACTIVITY_ID = "GenerateSpecDJFormNotifier";
    private static final String NOTIFY_RPA_EVENT = "NOTIFY_RPA_DJ_SPEC";
    private static final String NOTIFY_RPA_ACTIVITY_ID = "NotifyRPADJSPEC";

    GenerateSpecDjFormTest() {
        super("generate_spec_DJ_form.bpmn", PROCESS_ID);
    }

    @Test
    void shouldSuccessfullyComplete_whenDashboardDisabled() {
        VariableMap variables = flowFlags(false, false, false);

        startAndAssertProcess();
        startBusinessProcess(variables);
        completeDocGeneration(variables);
        completePrimaryNotifications(variables);
        assertProcessClosed();
    }

    @Test
    void shouldSuccessfullyComplete_whenDashboardEnabledAndClaimantRepresented() {
        VariableMap variables = flowFlags(true, false, false);

        startAndAssertProcess();
        startBusinessProcess(variables);
        completeDocGeneration(variables);
        completePrimaryNotifications(variables);

        ExternalTask dashboard = assertNextExternalTask(DASHBOARD_NOTIFICATION_TOPIC);
        assertCompleteExternalTask(dashboard, DASHBOARD_NOTIFICATION_TOPIC,
            DASHBOARD_NOTIFICATION_EVENT, DashboardTaskIds.GENERATE_SPEC_DJ_FORM, variables);

        assertProcessClosed();
    }

    @Test
    void shouldSuccessfullyComplete_whenLipVLipAndDashboardEnabled() {
        VariableMap variables = flowFlags(true, true, true);

        startAndAssertProcess();
        startBusinessProcess(variables);
        completeDocGeneration(variables);
        completePrimaryNotifications(variables);

        ExternalTask dashboard = assertNextExternalTask(DASHBOARD_NOTIFICATION_TOPIC);
        assertCompleteExternalTask(dashboard, DASHBOARD_NOTIFICATION_TOPIC,
            DASHBOARD_NOTIFICATION_EVENT, DashboardTaskIds.GENERATE_SPEC_DJ_FORM, variables);

        assertProcessClosed();
    }

    @Test
    void shouldAbort_whenStartBusinessProcessThrowsAnError() {
        startAndAssertProcess();

        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        assertFailExternalTask(startBusiness, START_BUSINESS_TOPIC, START_BUSINESS_EVENT, START_BUSINESS_ACTIVITY);

        assertNoExternalTasksLeft();
    }

    private void completePrimaryNotifications(VariableMap variables) {
        ExternalTask notification = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            notification,
            PROCESS_CASE_EVENT,
            NOTIFY_EVENT,
            NOTIFY_EVENT_ACTIVITY_ID,
            variables
        );

        ExternalTask roboticsNotification = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            roboticsNotification,
            PROCESS_CASE_EVENT,
            NOTIFY_RPA_EVENT,
            NOTIFY_RPA_ACTIVITY_ID,
            variables
        );
    }

    private void completeDocGeneration(VariableMap variables) {
        ExternalTask documentGeneration = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            documentGeneration,
            PROCESS_CASE_EVENT,
            GENERATE_DJ_FORM_SPEC_EVENT,
            GENERATE_DJ_FORM_SPEC_ACTIVITY_ID,
            variables
        );
    }

    private void startAndAssertProcess() {
        assertFalse(processInstance.isEnded());
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);
    }

    private void assertProcessClosed() {
        ExternalTask endBusinessProcess = assertNextExternalTask(END_BUSINESS_PROCESS);
        completeBusinessProcess(endBusinessProcess);
        assertNoExternalTasksLeft();
    }

    private VariableMap flowFlags(boolean dashboardEnabled,
                                  boolean lipCase,
                                  boolean unrepresentedDefendantOne) {
        VariableMap variables = Variables.createVariables();
        variables.putValue(FLOW_FLAGS, Map.of(
            DASHBOARD_SERVICE_ENABLED, dashboardEnabled,
            LIP_CASE, lipCase,
            UNREPRESENTED_DEFENDANT_ONE, unrepresentedDefendantOne
        ));
        return variables;
    }
}
