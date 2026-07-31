package uk.gov.hmcts.reform.civil.bpmn;

import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

class EnterBreathingSpaceSpecTest extends BpmnBaseTest {

    private static final String MESSAGE_NAME = "ENTER_BREATHING_SPACE_SPEC";
    private static final String PROCESS_ID = "ENTER_BREATHING_SPACE_SPEC";
    private static final String NOTIFY_RESPONDENT_SOLICITOR_1 = "NOTIFY_RESPONDENT_SOLICITOR1_BREATHING_SPACE_ENTER";
    private static final String NOTIFY_RESPONDENT_SOLICITOR_1_ACTIVITY_ID = "BreathingSpaceEnterNotifyRespondentSolicitor1";
    private static final String NOTIFY_RESPONDENT_SOLICITOR_2 = "NOTIFY_RESPONDENT_SOLICITOR2_BREATHING_SPACE_ENTER";
    private static final String NOTIFY_RESPONDENT_SOLICITOR_2_ACTIVITY_ID = "BreathingSpaceEnterNotifyRespondentSolicitor2";
    private static final String NOTIFY_APPLICANT_SOLICITOR = "NOTIFY_APPLICANT_SOLICITOR1_BREATHING_SPACE_ENTER";
    private static final String NOTIFY_APPLICANT_SOLICITOR_ACTIVITY_ID = "BreathingSpaceEnterNotifyApplicantSolicitor1";
    private static final String NOTIFY_LIP_RESPONDENT = "NOTIFY_LIP_RESPONDENT1_BREATHING_SPACE_ENTER";
    private static final String NOTIFY_LIP_RESPONDENT_ACTIVITY_ID = "BreathingSpaceEnterNotifyLipRespondent1";
    private static final String NOTIFY_LIP_APPLICANT = "NOTIFY_LIP_APPLICANT_BREATHING_SPACE_ENTER";
    private static final String NOTIFY_LIP_APPLICANT_ACTIVITY_ID = "BreathingSpaceEnterNotifyLipApplicant";
    private static final String NOTIFY_RPA_ON_CONTINUOUS_FEED = "NOTIFY_RPA_ON_CONTINUOUS_FEED";
    private static final String NOTIFY_RPA_ON_CONTINUOUS_FEED_ACTIVITY_ID = "NotifyRoboticsBreathingSpaceEnter";

    EnterBreathingSpaceSpecTest() {
        super("enter_breathing_space_spec.bpmn", PROCESS_ID);
    }

    @Test
    void shouldSuccessfullyComplete_whenSingleRespondentRepresentative() {
        VariableMap variables = flowFlagVariables(true, false, false, false);

        assertFalse(processInstance.isEnded());
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

        startBusinessProcess(variables);

        ExternalTask respondentNotification = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            respondentNotification,
            PROCESS_CASE_EVENT,
            NOTIFY_RESPONDENT_SOLICITOR_1,
            NOTIFY_RESPONDENT_SOLICITOR_1_ACTIVITY_ID,
            variables
        );

        assertApplicantSolicitorAndRoboticsNotifications(variables);
    }

    @Test
    void shouldSuccessfullyComplete_whenTwoRespondentRepresentatives() {
        VariableMap variables = flowFlagVariables(false, true, false, false);

        assertFalse(processInstance.isEnded());
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

        startBusinessProcess(variables);

        ExternalTask respondentOneNotification = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            respondentOneNotification,
            PROCESS_CASE_EVENT,
            NOTIFY_RESPONDENT_SOLICITOR_1,
            NOTIFY_RESPONDENT_SOLICITOR_1_ACTIVITY_ID,
            variables
        );

        ExternalTask respondentTwoNotification = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            respondentTwoNotification,
            PROCESS_CASE_EVENT,
            NOTIFY_RESPONDENT_SOLICITOR_2,
            NOTIFY_RESPONDENT_SOLICITOR_2_ACTIVITY_ID,
            variables
        );

        assertApplicantSolicitorAndRoboticsNotifications(variables);
    }

    @Test
    void shouldSuccessfullyComplete_whenLipClaimantAndLipDefendant() {
        VariableMap variables = flowFlagVariables(false, false, true, true);

        assertFalse(processInstance.isEnded());
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

        startBusinessProcess(variables);

        ExternalTask lipRespondentNotification = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            lipRespondentNotification,
            PROCESS_CASE_EVENT,
            NOTIFY_LIP_RESPONDENT,
            NOTIFY_LIP_RESPONDENT_ACTIVITY_ID,
            variables
        );

        ExternalTask lipApplicantNotification = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            lipApplicantNotification,
            PROCESS_CASE_EVENT,
            NOTIFY_LIP_APPLICANT,
            NOTIFY_LIP_APPLICANT_ACTIVITY_ID,
            variables
        );

        assertRoboticsAndEnd(variables);
    }

    @Test
    void shouldSuccessfullyComplete_whenLipClaimantAndLrDefendant() {
        VariableMap variables = flowFlagVariables(true, false, true, false);

        assertFalse(processInstance.isEnded());
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

        startBusinessProcess(variables);

        ExternalTask respondentNotification = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            respondentNotification,
            PROCESS_CASE_EVENT,
            NOTIFY_RESPONDENT_SOLICITOR_1,
            NOTIFY_RESPONDENT_SOLICITOR_1_ACTIVITY_ID,
            variables
        );

        ExternalTask lipApplicantNotification = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            lipApplicantNotification,
            PROCESS_CASE_EVENT,
            NOTIFY_LIP_APPLICANT,
            NOTIFY_LIP_APPLICANT_ACTIVITY_ID,
            variables
        );

        assertRoboticsAndEnd(variables);
    }

    @Test
    void shouldSuccessfullyComplete_whenLrClaimantAndLipDefendant() {
        VariableMap variables = flowFlagVariables(false, false, false, true);

        assertFalse(processInstance.isEnded());
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

        startBusinessProcess(variables);

        ExternalTask lipRespondentNotification = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            lipRespondentNotification,
            PROCESS_CASE_EVENT,
            NOTIFY_LIP_RESPONDENT,
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
            NOTIFY_APPLICANT_SOLICITOR,
            NOTIFY_APPLICANT_SOLICITOR_ACTIVITY_ID,
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

    private VariableMap flowFlagVariables(boolean oneRespondentRepresentative,
                                          boolean twoRespondentRepresentatives,
                                          boolean lipCase,
                                          boolean unrepresentedDefendantOne) {
        VariableMap variables = Variables.createVariables();
        Map<String, Object> flags = new HashMap<>();
        flags.put(ONE_RESPONDENT_REPRESENTATIVE, oneRespondentRepresentative);
        flags.put(TWO_RESPONDENT_REPRESENTATIVES, twoRespondentRepresentatives);
        flags.put(LIP_CASE, lipCase);
        flags.put(UNREPRESENTED_DEFENDANT_ONE, unrepresentedDefendantOne);
        variables.putValue(FLOW_FLAGS, flags);
        return variables;
    }
}
