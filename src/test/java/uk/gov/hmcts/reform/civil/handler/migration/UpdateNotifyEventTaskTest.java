package uk.gov.hmcts.reform.civil.handler.migration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.bulkupdate.csv.NotificationCaseReference;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.Notifier;
import uk.gov.hmcts.reform.civil.notification.handlers.NotifierFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UpdateNotifyEventTaskTest {

    private NotifierFactory notifierFactory;
    private Notifier notifier;
    private UpdateNotifyEventTask task;

    @BeforeEach
    void setUp() {
        notifierFactory = mock(NotifierFactory.class);
        notifier = mock(Notifier.class);
        task = new UpdateNotifyEventTask(notifierFactory);
    }

    @Test
    void shouldReturnCorrectTaskName() {
        assertEquals("UpdateNotifyEventTask", task.getTaskName());
    }

    @Test
    void shouldReturnCorrectEventSummary() {
        assertEquals("Run notify event via migration task", task.getEventSummary());
    }

    @Test
    void shouldReturnCorrectEventDescription() {
        assertEquals("This task sends failed notifications on the case", task.getEventDescription());
    }

    @Test
    void shouldThrowExceptionWhenCaseDataIsNull() {
        NotificationCaseReference reference = new NotificationCaseReference();
        reference.setCamundaProcessIdentifier("process-123");

        assertThrows(IllegalArgumentException.class, () -> task.migrateCaseData(null, reference));
    }

    @Test
    void shouldThrowExceptionWhenCaseReferenceIsNull() {
        CaseData caseData =  mock(CaseData.class);

        assertThrows(IllegalArgumentException.class, () -> task.migrateCaseData(caseData, null));
    }

    @Test
    void shouldThrowExceptionWhenCamundaProcessIdentifierIsNull() {
        CaseData caseData =  mock(CaseData.class);
        NotificationCaseReference reference = new NotificationCaseReference(); // camundaProcessIdentifier not set

        assertThrows(IllegalArgumentException.class, () -> task.migrateCaseData(caseData, reference));
    }

    @Test
    void shouldNotifyAndSetNotificationSummary() {
        CaseData caseData =  mock(CaseData.class);
        NotificationCaseReference reference = new NotificationCaseReference();
        reference.setCamundaProcessIdentifier("process-123");

        when(notifierFactory.getNotifier("process-123")).thenReturn(notifier);
        when(notifier.notifyParties(caseData, "NOTIFY_EVENT", "process-123"))
            .thenReturn("Notification sent successfully");

        CaseData result = task.migrateCaseData(caseData, reference);

        assertNotNull(result);

        verify(notifierFactory).getNotifier("process-123");
        verify(notifier).notifyParties(caseData, "NOTIFY_EVENT", "process-123");
    }
}
