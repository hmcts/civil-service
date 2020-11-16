package uk.gov.hmcts.reform.unspec.bpmn;

import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

class ClaimantResponseTest extends BpmnBaseTest {

    private static final String RESPONDENT_SOLICITOR_1
        = "NOTIFY_RESPONDENT_SOLICITOR1_FOR_CASE_TRANSFERRED_TO_LOCAL_COURT";
    private static final String RESPONDENT_ACTIVITY = "ClaimantResponseNotifyRespondentSolicitor1";
    private static final String APPLICANT_SOLICITOR_1
        = "NOTIFY_APPLICANT_SOLICITOR1_FOR_CASE_TRANSFERRED_TO_LOCAL_COURT";
    private static final String APPLICANT_ACTIVITY = "ClaimantResponseNotifyApplicantSolicitor1";

    public ClaimantResponseTest() {
        super("claimant_response.bpmn", "CLAIMANT_RESPONSE_PROCESS_ID");
    }

    @Test
    void shouldSuccessfullyCompleteClaimantResponse() {
        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage("CLAIMANT_RESPONSE").getKey())
            .isEqualTo("CLAIMANT_RESPONSE_PROCESS_ID");

        //complete the start business process
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        assertCompleteExternalTask(startBusiness, START_BUSINESS_TOPIC, START_BUSINESS_EVENT, START_BUSINESS_ACTIVITY);

        //complete the notification
        ExternalTask forRespondent = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(forRespondent, PROCESS_CASE_EVENT, RESPONDENT_SOLICITOR_1, RESPONDENT_ACTIVITY);

        //complete the notification
        ExternalTask forApplicant = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(forApplicant, PROCESS_CASE_EVENT, APPLICANT_SOLICITOR_1, APPLICANT_ACTIVITY);

        //end business process
        ExternalTask endBusinessProcess = assertNextExternalTask(END_BUSINESS_PROCESS);
        completeBusinessProcess(endBusinessProcess);

        assertNoExternalTasksLeft();
    }

}
