package uk.gov.hmcts.reform.civil.bpmn;

import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class BreathingSpaceLiftedTest extends BpmnBaseTest {

    public static final String MESSAGE_NAME = "LIFT_BREATHING_SPACE_SPEC";
    public static final String PROCESS_ID = "BREATHING_SPACE_LIFTED";

    //CCD CASE EVENTS
    public static final String NOTIFY_APPLICANT_SOLICITOR1_BREATHING_SPACE_LIFTED
        = "NOTIFY_APPLICANT_SOLICITOR1_BREATHING_SPACE_LIFTED";
    public static final String NOTIFY_RESPONDENT_SOLICITOR1_BREATHING_SPACE_LIFTED
        = "NOTIFY_RESPONDENT_SOLICITOR1_BREATHING_SPACE_LIFTED";
    public static final String NOTIFY_RPA_ON_CONTINUOUS_FEED
        = "NOTIFY_RPA_ON_CONTINUOUS_FEED";

    //ACTIVITY IDs
    public static final String NOTIFY_APPLICANT_SOLICITOR1_BREATHING_SPACE_LIFTED_ACTIVITY_ID
        = "NotifyApplicantSolicitorBSLifted";
    public static final String NOTIFY_RESPONDENT_SOLICITOR1_BREATHING_SPACE_LIFTED_ACTIVITY_ID
        = "NotifyRespondentSolicitorBSLifted";
    public static final String NOTIFY_RPA_ON_CONTINUOUS_FEED_ACTIVITY_ID = "NotifyRpaBsLifted";

    public BreathingSpaceLiftedTest() {
        super("breathing_space_lifted.bpmn", PROCESS_ID);
    }

    @Test
    void shouldSuccessfullyCompleteBreathingSpaceLifted_withRpa() {
        VariableMap variables = flowFlagVariables(false);

        assertFalse(processInstance.isEnded());
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

        //complete the start business process
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        assertCompleteExternalTask(
            startBusiness,
            START_BUSINESS_TOPIC,
            START_BUSINESS_EVENT,
            START_BUSINESS_ACTIVITY,
            variables
        );

        //complete the applicant notification
        ExternalTask notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(notificationTask,
                                   PROCESS_CASE_EVENT,
                                   NOTIFY_APPLICANT_SOLICITOR1_BREATHING_SPACE_LIFTED,
                                   NOTIFY_APPLICANT_SOLICITOR1_BREATHING_SPACE_LIFTED_ACTIVITY_ID,
                                   variables
        );

        //complete the respondent notification
        notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(notificationTask,
                                   PROCESS_CASE_EVENT,
                                   NOTIFY_RESPONDENT_SOLICITOR1_BREATHING_SPACE_LIFTED,
                                   NOTIFY_RESPONDENT_SOLICITOR1_BREATHING_SPACE_LIFTED_ACTIVITY_ID,
                                   variables
        );

        //complete the Robotics notification
        ExternalTask forRobotics = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            forRobotics,
            PROCESS_CASE_EVENT,
            NOTIFY_RPA_ON_CONTINUOUS_FEED,
            NOTIFY_RPA_ON_CONTINUOUS_FEED_ACTIVITY_ID,
            variables
        );

        ExternalTask endBusinessProcess = assertNextExternalTask(END_BUSINESS_PROCESS);
        completeBusinessProcess(endBusinessProcess);
        assertNoExternalTasksLeft();
    }

    @Test
    void shouldSuccessfullyCompleteBreathingSpaceLifted_whenUnrepresentedDefendant() {
        VariableMap variables = flowFlagVariables(true);

        assertFalse(processInstance.isEnded());
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        assertCompleteExternalTask(
            startBusiness,
            START_BUSINESS_TOPIC,
            START_BUSINESS_EVENT,
            START_BUSINESS_ACTIVITY,
            variables
        );

        ExternalTask notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(notificationTask,
                                   PROCESS_CASE_EVENT,
                                   NOTIFY_APPLICANT_SOLICITOR1_BREATHING_SPACE_LIFTED,
                                   NOTIFY_APPLICANT_SOLICITOR1_BREATHING_SPACE_LIFTED_ACTIVITY_ID,
                                   variables
        );

        // LiP defendant skips respondent solicitor notification
        ExternalTask forRobotics = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            forRobotics,
            PROCESS_CASE_EVENT,
            NOTIFY_RPA_ON_CONTINUOUS_FEED,
            NOTIFY_RPA_ON_CONTINUOUS_FEED_ACTIVITY_ID,
            variables
        );

        ExternalTask endBusinessProcess = assertNextExternalTask(END_BUSINESS_PROCESS);
        completeBusinessProcess(endBusinessProcess);
        assertNoExternalTasksLeft();
    }

    private VariableMap flowFlagVariables(boolean unrepresentedDefendantOne) {
        VariableMap variables = Variables.createVariables();
        variables.putValue(FLOW_FLAGS, Map.of(
            UNREPRESENTED_DEFENDANT_ONE, unrepresentedDefendantOne
        ));
        return variables;
    }
}
