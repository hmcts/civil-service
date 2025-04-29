package uk.gov.hmcts.reform.civil.notification.handlers;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.claimdismissed.ClaimDismissedAllLegalRepsEmailGenerator;
import uk.gov.hmcts.reform.civil.notify.NotificationException;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.service.CaseTaskTrackingService;

import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CASEMAN_REF;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_LEGAL_ORG_NAME_SPEC;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.PARTY_REFERENCES;

class NotifierTest {

    public static final Long CASE_ID = 1594901956117591L;
    protected static final String TASK_ID = "TaskId";

    private NotificationService notificationService;

    private CaseTaskTrackingService caseTaskTrackingService;

    private AllLegalRepsEmailGenerator emailGenerator;

    private TestNotifier notifier;
    EmailDTO party1;
    EmailDTO party2;
    EmailDTO party3;
    Set<EmailDTO> expected;
    CaseData caseData;
    String taskId = "taskId";

    private class TestNotifier extends Notifier {
        public TestNotifier(NotificationService notificationService,
                            CaseTaskTrackingService caseTaskTrackingService,
                            AllLegalRepsEmailGenerator emailGenerator) {
            super(notificationService, caseTaskTrackingService, emailGenerator);
        }

        @Override
        public String getTaskId() {
            return TASK_ID;
        }
    }

    @BeforeEach
    void setUp() {
        notificationService = mock(NotificationService.class);
        caseTaskTrackingService = mock(CaseTaskTrackingService.class);
        emailGenerator = mock(ClaimDismissedAllLegalRepsEmailGenerator.class);

        notifier = new TestNotifier(notificationService, caseTaskTrackingService, emailGenerator);
        caseData = mock(CaseData.class);
        when(caseData.getCcdCaseReference()).thenReturn(CASE_ID);

        MockitoAnnotations.openMocks(this);
        party1 = EmailDTO.builder()
            .targetEmail("applicantsolicitor@example.com")
            .emailTemplate("template-id")
            .parameters(getNotificationDataMap())
            .reference("claim-dismissed-applicant-notification-000DC001")
            .build();

        party2 = EmailDTO.builder()
            .targetEmail("respondentsolicitor@example.com")
            .emailTemplate("template-id")
            .parameters(getNotificationDataMap())
            .reference("claim-dismissed-respondent-notification-000DC001")
            .build();

        party3 = EmailDTO.builder()
            .targetEmail("respondentsolicitor2@example.com")
            .emailTemplate("template-id")
            .parameters(getNotificationDataMap())
            .reference("claim-dismissed-respondent-notification-000DC001")
            .build();
        expected = Set.of(party1, party2, party3);
    }

    @Test
    void shouldNotifyPartiesSuccessfully() {
        final Set<EmailDTO> expected = getEmailDTOS();

        when(emailGenerator.getPartiesToNotify(caseData, taskId)).thenReturn(expected);

        notifier.notifyParties(caseData, "eventId", "taskId");

        verify(emailGenerator, times(1)).getPartiesToNotify(caseData, taskId);
        verify(notificationService, times(3)).sendMail(anyString(), anyString(), anyMap(), anyString());
    }

    @Test
    void shouldHandleErrorsWhenNotifyPartiesAndContinueToNextEmail() {
        final Set<EmailDTO> expected = getEmailDTOS();

        when(emailGenerator.getPartiesToNotify(caseData, taskId)).thenReturn(expected);
        doThrow(new NotificationException(new Exception("Notification Service error null"))).when(notificationService)
            .sendMail(party2.getTargetEmail(), party2.getEmailTemplate(), party2.getParameters(), party2.getReference());

        notifier.notifyParties(caseData, "eventId", "taskId");

        verify(caseTaskTrackingService, times(1))
            .trackCaseTask(
                CASE_ID.toString(),
                "eventId",
                "taskId",
                Map.of("Errors", "[java.lang.Exception: Notification Service error null]")
            );
        verify(emailGenerator, times(1)).getPartiesToNotify(caseData, taskId);
        verify(notificationService, times(3)).sendMail(anyString(), anyString(), anyMap(), anyString());
    }

    @NotNull
    private Set<EmailDTO> getEmailDTOS() {
        return expected;
    }

    @Test
    void shouldReturnCorrectTaskId() {
        String taskId = notifier.getTaskId();
        assertThat(taskId).isEqualTo(TASK_ID);
    }

    @NotNull
    private Map<String, String> getNotificationDataMap() {
        return Map.of(
            CLAIM_REFERENCE_NUMBER, CASE_ID.toString(),
            PARTY_REFERENCES, "Claimant reference: 12345 - Defendant reference: 6789",
            CLAIM_LEGAL_ORG_NAME_SPEC, "org name",
            CASEMAN_REF, "000DC001"
        );
    }
}
