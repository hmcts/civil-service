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

class EnterBreathingSpaceSpecTest extends BpmnBaseTest {

    private static final String MESSAGE_NAME = "ENTER_BREATHING_SPACE_SPEC";
    private static final String PROCESS_ID = "ENTER_BREATHING_SPACE_SPEC";

    EnterBreathingSpaceSpecTest() {
        super("enter_breathing_space_spec.bpmn", PROCESS_ID);
    }

    @ParameterizedTest(name = "{0}")
    @CsvSource({
        "Single respondent representative, true,  false, false, false",
        "Two respondent representatives,   false, true,  false, false",
        "LiP claimant v LiP defendant,     false, false, true,  true",
        "LiP claimant v LR defendant,      true,  false, true,  false",
        "LR claimant v LiP defendant,      false, false, false, true"
    })
    void shouldNotifyAllPartiesInSingleNotifyPartiesTask_whenBreathingSpaceEntered(String scenario,
                                                                                   boolean oneRespondentRepresentative,
                                                                                   boolean twoRespondentRepresentatives,
                                                                                   boolean lipCase,
                                                                                   boolean unrepresentedDefendantOne) {
        VariableMap variables = flowFlagVariables(
            oneRespondentRepresentative, twoRespondentRepresentatives, lipCase, unrepresentedDefendantOne);

        assertFalse(processInstance.isEnded());
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

        startBusinessProcess(variables);
        assertCompleteExternalTask(
            assertNextExternalTask(PROCESS_CASE_EVENT), PROCESS_CASE_EVENT,
            "NOTIFY_EVENT", "BreathingSpaceEnterNotifyParties", variables
        );

        assertEnd();
    }

    @Test
    void shouldAbort_whenStartBusinessProcessThrowsAnError() {
        assertFalse(processInstance.isEnded());
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        assertFailExternalTask(startBusiness, START_BUSINESS_TOPIC, START_BUSINESS_EVENT, START_BUSINESS_ACTIVITY);

        assertNoExternalTasksLeft();
    }

    private void assertEnd() {
        ExternalTask endBusinessProcess = assertNextExternalTask(END_BUSINESS_PROCESS);
        completeBusinessProcess(endBusinessProcess);

        assertNoExternalTasksLeft();
    }

    private VariableMap flowFlagVariables(boolean oneRespondentRepresentative,
                                          boolean twoRespondentRepresentatives,
                                          boolean lipCase,
                                          boolean unrepresentedDefendantOne) {
        Map<String, Object> flags = new HashMap<>();
        flags.put(ONE_RESPONDENT_REPRESENTATIVE, oneRespondentRepresentative);
        flags.put(TWO_RESPONDENT_REPRESENTATIVES, twoRespondentRepresentatives);
        flags.put(LIP_CASE, lipCase);
        flags.put(UNREPRESENTED_DEFENDANT_ONE, unrepresentedDefendantOne);
        VariableMap variables = Variables.createVariables();
        variables.putValue(FLOW_FLAGS, flags);
        return variables;
    }
}
