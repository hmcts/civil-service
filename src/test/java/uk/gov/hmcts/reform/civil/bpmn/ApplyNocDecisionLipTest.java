package uk.gov.hmcts.reform.civil.bpmn;

import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;

public class ApplyNocDecisionLipTest extends BpmnBaseTest {

    public static final String MESSAGE_NAME = "APPLY_NOC_DECISION_LIP";
    public static final String PROCESS_ID = "APPLY_NOC_DECISION_LIP";

    public ApplyNocDecisionLipTest() {
        super("apply_noc_decision_lip.bpmn", PROCESS_ID);
    }

    @Test
    void shouldRunProcess() {
        VariableMap variables = Variables.createVariables();

        //assert process has started
        assertFalse(processInstance.isEnded());
        //complete the start business process
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        assertCompleteExternalTask(
            startBusiness,
            START_BUSINESS_TOPIC,
            START_BUSINESS_EVENT,
            START_BUSINESS_ACTIVITY,
            variables
        );

        //complete updating case details
        ExternalTask updateCaseDetails = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            updateCaseDetails,
            PROCESS_CASE_EVENT,
            "UPDATE_CASE_DETAILS_AFTER_NOC",
            "UpdateCaseDetailsAfterNoC"
        );

        //update GA language flag
        ExternalTask updateGeneralApps = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            updateGeneralApps,
            PROCESS_CASE_EVENT,
            "UPDATE_GA_LANGUAGE_PREFERENCE",
            "UpdateGenAppLanguagePreference"
        );

        //complete notify claimant
        ExternalTask notifyParies = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            notifyParies,
            PROCESS_CASE_EVENT,
            "NOTIFY_EVENT",
            "ClaimantLipRepresentedWithNoCNotifier"
        );

        //update main claim language flag
        ExternalTask updateLanguage = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            updateLanguage,
            PROCESS_CASE_EVENT,
            "RESET_LANGUAGE_PREFERENCE",
            "ResetLanguagePreferenceAfterNoC"
        );

        //end business process
        ExternalTask endBusinessProcess = assertNextExternalTask(END_BUSINESS_PROCESS);
        completeBusinessProcess(endBusinessProcess);

        assertNoExternalTasksLeft();
    }
}
