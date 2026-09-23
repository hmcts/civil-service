package uk.gov.hmcts.reform.civil.bpmn;

import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

class ClaimSettledLetterNotificationTest extends BpmnBaseTest {

    public static final String MESSAGE_NAME = "UNSPEC_CLAIM_SETTLED_LETTER_NOTIFICATION";
    public static final String PROCESS_ID = "UNSPEC_CLAIM_SETTLED_LETTER_NOTIFICATION_ID";
    public static final String SEND_CLAIM_SETTLED_LETTER_TO_LIP_DEFENDANT1_ID = "SendLetterClaimSettledDefendantLiP";
    public static final String SEND_CLAIM_SETTLED_LETTER_TO_LIP_DEFENDANT1_EVENT = "SEND_UNSPEC_CLAIM_SETTLED_LETTER_TO_LIP_DEFENDANT1";
    public static final String NOTIFY_EVENT_ID = "UnspecClaimSettledNotifier";
    public static final String NOTIFY_EVENT_EVENT = "NOTIFY_EVENT";
    public static final String CLAIM_SETTLED_LETTER_REQUIRED = "isClaimSettledLetterRequired";

    public ClaimSettledLetterNotificationTest() {
        super("unspec_claim_settled_letter_notification.bpmn", PROCESS_ID);
    }

    @ParameterizedTest
    @CsvSource(value = {
        // isLiPDefendant, isClaimSettledLetterRequired (null = not set), expectLetterEvent
        "true, true, true",
        "true, false, false",
        "false, true, false",
        "false, false, false",
        // variable not set (e.g. BPMN deployed before civil-service): keep original behaviour
        "true, null, true",
        "false, null, false"
    }, nullValues = "null")
    void shouldSuccessfullyComplete(boolean isLiPDefendant, Boolean isClaimSettledLetterRequired, boolean expectLetterEvent) {
        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

        VariableMap variables = Variables.createVariables();
        variables.put(FLOW_FLAGS, Map.of(UNREPRESENTED_DEFENDANT_ONE, isLiPDefendant));
        if (isClaimSettledLetterRequired != null) {
            variables.put(CLAIM_SETTLED_LETTER_REQUIRED, isClaimSettledLetterRequired);
        }

        //complete the start business process
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        assertCompleteExternalTask(
            startBusiness,
            START_BUSINESS_TOPIC,
            START_BUSINESS_EVENT,
            START_BUSINESS_ACTIVITY,
            variables
        );

        ExternalTask nextTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        if (expectLetterEvent) {
            //complete the letter generation task
            assertCompleteExternalTask(
                nextTask,
                PROCESS_CASE_EVENT,
                SEND_CLAIM_SETTLED_LETTER_TO_LIP_DEFENDANT1_EVENT,
                SEND_CLAIM_SETTLED_LETTER_TO_LIP_DEFENDANT1_ID,
                variables
            );
            nextTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        }

        //complete the notify parties task
        assertCompleteExternalTask(
            nextTask,
            PROCESS_CASE_EVENT,
            NOTIFY_EVENT_EVENT,
            NOTIFY_EVENT_ID,
            variables
        );

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
