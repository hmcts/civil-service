package uk.gov.hmcts.reform.civil.bpmn;

import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

class CreateSDOTest extends BpmnBaseTest {

    public static final String MESSAGE_NAME = "CREATE_SDO";
    public static final String PROCESS_ID = "CREATE_SDO";
    private static final String TRIGGER_UPDATE_GA_LOCATION = "TRIGGER_UPDATE_GA_LOCATION";
    private static final String TRIGGER_UPDATE_GA_LOCATION_ACTIVITY_ID = "TriggerAndUpdateGenAppLocation";
    public static final String GENERATE_DASHBOARD_NOTIFICATION_CREATE_SDO = "GenerateDashboardNotificationCreateSDO";

    public CreateSDOTest() {
        super("create_sdo.bpmn", PROCESS_ID);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    @NullSource
    void shouldSuccessfullyCompleteTakeCaseOffline(Boolean dashboardEnabled) {
        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

        VariableMap variables = Variables.createVariables();
        Map<String, Object> flowFlags = new HashMap<>(Map.of());
        if (dashboardEnabled != null) {
            flowFlags.put(DASHBOARD_SERVICE_ENABLED, dashboardEnabled);
        }
        variables.putValue(FLOW_FLAGS, flowFlags);

        //complete the start business process
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        assertCompleteExternalTask(startBusiness,
                                   START_BUSINESS_TOPIC,
                                   START_BUSINESS_EVENT,
                                   START_BUSINESS_ACTIVITY,
                                   variables);

        //complete the notification to parties
        ExternalTask notification = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            notification,
            PROCESS_CASE_EVENT,
            "NOTIFY_EVENT",
            "CreateSDONotify",
            variables
        );

        if (Boolean.TRUE.equals(dashboardEnabled)) {
            ExternalTask dashboardTask = assertNextExternalTask(DASHBOARD_NOTIFICATION_TOPIC);
            assertCompleteExternalTask(dashboardTask,
                                       DASHBOARD_NOTIFICATION_TOPIC,
                                       DASHBOARD_NOTIFICATION_EVENT,
                                       GENERATE_DASHBOARD_NOTIFICATION_CREATE_SDO,
                                       variables
            );
        }

        //complete the Trigger and Update GA Location event
        ExternalTask triggerAndUpdateGenAppLocation = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            triggerAndUpdateGenAppLocation,
            PROCESS_CASE_EVENT,
            TRIGGER_UPDATE_GA_LOCATION,
            TRIGGER_UPDATE_GA_LOCATION_ACTIVITY_ID,
            variables
        );

        //end business process
        ExternalTask endBusinessProcess = assertNextExternalTask(END_BUSINESS_PROCESS);
        completeBusinessProcess(endBusinessProcess);

        assertNoExternalTasksLeft();
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    @NullSource
    void shouldSuccessfullyCompleteTakeCaseOfflineForLiPDefendant(Boolean dashboardEnabled) {
        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

        VariableMap variables = Variables.createVariables();
        Map<String, Object> flowFlags = new HashMap<>(Map.of(
            UNREPRESENTED_DEFENDANT_ONE, true,
            LIP_CASE, true
        ));
        if (dashboardEnabled != null) {
            flowFlags.put(DASHBOARD_SERVICE_ENABLED, dashboardEnabled);
        }
        variables.putValue(FLOW_FLAGS, flowFlags);

        //complete the start business process
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        assertCompleteExternalTask(startBusiness,
                                   START_BUSINESS_TOPIC,
                                   START_BUSINESS_EVENT,
                                   START_BUSINESS_ACTIVITY,
                                   variables);

        //complete the notification to parties
        ExternalTask partiesNotification = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            partiesNotification,
            PROCESS_CASE_EVENT,
            "NOTIFY_EVENT",
            "CreateSDONotify",
            variables
        );

