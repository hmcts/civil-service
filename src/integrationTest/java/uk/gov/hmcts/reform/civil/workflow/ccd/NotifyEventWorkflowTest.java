package uk.gov.hmcts.reform.civil.workflow.ccd;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.notification.handlers.Notifier;
import uk.gov.hmcts.reform.civil.notification.handlers.NotifierFactory;
import uk.gov.hmcts.reform.civil.workflow.WorkflowIntegrationTest;
import uk.gov.hmcts.reform.civil.workflow.ccd.fixture.NotifyEventFixtures;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings("java:S5960")
class NotifyEventWorkflowTest extends WorkflowIntegrationTest {

    @MockBean
    private NotifierFactory notifierFactory;

    @Test
    void shouldPopulateNotificationSummaryAtAboutToSubmit() throws Exception {
        Notifier notifier = mock(Notifier.class);
        when(notifierFactory.getNotifier(NotifyEventFixtures.activityId())).thenReturn(notifier);
        when(notifier.notifyParties(any(), eq(CaseEvent.NOTIFY_EVENT.toString()), eq(NotifyEventFixtures.activityId())))
            .thenReturn("Attempted: Example notifier");

        startWorkflow(NotifyEventFixtures.caseData())
            .eventId(CaseEvent.NOTIFY_EVENT)
            .aboutToSubmit()
            .then(result -> {
                assertThat(result.response().getErrors()).isNullOrEmpty();
                assertThat(result.caseData().getNotificationSummary()).isEqualTo("Attempted: Example notifier");
            });

        verify(notifier).notifyParties(
            any(),
            eq(CaseEvent.NOTIFY_EVENT.toString()),
            eq(NotifyEventFixtures.activityId())
        );
    }
}
