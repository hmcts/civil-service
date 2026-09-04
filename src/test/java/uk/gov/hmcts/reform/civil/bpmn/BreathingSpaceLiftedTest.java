package uk.gov.hmcts.reform.civil.bpmn;

import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

class BreathingSpaceLiftedTest extends BpmnBaseTest {

    public static final String MESSAGE_NAME = "LIFT_BREATHING_SPACE_SPEC";
    public static final String PROCESS_ID = "BREATHING_SPACE_LIFTED";

    private static final String NOTIFY_APPLICANT_SOLICITOR1_BREATHING_SPACE_LIFTED
        = "NOTIFY_APPLICANT_SOLICITOR1_BREATHING_SPACE_LIFTED";
    private static final String NOTIFY_RESPONDENT_SOLICITOR1_BREATHING_SPACE_LIFTED
        = "NOTIFY_RESPONDENT_SOLICITOR1_BREATHING_SPACE_LIFTED";
    private static final String NOTIFY_LIP_APPLICANT_BREATHING_SPACE_LIFTED
        = "NOTIFY_LIP_APPLICANT_BREATHING_SPACE_LIFTED";
    private static final String NOTIFY_LIP_RESPONDENT1_BREATHING_SPACE_LIFTED
        = "NOTIFY_LIP_RESPONDENT1_BREATHING_SPACE_LIFTED";
    private static final String NOTIFY_RPA_ON_CONTINUOUS_FEED
        = "NOTIFY_RPA_ON_CONTINUOUS_FEED";

    private static final String NOTIFY_APPLICANT_SOLICITOR1_BREATHING_SPACE_LIFTED_ACTIVITY_ID
        = "NotifyApplicantSolicitorBSLifted";
    private static final String NOTIFY_RESPONDENT_SOLICITOR1_BREATHING_SPACE_LIFTED_ACTIVITY_ID
        = "NotifyRespondentSolicitorBSLifted";
    private static final String NOTIFY_LIP_APPLICANT_ACTIVITY_ID = "BreathingSpaceLiftedNotifyLipApplicant";
    private static final String NOTIFY_LIP_RESPONDENT_ACTIVITY_ID = "BreathingSpaceLiftedNotifyLipRespondent1";
    private static final String NOTIFY_RPA_ON_CONTINUOUS_FEED_ACTIVITY_ID = "NotifyRpaBsLifted";

    public BreathingSpaceLiftedTest() {
        super("breathing_space_lifted.bpmn", PROCESS_ID);
    }

    @Test
    void shouldSuccessfullyComplete_whenLrClaimantAndLrDefendant() {
        VariableMap variables = flowFlagVariables(false, false);

        assertFalse(processInstance.isEnded());
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

        startBusinessProcess(variables);

        ExternalTask respondentNotification = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            respondentNotification,
            PROCESS_CASE_EVENT,
            NOTIFY_RESPONDENT_SOLICITOR1_BREATHING_SPACE_LIFTED,
            NOTIFY_RESPONDENT_SOLICITOR1_BREATHING_SPACE_LIFTED_ACTIVITY_ID,
            variables
        );

        assertApplicantSolicitorAndRoboticsNotifications(variables);
    }

    @Test
    void shouldSuccessfullyComplete_whenLipClaimantAndLipDefendant() {
        VariableMap variables = flowFlagVariables(true, true);

        assertFalse(processInstance.isEnded());
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

        startBusinessProcess(variables);

        ExternalTask lipRespondentNotification = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            lipRespondentNotification,
            PROCESS_CASE_EVENT,
            NOTIFY_LIP_RESPONDENT1_BREATHING_SPACE_LIFTED,
            NOTIFY_LIP_RESPONDENT_ACTIVITY_ID,
            variables
        );

        ExternalTask lipApplicantNotification = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            lipApplicantNotification,
            PROCESS_CASE_EVENT,
            NOTIFY_LIP_APPLICANT_BREATHING_SPACE_LIFTED,
            NOTIFY_LIP_APPLICANT_ACTIVITY_ID,
            variables
        );

        assertRoboticsAndEnd(variables);
    }

    @Test
    void shouldSuccessfullyComplete_whenLipClaimantAndLrDefendant() {
        VariableMap variables = flowFlagVariables(true, false);

        assertFalse(processInstance.isEnded());
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

        startBusinessProcess(variables);

        ExternalTask respondentNotification = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            respondentNotification,
            PROCESS_CASE_EVENT,
            NOTIFY_RESPONDENT_SOLICITOR1_BREATHING_SPACE_LIFTED,
            NOTIFY_RESPONDENT_SOLICITOR1_BREATHING_SPACE_LIFTED_ACTIVITY_ID,
            variables
        );

        ExternalTask lipApplicantNotification = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            lipApplicantNotification,
            PROCESS_CASE_EVENT,
            NOTIFY_LIP_APPLICANT_BREATHING_SPACE_LIFTED,
            NOTIFY_LIP_APPLICANT_ACTIVITY_ID,
            variables
        );

        assertRoboticsAndEnd(variables);
    }

    @Test
    void shouldSuccessfullyComplete_whenLrClaimantAndLipDefendant() {
        VariableMap variables = flowFlagVariables(false, true);

        assertFalse(processInstance.isEnded());
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

        startBusinessProcess(variables);

        ExternalTask lipRespondentNotification = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            lipRespondentNotification,
            PROCESS_CASE_EVENT,
            NOTIFY_LIP_RESPONDENT1_BREATHING_SPACE_LIFTED,
            NOTIFY_LIP_RESPONDENT_ACTIVITY_ID,
            variables
        );

        assertApplicantSolicitorAndRoboticsNotifications(variables);
    }

    @Test
    void shouldAbort_whenStartBusinessProcessThrowsAnError() {
        assertFalse(processInstance.isEnded());
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        assertFailExternalTask(startBusiness, START_BUSINESS_TOPIC, START_BUSINESS_EVENT, START_BUSINESS_ACTIVITY);

        assertNoExternalTasksLeft();
    }

    private void assertApplicantSolicitorAndRoboticsNotifications(VariableMap variables) {
        ExternalTask applicantNotification = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            applicantNotification,
            PROCESS_CASE_EVENT,
            NOTIFY_APPLICANT_SOLICITOR1_BREATHING_SPACE_LIFTED,
            NOTIFY_APPLICANT_SOLICITOR1_BREATHING_SPACE_LIFTED_ACTIVITY_ID,
            variables
        );

        assertRoboticsAndEnd(variables);
    }

    private void assertRoboticsAndEnd(VariableMap variables) {
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

    private VariableMap flowFlagVariables(boolean lipCase, boolean unrepresentedDefendantOne) {
        Map<String, Object> flags = new HashMap<>();
        flags.put(LIP_CASE, lipCase);
        flags.put(UNREPRESENTED_DEFENDANT_ONE, unrepresentedDefendantOne);
        VariableMap variables = Variables.createVariables();
        variables.putValue(FLOW_FLAGS, flags);
        return variables;
    }
}