        //Trigger Bulk Print
        ExternalTask sendSDOOrderToClaimant = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            sendSDOOrderToClaimant,
            PROCESS_CASE_EVENT,
            "SEND_SDO_ORDER_TO_LIP_CLAIMANT",
            "SendSDOToClaimantLIP",
            variables
        );

        ExternalTask sendSDOOrderToDefendant = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            sendSDOOrderToDefendant,
            PROCESS_CASE_EVENT,
            "SEND_SDO_ORDER_TO_LIP_DEFENDANT",
            "SendSDOToDefendantLIP",
            variables
        );

        if (Boolean.TRUE.equals(dashboardEnabled)) {
            ExternalTask dashboardTask = assertNextExternalTask(DASHBOARD_NOTIFICATION_TOPIC);
            assertCompleteExternalTask(dashboardTask,
                                       DASHBOARD_NOTIFICATION_TOPIC,
                                       DASHBOARD_NOTIFICATION_EVENT,
                                       GENERATE_DASHBOARD_NOTIFICATION_CREATE_SDO,
                                       variables
            );
        }

        //complete the Trigger and Update GA Location event
        ExternalTask triggerAndUpdateGenAppLocation = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            triggerAndUpdateGenAppLocation,
            PROCESS_CASE_EVENT,
            TRIGGER_UPDATE_GA_LOCATION,
            TRIGGER_UPDATE_GA_LOCATION_ACTIVITY_ID,
            variables
        );

        //end business process
        ExternalTask endBusinessProcess = assertNextExternalTask(END_BUSINESS_PROCESS);
        completeBusinessProcess(endBusinessProcess);

        assertNoExternalTasksLeft();
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    @NullSource
    void shouldSuccessfullyCompleteTakeCaseOfflineForLiPvLrClaim(Boolean dashboardEnabled) {
        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

        VariableMap variables = Variables.createVariables();
        Map<String, Object> flowFlags = new HashMap<>(Map.of(
            UNREPRESENTED_DEFENDANT_ONE, false,
            LIP_CASE, true
        ));
        if (dashboardEnabled != null) {
            flowFlags.put(DASHBOARD_SERVICE_ENABLED, dashboardEnabled);
        }
        variables.putValue(FLOW_FLAGS, flowFlags);

        //complete the start business process
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        assertCompleteExternalTask(startBusiness,
                                   START_BUSINESS_TOPIC,
                                   START_BUSINESS_EVENT,
                                   START_BUSINESS_ACTIVITY,
                                   variables);

        //complete the notification to parties
        ExternalTask partiesNotification = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            partiesNotification,
            PROCESS_CASE_EVENT,
            "NOTIFY_EVENT",
            "CreateSDONotify",
            variables
        );

        //Trigger Bulk Print
        ExternalTask sendSDOOrderToClaimant = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            sendSDOOrderToClaimant,
            PROCESS_CASE_EVENT,
            "SEND_SDO_ORDER_TO_LIP_CLAIMANT",
            "SendSDOToClaimantLIP",
            variables
        );

        if (Boolean.TRUE.equals(dashboardEnabled)) {
            ExternalTask dashboardTask = assertNextExternalTask(DASHBOARD_NOTIFICATION_TOPIC);
            assertCompleteExternalTask(dashboardTask,
                                       DASHBOARD_NOTIFICATION_TOPIC,
                                       DASHBOARD_NOTIFICATION_EVENT,
                                       GENERATE_DASHBOARD_NOTIFICATION_CREATE_SDO,
                                       variables
            );
        }

        //complete the Trigger and Update GA Location event
        ExternalTask triggerAndUpdateGenAppLocation = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            triggerAndUpdateGenAppLocation,
            PROCESS_CASE_EVENT,
            TRIGGER_UPDATE_GA_LOCATION,
            TRIGGER_UPDATE_GA_LOCATION_ACTIVITY_ID,
            variables
        );

        //end business process
        ExternalTask endBusinessProcess = assertNextExternalTask(END_BUSINESS_PROCESS);
        completeBusinessProcess(endBusinessProcess);

        assertNoExternalTasksLeft();
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    @NullSource
    void shouldSuccessfullyCompleteTakeCaseOfflineForLrvLrClaim(Boolean dashboardEnabled) {
        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

        VariableMap variables = Variables.createVariables();
        Map<String, Object> flowFlags = new HashMap<>(Map.of(
            UNREPRESENTED_DEFENDANT_ONE, false,
            LIP_CASE, false
        ));
        if (dashboardEnabled != null) {
            flowFlags.put(DASHBOARD_SERVICE_ENABLED, dashboardEnabled);
        }
        variables.putValue(FLOW_FLAGS, flowFlags);

        //complete the start business process
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        assertCompleteExternalTask(startBusiness,
                                   START_BUSINESS_TOPIC,
                                   START_BUSINESS_EVENT,
                                   START_BUSINESS_ACTIVITY,
                                   variables);

        //complete the notification to parties
        ExternalTask partiesNotification = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            partiesNotification,
            PROCESS_CASE_EVENT,
            "NOTIFY_EVENT",
            "CreateSDONotify",
            variables
        );

        if (Boolean.TRUE.equals(dashboardEnabled)) {
            ExternalTask dashboardTask = assertNextExternalTask(DASHBOARD_NOTIFICATION_TOPIC);
            assertCompleteExternalTask(dashboardTask,
                                       DASHBOARD_NOTIFICATION_TOPIC,
                                       DASHBOARD_NOTIFICATION_EVENT,
                                       GENERATE_DASHBOARD_NOTIFICATION_CREATE_SDO,
                                       variables
            );
        }

        //complete the Trigger and Update GA Location event
        ExternalTask triggerAndUpdateGenAppLocation = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            triggerAndUpdateGenAppLocation,
            PROCESS_CASE_EVENT,
            TRIGGER_UPDATE_GA_LOCATION,
            TRIGGER_UPDATE_GA_LOCATION_ACTIVITY_ID,
            variables
        );

        //end business process
        ExternalTask endBusinessProcess = assertNextExternalTask(END_BUSINESS_PROCESS);
        completeBusinessProcess(endBusinessProcess);

        assertNoExternalTasksLeft();
    }

    @ParameterizedTest
    @CsvSource({
        "true,false,true", "false,true,true", "true,true,true",
        "true,false,false", "false,true,false", "true,true,false",
        "true,false,", "false,true,", "true,true,"
    })
    void shouldSkipTasksWhenLanguagePreferenceWelsh(boolean claimantBilingual, boolean defendantBilingual, Boolean dashboardEnabled) {
        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

        VariableMap variables = Variables.createVariables();
        Map<String, Object> flowFlags = new HashMap<>(Map.of(
            WELSH_ENABLED, true,
            CLAIM_ISSUE_BILINGUAL, claimantBilingual,
            RESPONDENT_RESPONSE_LANGUAGE_IS_BILINGUAL, defendantBilingual
        ));
        if (dashboardEnabled != null) {
            flowFlags.put(DASHBOARD_SERVICE_ENABLED, dashboardEnabled);
        }
        variables.putValue(FLOW_FLAGS, flowFlags);

        //complete the start business process
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        assertCompleteExternalTask(startBusiness,
                                   START_BUSINESS_TOPIC,
                                   START_BUSINESS_EVENT,
                                   START_BUSINESS_ACTIVITY,
                                   variables);

        if (Boolean.TRUE.equals(dashboardEnabled)) {
            ExternalTask dashboardTask = assertNextExternalTask(DASHBOARD_NOTIFICATION_TOPIC);
            assertCompleteExternalTask(dashboardTask,
                                       DASHBOARD_NOTIFICATION_TOPIC,
                                       DASHBOARD_NOTIFICATION_EVENT,
                                       GENERATE_DASHBOARD_NOTIFICATION_CREATE_SDO,
                                       variables
            );
        }

        //complete the Trigger and Update GA Location event
        ExternalTask triggerAndUpdateGenAppLocation = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            triggerAndUpdateGenAppLocation,
            PROCESS_CASE_EVENT,
            TRIGGER_UPDATE_GA_LOCATION,
            TRIGGER_UPDATE_GA_LOCATION_ACTIVITY_ID,
            variables
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
