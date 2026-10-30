package uk.gov.hmcts.reform.civil.bpmn;

import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class StayCaseTest extends BpmnBaseTest {

    public static final String MESSAGE_NAME = "STAY_CASE";
    public static final String PROCESS_ID = "STAY_CASE";

    public StayCaseTest() {
        super("stay_case.bpmn", PROCESS_ID);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldCompleteStayCaseWithOneNotificationTask(boolean twoRepresentatives) {
        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

        VariableMap variables = Variables.createVariables();
        variables.put("flowFlags", Map.of(
            ONE_RESPONDENT_REPRESENTATIVE, !twoRepresentatives,
            DASHBOARD_SERVICE_ENABLED, true,
            TWO_RESPONDENT_REPRESENTATIVES, twoRepresentatives
        ));

        //complete the start business process
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        assertCompleteExternalTask(startBusiness, START_BUSINESS_TOPIC,
                                   START_BUSINESS_EVENT, START_BUSINESS_ACTIVITY, variables
        );

        ExternalTask notificationTask;

        // One framework task selects all recipients, regardless of the former gateway branch.
        notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(notificationTask, PROCESS_CASE_EVENT,
                                   "NOTIFY_EVENT", "StayCaseNotifyParties", variables);

        //complete the dashboard notification
        notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(notificationTask, PROCESS_CASE_EVENT,
                                   "DASHBOARD_NOTIFICATION_EVENT",
                                   "GenerateDashboardNotificationsStayCase",
                                   variables
        );

        //end business process
        ExternalTask endBusinessProcess = assertNextExternalTask(END_BUSINESS_PROCESS);
        completeBusinessProcess(endBusinessProcess);

        assertNoExternalTasksLeft();
    }
}
