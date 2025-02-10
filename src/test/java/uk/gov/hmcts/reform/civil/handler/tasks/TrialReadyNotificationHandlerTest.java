package uk.gov.hmcts.reform.civil.handler.tasks;

import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.event.TakeCaseOfflineEvent;
import uk.gov.hmcts.reform.civil.event.TrialReadyNotificationEvent;
import uk.gov.hmcts.reform.civil.service.search.TrialReadyNotificationSearchService;

import java.util.Map;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class TrialReadyNotificationHandlerTest {

    @Mock
    private ExternalTask mockTask;

    @Mock
    private ExternalTaskService externalTaskService;

    @Mock
    private TrialReadyNotificationSearchService searchService;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @InjectMocks
    private TrialReadyNotificationCheckHandler handler;

    @BeforeEach
    void init() {
        when(mockTask.getTopicName()).thenReturn("test");
        when(mockTask.getWorkerId()).thenReturn("worker");
    }

    @Test
    void shouldEmitTrialReadyCheckEvent_whenCasesFound() {
        long caseId = 1L;
        Map<String, Object> data = Map.of("data", "some data");
        Set<CaseDetails> caseDetails = Set.of(CaseDetails.builder().id(caseId).data(data).build());

        when(searchService.getCases()).thenReturn(caseDetails);

        handler.execute(mockTask, externalTaskService);

        verify(applicationEventPublisher).publishEvent(new TrialReadyNotificationEvent(caseId));
        verify(externalTaskService).complete(mockTask, null);
    }

    @Test
    void shouldNotEmitTrialReadyCheckEvent_WhenNoCasesFound() {
        when(searchService.getCases()).thenReturn(Set.of());

        handler.execute(mockTask, externalTaskService);

        verifyNoInteractions(applicationEventPublisher);
    }

    @Test
    void shouldCallHandleFailureMethod_whenExceptionFromBusinessLogic() {
        String errorMessage = "there was an error";

        when(mockTask.getRetries()).thenReturn(null);
        when(searchService.getCases()).thenAnswer(invocation -> {
            throw new Exception(errorMessage);
        });

        handler.execute(mockTask, externalTaskService);

        verify(externalTaskService, never()).complete(mockTask);
        verify(externalTaskService).handleFailure(
            eq(mockTask),
            eq(errorMessage),
            anyString(),
            eq(2),
            eq(300000L)
        );
    }

    @Test
    void shouldNotCallHandleFailureMethod_whenExceptionOnCompleteCall() {

        handler.execute(mockTask, externalTaskService);

        verify(externalTaskService, never()).handleFailure(
            any(ExternalTask.class),
            anyString(),
            anyString(),
            anyInt(),
            anyLong()
        );
    }

    @Test
    void shouldHandleExceptionAndContinue_whenOneCaseErrors() {
        long caseId = 1L;
        long otherId = 2L;
        Map<String, Object> data = Map.of("data", "some data");
        Set<CaseDetails> caseDetails = Set.of(
            CaseDetails.builder().id(caseId).data(data).build(),
            CaseDetails.builder().id(otherId).data(data).build());

        when(searchService.getCases()).thenReturn(caseDetails);

        String errorMessage = "there was an error";

        doThrow(new NullPointerException(errorMessage))
            .when(applicationEventPublisher).publishEvent(eq(new TakeCaseOfflineEvent(caseId)));

        handler.execute(mockTask, externalTaskService);

        verify(externalTaskService, never()).handleFailure(
            any(ExternalTask.class),
            anyString(),
            anyString(),
            anyInt(),
            anyLong()
        );

        verify(applicationEventPublisher, times(2)).publishEvent(any(TrialReadyNotificationEvent.class));
        verify(applicationEventPublisher).publishEvent(new TrialReadyNotificationEvent(caseId));
        verify(applicationEventPublisher).publishEvent(new TrialReadyNotificationEvent(otherId));
    }
}
