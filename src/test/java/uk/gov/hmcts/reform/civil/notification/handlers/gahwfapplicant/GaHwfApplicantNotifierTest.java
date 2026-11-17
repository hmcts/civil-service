package uk.gov.hmcts.reform.civil.notification.handlers.gahwfapplicant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.EmailDTO;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.service.CaseTaskTrackingService;

import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * {@link GaHwfApplicantNotifier} is the first notifier the GA dispatch path resolves, and its task
 * id is the only thing tying it to {@code notify_ga_applicant_hwf_outcome.bpmn}. A wrong or renamed
 * id does not fail to compile: it fails at runtime as "no notifier found" on a live case, so it is
 * asserted here rather than left to the BPMN test alone.
 */
class GaHwfApplicantNotifierTest {

    private static final Long CASE_ID = 1234567890123456L;
    private static final String EVENT_ID = "NOTIFY_EVENT";

    private NotificationService notificationService;
    private GaHwfApplicantEmailGenerator emailGenerator;
    private GaHwfApplicantNotifier notifier;

    @SuppressWarnings("unchecked")
    @BeforeEach
    void setUp() {
        notificationService = mock(NotificationService.class);
        emailGenerator = mock(GaHwfApplicantEmailGenerator.class);
        notifier = new GaHwfApplicantNotifier(
            notificationService, mock(CaseTaskTrackingService.class), emailGenerator);
    }

    @Test
    void shouldReturnTheActivityIdTheMigratedBpmnRaises() {
        assertThat(notifier.getTaskId()).isEqualTo("NotifyGaApplicantHwfOutcome");
    }

    @Test
    void shouldSendTheHelpWithFeesEmailToTheApplicantSolicitor() {
        GeneralApplicationCaseData caseData = mock(GeneralApplicationCaseData.class);
        when(caseData.getCcdCaseReference()).thenReturn(CASE_ID);

        EmailDTO applicant = new EmailDTO();
        applicant.setTargetEmail("applicant-solicitor@example.com");
        applicant.setEmailTemplate("hwf-outcome-template");
        applicant.setReference("1594901956117591");
        applicant.setParameters(Map.of("claimReferenceNumber", String.valueOf(CASE_ID)));
        when(emailGenerator.getPartiesToNotify(caseData, GaHwfApplicantNotifier.TASK_ID))
            .thenReturn(Set.of(applicant));

        String summary = notifier.notifyParties(caseData, EVENT_ID, GaHwfApplicantNotifier.TASK_ID);

        verify(notificationService).sendMail(
            eq("applicant-solicitor@example.com"), eq("hwf-outcome-template"), anyMap(),
            eq("1594901956117591"));
        assertThat(summary)
            .contains("applicant-solicitor@example.com : 1594901956117591 : hwf-outcome-template")
            .endsWith("|| Errors: ");
    }
}
