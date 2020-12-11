package uk.gov.hmcts.reform.unspec.bpmn;

import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

class ConfirmServiceTest extends BpmnBaseTest {

    public static final String GENERATE_CERTIFICATE_OF_SERVICE = "GENERATE_CERTIFICATE_OF_SERVICE";
    public static final String ACTIVITY_ID = "GenerateCertificateOfService";

    public ConfirmServiceTest() {
        super("confirm_service.bpmn", "CONFIRM_SERVICE_PROCESS_ID");
    }

    @Test
    void shouldSuccessfullyCompleteConfirmService() {
        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage("CONFIRM_SERVICE").getKey())
            .isEqualTo("CONFIRM_SERVICE_PROCESS_ID");

        //complete the start business process
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        assertCompleteExternalTask(startBusiness, START_BUSINESS_TOPIC, START_BUSINESS_EVENT, START_BUSINESS_ACTIVITY);

        //complete the document generation
        ExternalTask documentGeneration = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            documentGeneration,
            PROCESS_CASE_EVENT,
            GENERATE_CERTIFICATE_OF_SERVICE,
            ACTIVITY_ID
        );

        //end business process
        ExternalTask endBusinessProcess = assertNextExternalTask(END_BUSINESS_PROCESS);
        completeBusinessProcess(endBusinessProcess);

        assertNoExternalTasksLeft();
    }
}
