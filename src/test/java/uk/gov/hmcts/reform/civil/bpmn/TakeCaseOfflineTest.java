package uk.gov.hmcts.reform.civil.bpmn;

import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

class TakeCaseOfflineTest extends BpmnBaseTest {

    public static final String MESSAGE_NAME = "TAKE_CASE_OFFLINE";
    public static final String PROCESS_ID = "TAKE_CASE_OFFLINE";

    private static final String NOTIFY_RPA_ON_CASE_HANDED_OFFLINE = "NOTIFY_RPA_ON_CASE_HANDED_OFFLINE";
    private static final String NOTIFY_RPA_ON_CASE_HANDED_OFFLINE_ACTIVITY_ID = "NotifyRoboticsOnCaseHandedOffline";

    public static final String TRIGGER_APPLICATION_PROCEEDS_IN_HERITAGE = "TRIGGER_APPLICATION_PROCEEDS_IN_HERITAGE";
    private static final String APPLICATION_PROCEEDS_IN_HERITAGE_ACTIVITY_ID = "UpdateGeneralApplicationStatus";
    public static final String APPLICATION_OFFLINE_UPDATE_CLAIM = "APPLICATION_OFFLINE_UPDATE_CLAIM";
    private static final String APPLICATION_OFFLINE_UPDATE_CLAIM_ACTIVITY_ID = "UpdateClaimWithApplicationStatus";
    public static final String NOTIFY_EVENT = "NOTIFY_EVENT";
    public static final String TAKE_CASE_OFFLINE_NOTIFIER = "TakeCaseOfflineNotifier";
    private static final String DASHBOARD_NOTIFICATION_ACTIVITY_ID = "GenerateDashboardNotificationsTakeCaseOffline";

    public TakeCaseOfflineTest() {
        super("take_case_offline.bpmn", "TAKE_CASE_OFFLINE");
    }

    @ParameterizedTest
    @CsvSource({"true", "false"})
    void shouldSuccessfullyCompleteTakeCaseOfflineMultiparty(boolean twoRepresentatives) {
        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

        VariableMap variables = Variables.createVariables();
        variables.put("flowFlags", Map.of(
                ONE_RESPONDENT_REPRESENTATIVE, !twoRepresentatives,
                TWO_RESPONDENT_REPRESENTATIVES, twoRepresentatives,
                UNREPRESENTED_DEFENDANT_ONE, false));

        //complete the start business process
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        assertCompleteExternalTask(
                startBusiness,
                START_BUSINESS_TOPIC,
                START_BUSINESS_EVENT,
                START_BUSINESS_ACTIVITY,
                variables
        );

        //Update General Application Status
        ExternalTask updateApplicationStatus = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            updateApplicationStatus,
            PROCESS_CASE_EVENT,
            TRIGGER_APPLICATION_PROCEEDS_IN_HERITAGE,
            APPLICATION_PROCEEDS_IN_HERITAGE_ACTIVITY_ID
        );

