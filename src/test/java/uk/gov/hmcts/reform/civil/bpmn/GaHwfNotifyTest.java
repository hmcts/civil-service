package uk.gov.hmcts.reform.civil.bpmn;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static uk.gov.hmcts.reform.civil.bpmn.BpmnBaseTest.DASHBOARD_NOTIFICATION_EVENT;

import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class GaHwfNotifyTest extends BpmnBaseGASpecTest {

    private static final String FILE_NAME = "notify_ga_applicant_hwf_outcome.bpmn";
    private static final String MESSAGE_NAME = "NOTIFY_APPLICANT_LIP_HWF";
    private static final String PROCESS_ID = "GA_NOTIFY_HWF";
    private static final String ACTIVITY_ID = "NotifyGaApplicantHwfOutcome";
    private static final String START_BUSINESS_TOPIC = "START_GA_BUSINESS_PROCESS";
    // the external task topic, not the case event. Kept distinct deliberately: the two were
    // conflated before this migration, which is what made the assertion below wrong.
    public static final String NOTIFY_TOPIC = "applicationProcessCaseEventGASpec";
    private static final String NOTIFY_CASE_EVENT = "NOTIFY_EVENT";
    public static final String NOTIFY_EVENT_DASHBOARD = "processExternalCaseEventGASpec";
    private static final String CREATE_DASHBOARD_NOTIFICATION_HWF_OUTCOME_ACTIVITY_ID
        = "GenerateDashboardNotificationsGaHwfOutcome";

    public GaHwfNotifyTest() {
        super(FILE_NAME, PROCESS_ID);
    }

    @BeforeEach
    void setup() {
        //deploy process
        startBusinessProcessDeployment = engine.getRepositoryService()
                .createDeployment()
                .addClasspathResource(String.format(DIAGRAM_PATH,
                        "start_business_process_in_general_application.bpmn"))
                .deploy();
        endBusinessProcessDeployment = engine.getRepositoryService()
                .createDeployment()
                .addClasspathResource(String.format(DIAGRAM_PATH, "end_ga_hwf_notify_process.bpmn"))
                .deploy();
        deployment = engine.getRepositoryService()
                .createDeployment()
                .addClasspathResource(String.format(DIAGRAM_PATH, bpmnFileName))
                .deploy();
        processInstance = engine.getRuntimeService().startProcessInstanceByKey(processId);
    }

    @Test
    void shouldSuccessfullyCompleteNotifyApplicant_whenCalled() {
        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

        //complete the start business process
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        assertCompleteExternalTask(
                startBusiness,
                START_BUSINESS_TOPIC,
                START_GA_BUSINESS_EVENT,
                START_GA_BUSINESS_ACTIVITY
        );
        ExternalTask notifyTask = assertNextExternalTask(NOTIFY_TOPIC);
        assertCompleteExternalTask(
                notifyTask,
                NOTIFY_TOPIC,
                NOTIFY_CASE_EVENT,
                ACTIVITY_ID
        );
        ExternalTask notifyTaskDashboard = assertNextExternalTask(NOTIFY_EVENT_DASHBOARD);
        assertCompleteExternalTask(
            notifyTaskDashboard,
            NOTIFY_EVENT_DASHBOARD,
            DASHBOARD_NOTIFICATION_EVENT,
            CREATE_DASHBOARD_NOTIFICATION_HWF_OUTCOME_ACTIVITY_ID
        );

        //end business process
        ExternalTask endBusinessProcess = assertNextExternalTask(END_GA_HWF_NOTIFY_PROCESS);
        completeGaBusinessProcess(endBusinessProcess);

        assertNoExternalTasksLeft();
    }
}
