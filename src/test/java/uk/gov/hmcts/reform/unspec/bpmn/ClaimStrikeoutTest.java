package uk.gov.hmcts.reform.unspec.bpmn;

import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

class ClaimStrikeoutTest extends BpmnBaseTest {

    public static final String NOTIFY_RESPONDENT_SOLICITOR_1 = "NOTIFY_RESPONDENT_SOLICITOR1_CASE_STRIKE_OUT";
    public static final String RESPONDENT_SOLICITOR_1_ACTIVITY_ID = "ClaimStrikeoutNotifyRespondentSolicitor1";
    public static final String NOTIFY_APPLICANT_SOLICITOR_1 = "NOTIFY_APPLICANT_SOLICITOR1_CASE_STRIKE_OUT";
    public static final String APPLICANT_SOLICITOR_1_ACTIVITY_ID = "ClaimStrikeoutNotifyApplicantSolicitor1";

    public ClaimStrikeoutTest() {
        super("claim_strikeout.bpmn", "CLAIM_STRIKEOUT_PROCESS_ID");
    }

    @Test
    void shouldSuccessfullyCompleteStrikeoutClaim() {
        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage("MOVE_CLAIM_TO_STRUCK_OUT").getKey())
            .isEqualTo("CLAIM_STRIKEOUT_PROCESS_ID");

        //complete the start business process
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        assertCompleteExternalTask(startBusiness, START_BUSINESS_TOPIC, START_BUSINESS_EVENT, START_BUSINESS_ACTIVITY);

        //complete the notification to respondent solicitor 1
        ExternalTask respondentNotification = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            respondentNotification,
            PROCESS_CASE_EVENT,
            NOTIFY_RESPONDENT_SOLICITOR_1,
            RESPONDENT_SOLICITOR_1_ACTIVITY_ID
        );

        //complete the notification to applicant solicitor 1
        ExternalTask applicantNotification = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            applicantNotification,
            PROCESS_CASE_EVENT,
            NOTIFY_APPLICANT_SOLICITOR_1,
            APPLICANT_SOLICITOR_1_ACTIVITY_ID
        );

        //end business process
        ExternalTask endBusinessProcess = assertNextExternalTask(END_BUSINESS_PROCESS);
        completeBusinessProcess(endBusinessProcess);

        assertNoExternalTasksLeft();
    }
}