        //Update Claim Details with General Application Status
        ExternalTask updateClaimWithApplicationStatus = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            updateClaimWithApplicationStatus,
            PROCESS_CASE_EVENT,
            APPLICATION_OFFLINE_UPDATE_CLAIM,
            APPLICATION_OFFLINE_UPDATE_CLAIM_ACTIVITY_ID
        );

        //complete the RPA notification
        ExternalTask rpaNotification = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
                rpaNotification,
                PROCESS_CASE_EVENT,
                NOTIFY_RPA_ON_CASE_HANDED_OFFLINE,
                NOTIFY_RPA_ON_CASE_HANDED_OFFLINE_ACTIVITY_ID
        );

        //complete the notification to relevant parties
        ExternalTask respondentNotification = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(respondentNotification,
                PROCESS_CASE_EVENT,
                NOTIFY_EVENT,
                TAKE_CASE_OFFLINE_NOTIFIER
        );

        //end business process
        ExternalTask endBusinessProcess = assertNextExternalTask(END_BUSINESS_PROCESS);
        completeBusinessProcess(endBusinessProcess);

        assertNoExternalTasksLeft();
    }

    @ParameterizedTest
    @CsvSource(value = {"true, true", "true, false", "false, true", "true, null"}, nullValues = "null")
    void shouldSuccessfullyCompleteTakeCaseOfflineUnrepresentedDefendant(boolean unrepresentedDefendant1,
                                                                         Boolean unrepresentedDefendant2) {
        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

        Map<String, Object> flowFlags = new HashMap<>();
        flowFlags.put(UNREPRESENTED_DEFENDANT_ONE, unrepresentedDefendant1);
        if (unrepresentedDefendant2 != null) {
            flowFlags.put(UNREPRESENTED_DEFENDANT_TWO, unrepresentedDefendant2);
        }
        VariableMap variables = Variables.createVariables();
        variables.put("flowFlags", flowFlags);

        //complete the start business process
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        assertCompleteExternalTask(
                startBusiness,
                START_BUSINESS_TOPIC,
                START_BUSINESS_EVENT,
                START_BUSINESS_ACTIVITY,
                variables
        );

        //Update General Application Status
        ExternalTask updateApplicationStatus = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            updateApplicationStatus,
            PROCESS_CASE_EVENT,
            TRIGGER_APPLICATION_PROCEEDS_IN_HERITAGE,
            APPLICATION_PROCEEDS_IN_HERITAGE_ACTIVITY_ID
        );

        //Update Claim Details with General Application Status
        ExternalTask updateClaimWithApplicationStatus = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            updateClaimWithApplicationStatus,
            PROCESS_CASE_EVENT,
            APPLICATION_OFFLINE_UPDATE_CLAIM,
            APPLICATION_OFFLINE_UPDATE_CLAIM_ACTIVITY_ID
        );

        //complete the RPA notification
        ExternalTask rpaNotification = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
                rpaNotification,
                PROCESS_CASE_EVENT,
                NOTIFY_RPA_ON_CASE_HANDED_OFFLINE,
                NOTIFY_RPA_ON_CASE_HANDED_OFFLINE_ACTIVITY_ID
        );

        //complete the notification to relevant parties
        ExternalTask applicantNotification = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(applicantNotification,
                PROCESS_CASE_EVENT,
                NOTIFY_EVENT,
                TAKE_CASE_OFFLINE_NOTIFIER
        );

        //end business process
        ExternalTask endBusinessProcess = assertNextExternalTask(END_BUSINESS_PROCESS);
        completeBusinessProcess(endBusinessProcess);

        assertNoExternalTasksLeft();
    }

    @ParameterizedTest
    @CsvSource({"true", "false"})
    void shouldSuccessfullyCompleteTakeCaseOfflineMultiparty_GAEnabled(boolean twoRepresentatives) {
        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

        VariableMap variables = Variables.createVariables();
        variables.put("flowFlags", Map.of(
                ONE_RESPONDENT_REPRESENTATIVE, !twoRepresentatives,
                TWO_RESPONDENT_REPRESENTATIVES, twoRepresentatives,
                UNREPRESENTED_DEFENDANT_ONE, false,
                DASHBOARD_SERVICE_ENABLED, true));

        //complete the start business process
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        assertCompleteExternalTask(
                startBusiness,
                START_BUSINESS_TOPIC,
                START_BUSINESS_EVENT,
                START_BUSINESS_ACTIVITY,
                variables
        );

        //Update General Application Status
        ExternalTask updateApplicationStatus = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
                updateApplicationStatus,
                PROCESS_CASE_EVENT,
                TRIGGER_APPLICATION_PROCEEDS_IN_HERITAGE,
                APPLICATION_PROCEEDS_IN_HERITAGE_ACTIVITY_ID
        );

        //Update Claim Details with General Application Status
        ExternalTask updateClaimWithApplicationStatus = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
                updateClaimWithApplicationStatus,
                PROCESS_CASE_EVENT,
                APPLICATION_OFFLINE_UPDATE_CLAIM,
                APPLICATION_OFFLINE_UPDATE_CLAIM_ACTIVITY_ID
        );

        //complete the RPA notification
        ExternalTask rpaNotification = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
                rpaNotification,
                PROCESS_CASE_EVENT,
                NOTIFY_RPA_ON_CASE_HANDED_OFFLINE,
                NOTIFY_RPA_ON_CASE_HANDED_OFFLINE_ACTIVITY_ID
        );

        //complete the notification to relevant parties
        ExternalTask respondentNotification = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(respondentNotification,
                PROCESS_CASE_EVENT,
                NOTIFY_EVENT,
                TAKE_CASE_OFFLINE_NOTIFIER
        );

        //Dashboard notification
        ExternalTask mainCaseNotification = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
                mainCaseNotification,
                PROCESS_CASE_EVENT,
                DASHBOARD_NOTIFICATION_EVENT,
                DASHBOARD_NOTIFICATION_ACTIVITY_ID
        );

        //end business process
        ExternalTask endBusinessProcess = assertNextExternalTask(END_BUSINESS_PROCESS);
        completeBusinessProcess(endBusinessProcess);

        assertNoExternalTasksLeft();
    }

    @ParameterizedTest
    @CsvSource(value = {"true, true", "true, false", "false, true", "true, null"}, nullValues = "null")
    void shouldSuccessfullyCompleteTakeCaseOfflineUnrepresentedDefendant_GAEnabled(boolean unrepresentedDefendant1,
                                                                                   Boolean unrepresentedDefendant2) {
        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

        Map<String, Object> flowFlags = new HashMap<>();
        flowFlags.put(UNREPRESENTED_DEFENDANT_ONE, unrepresentedDefendant1);
        if (unrepresentedDefendant2 != null) {
            flowFlags.put(UNREPRESENTED_DEFENDANT_TWO, unrepresentedDefendant2);
        }
        flowFlags.put(DASHBOARD_SERVICE_ENABLED, true);
        VariableMap variables = Variables.createVariables();
        variables.put("flowFlags", flowFlags);

        //complete the start business process
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        assertCompleteExternalTask(
                startBusiness,
                START_BUSINESS_TOPIC,
                START_BUSINESS_EVENT,
                START_BUSINESS_ACTIVITY,
                variables
        );

        //Update General Application Status
        ExternalTask updateApplicationStatus = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
                updateApplicationStatus,
                PROCESS_CASE_EVENT,
                TRIGGER_APPLICATION_PROCEEDS_IN_HERITAGE,
                APPLICATION_PROCEEDS_IN_HERITAGE_ACTIVITY_ID
        );

        //Update Claim Details with General Application Status
        ExternalTask updateClaimWithApplicationStatus = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
                updateClaimWithApplicationStatus,
                PROCESS_CASE_EVENT,
                APPLICATION_OFFLINE_UPDATE_CLAIM,
                APPLICATION_OFFLINE_UPDATE_CLAIM_ACTIVITY_ID
        );

        //complete the RPA notification
        ExternalTask rpaNotification = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
                rpaNotification,
                PROCESS_CASE_EVENT,
                NOTIFY_RPA_ON_CASE_HANDED_OFFLINE,
                NOTIFY_RPA_ON_CASE_HANDED_OFFLINE_ACTIVITY_ID
        );

        //complete the notification to relevant parties
        ExternalTask applicantNotification = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(applicantNotification,
                PROCESS_CASE_EVENT,
                NOTIFY_EVENT,
                TAKE_CASE_OFFLINE_NOTIFIER
        );

        //Dashboard notification
        ExternalTask mainCaseNotification = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
                mainCaseNotification,
                PROCESS_CASE_EVENT,
                DASHBOARD_NOTIFICATION_EVENT,
                DASHBOARD_NOTIFICATION_ACTIVITY_ID
        );

        //end business process
        ExternalTask endBusinessProcess = assertNextExternalTask(END_BUSINESS_PROCESS);
        completeBusinessProcess(endBusinessProcess);

        assertNoExternalTasksLeft();
    }

    @Test
    void shouldAbort_whenStartBusinessProcessThrowsAnError() {
        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

        //fail the start business process
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        assertFailExternalTask(startBusiness, START_BUSINESS_TOPIC, START_BUSINESS_EVENT, START_BUSINESS_ACTIVITY);

        assertNoExternalTasksLeft();
    }
}
