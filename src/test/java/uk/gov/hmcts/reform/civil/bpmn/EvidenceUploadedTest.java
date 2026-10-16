package uk.gov.hmcts.reform.civil.bpmn;

import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskIds;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class EvidenceUploadedTest extends BpmnBaseTest {

    private static final String MESSAGE_NAME = "EVIDENCE_UPLOADED";
    private static final String PROCESS_ID = "EVIDENCE_UPLOADED_PROCESS_ID";

    public EvidenceUploadedTest() {
        super("evidence_uploaded.bpmn", "EVIDENCE_UPLOADED_PROCESS_ID");
    }

    @Test
    void shouldSuccessfullyCompleteEvidenceUpload_whenCalled() {
        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

        //complete the start business process
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        assertCompleteExternalTask(startBusiness,
                                   START_BUSINESS_TOPIC,
                                   START_BUSINESS_EVENT,
                                   START_BUSINESS_ACTIVITY);

        ExternalTask notification = assertNextExternalTask(DASHBOARD_NOTIFICATION_TOPIC);
        assertCompleteExternalTask(notification, DASHBOARD_NOTIFICATION_TOPIC,
            DASHBOARD_NOTIFICATION_EVENT, DashboardTaskIds.EVIDENCE_UPLOADED);

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
