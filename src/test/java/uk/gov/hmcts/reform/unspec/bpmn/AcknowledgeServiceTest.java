package uk.gov.hmcts.reform.unspec.bpmn;

import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

class AcknowledgeServiceTest extends BpmnBaseTest {

    public static final String NOTIFY_APPLICANT_SOLICITOR_1 = "NOTIFY_APPLICANT_SOLICITOR1_FOR_SERVICE_ACKNOWLEDGEMENT";
    public static final String GENERATE_ACKNOWLEDGEMENT_OF_SERVICE = "GENERATE_ACKNOWLEDGEMENT_OF_SERVICE";
    public static final String NOTIFICATION_ACTIVITY_ID = "AcknowledgeServiceNotifyApplicantSolicitor1";
    public static final String GENERATE_CERTIFICATE_ACTIVITY_ID = "AcknowledgeServiceGenerateAcknowledgementOfService";

    public AcknowledgeServiceTest() {
        super("acknowledge_service.bpmn", "ACKNOWLEDGE_SERVICE_PROCESS_ID");
    }

    @Test
    void shouldSuccessfullyCompleteAcknowledgeService() {
        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage("ACKNOWLEDGE_SERVICE").getKey())
            .isEqualTo("ACKNOWLEDGE_SERVICE_PROCESS_ID");

        //complete the start business process
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        assertCompleteExternalTask(startBusiness, START_BUSINESS_TOPIC, START_BUSINESS_EVENT, START_BUSINESS_ACTIVITY);

        //complete the certificate generation
        ExternalTask certificateGeneration = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(certificateGeneration, PROCESS_CASE_EVENT, GENERATE_ACKNOWLEDGEMENT_OF_SERVICE,
                                   GENERATE_CERTIFICATE_ACTIVITY_ID);

        //complete the notification to applicant
        ExternalTask notification = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(notification, PROCESS_CASE_EVENT, NOTIFY_APPLICANT_SOLICITOR_1,
                                   NOTIFICATION_ACTIVITY_ID);

        //end business process
        ExternalTask endBusinessProcess = assertNextExternalTask(END_BUSINESS_PROCESS);
        completeBusinessProcess(endBusinessProcess);

        assertNoExternalTasksLeft();
    }
}
