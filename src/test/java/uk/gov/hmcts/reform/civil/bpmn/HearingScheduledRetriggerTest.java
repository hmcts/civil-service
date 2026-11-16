package uk.gov.hmcts.reform.civil.bpmn;

import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class HearingScheduledRetriggerTest extends BpmnBaseTest {

    public static final String MESSAGE_NAME = "HEARING_SCHEDULED_RETRIGGER";
    public static final String PROCESS_ID = "HEARING_SCHEDULED_RETRIGGER";

    //ACTIVITY IDs
    private static final String GENERATE_DASHBOARD_NOTIFICATION_REQUEST_A_LISTING
        = "GenerateDashboardNotificationRequestAListing";

    public HearingScheduledRetriggerTest() {
        super("hearing_scheduled_retrigger.bpmn", PROCESS_ID);
    }

    @Test
    void shouldSuccessfullyCompleteHearingScheduledRetrigger() {
        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

        VariableMap variables = Variables.createVariables();

        //complete the start business process
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        assertCompleteExternalTask(startBusiness, START_BUSINESS_TOPIC,
                                   START_BUSINESS_EVENT, START_BUSINESS_ACTIVITY, variables
        );

        //Generate the Dashboard notifications
        ExternalTask dashboardTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(dashboardTask,
                                   PROCESS_CASE_EVENT,
                                   DASHBOARD_NOTIFICATION_EVENT,
                                   GENERATE_DASHBOARD_NOTIFICATION_REQUEST_A_LISTING,
                                   variables
        );

        //end business process
        ExternalTask endBusinessProcess = assertNextExternalTask(END_BUSINESS_PROCESS);
        completeBusinessProcess(endBusinessProcess);

        assertNoExternalTasksLeft();
    }
}
