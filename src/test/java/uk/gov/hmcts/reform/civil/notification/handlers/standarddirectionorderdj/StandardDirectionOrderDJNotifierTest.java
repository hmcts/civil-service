package uk.gov.hmcts.reform.civil.notification.handlers.standarddirectionorderdj;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.EmailDTO;
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

class StandardDirectionOrderDJNotifierTest {

    private static final Long CASE_ID = 1594901956117591L;
    private static final String TASK_ID = "STANDARD_DIRECTION_ORDER_DJ_NOTIFY_PARTIES";
    private static final String EVENT_ID = "eventId";

    @Mock
    private NotificationService notificationService;

    @Mock
    private CaseTaskTrackingService caseTaskTrackingService;

    @Mock
    private StandardDirectionOrderDJAllPartiesEmailGenerator emailGenerator;

    private StandardDirectionOrderDJNotifier notifier;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        notifier = new StandardDirectionOrderDJNotifier(
            notificationService,
            caseTaskTrackingService,
            emailGenerator
        );
    }

    @Test
    void shouldReturnCorrectTaskId() {
        String taskId = notifier.getTaskId();
        assertThat(taskId).isEqualTo("STANDARD_DIRECTION_ORDER_DJ_NOTIFY_PARTIES");
    }

    @Test
    void shouldNotifyPartiesSuccessfully() {
        CaseData caseData = mock(CaseData.class);
        when(caseData.getCcdCaseReference()).thenReturn(CASE_ID);

        EmailDTO party1 = createEmailDTO("applicant@example.com", "template-id", "ref-1");
        EmailDTO party2 = createEmailDTO("respondent1@example.com", "template-id", "ref-2");
        Set<EmailDTO> emailDTOs = Set.of(party1, party2);

        when(emailGenerator.getPartiesToNotify(caseData, TASK_ID)).thenReturn(emailDTOs);

        String result = notifier.notifyParties(caseData, EVENT_ID, TASK_ID);

        assertThat(result)
            .contains("Attempted:")
            .contains("applicant@example.com")
            .contains("respondent1@example.com");
        verify(emailGenerator, times(1)).getPartiesToNotify(caseData, TASK_ID);
        verify(notificationService, times(2)).sendMail(anyString(), anyString(), anyMap(), anyString());
    }

    @Test
    void shouldHandleErrorsWhenNotifyingParties() {
        CaseData caseData = mock(CaseData.class);
        when(caseData.getCcdCaseReference()).thenReturn(CASE_ID);

        EmailDTO party1 = createEmailDTO("applicant@example.com", "template-id", "ref-1");
        EmailDTO party2 = createEmailDTO("respondent1@example.com", "template-id", "ref-2");
        Set<EmailDTO> emailDTOs = Set.of(party1, party2);

        when(emailGenerator.getPartiesToNotify(caseData, TASK_ID)).thenReturn(emailDTOs);
        doThrow(new NotificationException(new Exception("Notification Service error")))
            .when(notificationService)
            .sendMail(party2.getTargetEmail(), party2.getEmailTemplate(), party2.getParameters(), party2.getReference());

        String result = notifier.notifyParties(caseData, EVENT_ID, TASK_ID);

        assertThat(result).contains("Errors:");
        verify(caseTaskTrackingService, times(1)).trackCaseTask(
            anyString(),
            anyString(),
            anyString(),
            anyMap()
        );
        verify(emailGenerator, times(1)).getPartiesToNotify(caseData, TASK_ID);
        verify(notificationService, times(2)).sendMail(anyString(), anyString(), anyMap(), anyString());
    }

    @Test
    void shouldNotifyNoPartiesWhenListIsEmpty() {
        CaseData caseData = mock(CaseData.class);
        when(caseData.getCcdCaseReference()).thenReturn(CASE_ID);

        when(emailGenerator.getPartiesToNotify(caseData, TASK_ID)).thenReturn(Set.of());

        String result = notifier.notifyParties(caseData, EVENT_ID, TASK_ID);

        assertThat(result)
            .contains("Attempted:")
            .contains("Errors:");
        verify(emailGenerator, times(1)).getPartiesToNotify(caseData, TASK_ID);
        verify(notificationService, times(0)).sendMail(anyString(), anyString(), anyMap(), anyString());
    }

    private EmailDTO createEmailDTO(String email, String template, String reference) {
        EmailDTO dto = new EmailDTO();
        dto.setTargetEmail(email);
        dto.setEmailTemplate(template);
        dto.setReference(reference);
        dto.setParameters(Map.of("claimReferenceNumber", CASE_ID.toString()));
        return dto;
    }
}
