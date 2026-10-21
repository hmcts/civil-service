package uk.gov.hmcts.reform.civil.bpmn;

import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

class EnterBreathingSpaceSpecTest extends BpmnBaseTest {

    private static final String MESSAGE_NAME = "ENTER_BREATHING_SPACE_SPEC";
    private static final String PROCESS_ID = "ENTER_BREATHING_SPACE_SPEC";
    private static final String NOTIFY_RPA_ON_CONTINUOUS_FEED = "NOTIFY_RPA_ON_CONTINUOUS_FEED";
    private static final String NOTIFY_RPA_ON_CONTINUOUS_FEED_ACTIVITY_ID = "NotifyRoboticsBreathingSpaceEnter";

    EnterBreathingSpaceSpecTest() {
        super("enter_breathing_space_spec.bpmn", PROCESS_ID);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldCompleteWithOneNotificationTask(boolean twoRepresentatives) {
        VariableMap variables = flowFlagVariables(!twoRepresentatives, twoRepresentatives);
        assertFalse(processInstance.isEnded());
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);
        startBusinessProcess(variables);
        assertNotifications(variables);
    }

    @Test
    void shouldAbort_whenStartBusinessProcessThrowsAnError() {
        assertFalse(processInstance.isEnded());
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        assertFailExternalTask(startBusiness, START_BUSINESS_TOPIC, START_BUSINESS_EVENT, START_BUSINESS_ACTIVITY);

        assertNoExternalTasksLeft();
    }

    private void assertNotifications(VariableMap variables) {
        ExternalTask applicantNotification = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            applicantNotification,
            PROCESS_CASE_EVENT,
            "NOTIFY_EVENT",
            "BreathingSpaceEnterNotifyParties",
            variables
        );

        ExternalTask roboticsNotification = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            roboticsNotification,
            PROCESS_CASE_EVENT,
            NOTIFY_RPA_ON_CONTINUOUS_FEED,
            NOTIFY_RPA_ON_CONTINUOUS_FEED_ACTIVITY_ID,
            variables
        );

        ExternalTask endBusinessProcess = assertNextExternalTask(END_BUSINESS_PROCESS);
        completeBusinessProcess(endBusinessProcess);

        assertNoExternalTasksLeft();
    }

    private VariableMap flowFlagVariables(boolean oneRespondentRepresentative, boolean twoRespondentRepresentatives) {
        VariableMap variables = Variables.createVariables();
        variables.putValue(FLOW_FLAGS, Map.of(
            ONE_RESPONDENT_REPRESENTATIVE, oneRespondentRepresentative,
            TWO_RESPONDENT_REPRESENTATIVES, twoRespondentRepresentatives
        ));
        return variables;
    }
}
