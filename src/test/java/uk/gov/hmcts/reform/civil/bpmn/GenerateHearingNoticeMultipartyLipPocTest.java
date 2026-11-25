package uk.gov.hmcts.reform.civil.bpmn;

import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class GenerateHearingNoticeMultipartyLipPocTest extends BpmnBaseTest {

    private static final String DIAGRAM_PATH = "camunda/%s";
    private static final String PROCESS_ID = "NOTIFY_HEARING_PARTIES";
    private static final String START_BUSINESS_TOPIC = "START_HEARING_NOTICE_BUSINESS_PROCESS";
    private static final String START_BUSINESS_EVENT = "START_BUSINESS_PROCESS";
    private static final String START_BUSINESS_ACTIVITY = "StartHearingNoticeBusinessProcessTaskId";
    private static final String GENERATE_HEARING_NOTICE_HMC = "GENERATE_HEARING_NOTICE_HMC";
    private static final String GENERATE_HEARING_NOTICE_HMC_ACTIVITY_ID = "GenerateHearingNotice";
    private static final String NOTIFIER_ACTIVITY_ID = "HearingNoticeGeneratorHMCNotifier";
    private static final String DEFENDANT_PRINT_EVENT = "SEND_HEARING_TO_LIP_DEFENDANT_HMC";

    GenerateHearingNoticeMultipartyLipPocTest() {
        super("generate_hearing_notice.bpmn", PROCESS_ID);
    }

    @BeforeEach
    void setup() {
        startBusinessProcessDeployment = engine.getRepositoryService()
            .createDeployment()
            .addClasspathResource(String.format(DIAGRAM_PATH, "start_hearing_notice_business_process.bpmn"))
            .deploy();
        endBusinessProcessDeployment = engine.getRepositoryService()
            .createDeployment()
            .addClasspathResource(String.format(DIAGRAM_PATH, "end_business_process.bpmn"))
            .deploy();
        deployment = engine.getRepositoryService()
            .createDeployment()
            .addClasspathResource(String.format(DIAGRAM_PATH, bpmnFileName))
            .deploy();
        processInstance = engine.getRuntimeService().startProcessInstanceByKey(processId);
    }

    @AfterEach
    void deleteUnfinishedProcessInstances() {
        engine.getRuntimeService().createProcessInstanceQuery().list()
            .forEach(instance -> engine.getRuntimeService().deleteProcessInstance(instance.getId(), "OCCC-631 POC"));
    }

    @ParameterizedTest(name = "{0}")
    @CsvSource({
        "A LR v 2LiP, true, true",
        "B LR v LiP(D1) + LR(D2), true, false",
        "C LR v LR(D1) + LiP(D2), false, true"
    })
    void shouldPostHearingNoticeToEveryLipDefendant(String variant, boolean defendantOneLip, boolean defendantTwoLip) {
        VariableMap variables = Variables.createVariables();
        variables.put("flowFlags", Map.of(
            "UNREPRESENTED_DEFENDANT_ONE", defendantOneLip,
            "UNREPRESENTED_DEFENDANT_TWO", defendantTwoLip,
            "TWO_RESPONDENT_REPRESENTATIVES", true,
            LIP_CASE, false,
            DASHBOARD_SERVICE_ENABLED, true));
        variables.put("caseState", "CASE_PROGRESSION");

        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        assertCompleteExternalTask(startBusiness, START_BUSINESS_TOPIC, START_BUSINESS_EVENT,
                                   START_BUSINESS_ACTIVITY, variables);

        ExternalTask generateNotice = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(generateNotice, PROCESS_CASE_EVENT, GENERATE_HEARING_NOTICE_HMC,
                                   GENERATE_HEARING_NOTICE_HMC_ACTIVITY_ID, variables);

        List<String> defendantPostActivities = new ArrayList<>();
        ExternalTask next = assertNextExternalTask(PROCESS_CASE_EVENT);
        while (!NOTIFIER_ACTIVITY_ID.equals(next.getActivityId())) {
            defendantPostActivities.add(next.getActivityId());
            assertCompleteExternalTask(next, PROCESS_CASE_EVENT, DEFENDANT_PRINT_EVENT, next.getActivityId(), variables);
            next = assertNextExternalTask(PROCESS_CASE_EVENT);
        }

        int lipDefendants = (defendantOneLip ? 1 : 0) + (defendantTwoLip ? 1 : 0);
        assertThat(defendantPostActivities)
            .as("hearing notice post tasks for %s", variant)
            .hasSize(lipDefendants);
    }
}
