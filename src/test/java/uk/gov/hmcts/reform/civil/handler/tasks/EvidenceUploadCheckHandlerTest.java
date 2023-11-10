package uk.gov.hmcts.reform.civil.handler.tasks;

import java.util.List;
import java.util.Map;

import org.camunda.bpm.client.exception.NotFoundException;
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
import uk.gov.hmcts.reform.civil.event.EvidenceUploadNotificationEvent;
import uk.gov.hmcts.reform.civil.service.search.EvidenceUploadNotificationSearchService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class EvidenceUploadCheckHandlerTest {

    @Mock
    private ExternalTask mockTask;

    @Mock
    private ExternalTaskService externalTaskService;

    @Mock
    private EvidenceUploadNotificationSearchService searchService;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @InjectMocks
    private EvidenceUploadCheckHandler handler;

    @BeforeEach
    void init() {
        when(mockTask.getTopicName()).thenReturn("test");
        when(mockTask.getWorkerId()).thenReturn("worker");
    }

    @Test
    void shouldEmitEvidenceUploadCheckEvent_whenCasesFound() {
        // Given: one case found from search service
        long caseId = 1L;
        Map<String, Object> data = Map.of("data", "some data");
        List<CaseDetails> caseDetails = List.of(CaseDetails.builder().id(caseId).data(data).build());

        given(searchService.getCases()).willReturn(caseDetails);

        // When: handler is called
        handler.execute(mockTask, externalTaskService);

        // Then: task should be completed
        verify(applicationEventPublisher).publishEvent(new EvidenceUploadNotificationEvent(caseId));
        verify(externalTaskService).complete(mockTask);
    }

    @Test
    void shouldNotEmitEvidenceUploadCheckEvent_WhenNoCasesFound() {
        // Given: no case found from search service
        when(searchService.getCases()).thenReturn(List.of());

        // When: handler is called
        handler.execute(mockTask, externalTaskService);

        // Then: publish event should not get called
        verifyNoInteractions(applicationEventPublisher);
    }

    @Test
    void shouldCallHandleFailureMethod_whenExceptionFromBusinessLogic() {
        // Given: error is returned from search service
        String errorMessage = "there was an error";

        when(mockTask.getRetries()).thenReturn(null);
        when(searchService.getCases()).thenAnswer(invocation -> {
            throw new Exception(errorMessage);
        });

        // When: handler is called
        handler.execute(mockTask, externalTaskService);

        // Then: error should be handled
        verify(externalTaskService, never()).complete(mockTask);
        verify(externalTaskService).handleFailure(
            eq(mockTask),
            eq(errorMessage),
            anyString(),
            eq(2),
            eq(1000L)
        );
    }

    @Test
    void shouldNotCallHandleFailureMethod_whenExceptionOnCompleteCall() {
        //Given: exception thrown on task complete
        String errorMessage = "there was an error";

        doThrow(new NotFoundException(errorMessage)).when(externalTaskService).complete(mockTask);

        // When: handler is called
        handler.execute(mockTask, externalTaskService);

        // Then: handle failure should not get called
        verify(externalTaskService).handleFailure(
            any(ExternalTask.class),
            anyString(),
            anyString(),
            anyInt(),
            anyLong()
        );
    }

    @Test
    void shouldHandleExceptionAndContinue_whenOneCaseErrors() {
        // Given: search service returns two cases and returns error in publish event
        long caseId = 1L;
        long otherId = 2L;
        Map<String, Object> data = Map.of("data", "some data");
        List<CaseDetails> caseDetails = List.of(
            CaseDetails.builder().id(caseId).data(data).build(),
            CaseDetails.builder().id(otherId).data(data).build());

        given(searchService.getCases()).willReturn(caseDetails);

        String errorMessage = "there was an error";

        doThrow(new NullPointerException(errorMessage))
            .when(applicationEventPublisher).publishEvent(new EvidenceUploadNotificationEvent(caseId));

        // When: handler is called
        handler.execute(mockTask, externalTaskService);

        // Then: handle failure should not get called
        verify(externalTaskService, never()).handleFailure(
            any(ExternalTask.class),
            anyString(),
            anyString(),
            anyInt(),
            anyLong()
        );

        //Then: publish event should be called for all cases
        verify(applicationEventPublisher, times(2)).publishEvent(any(EvidenceUploadNotificationEvent.class));
        verify(applicationEventPublisher).publishEvent(new EvidenceUploadNotificationEvent(caseId));
        verify(applicationEventPublisher).publishEvent(new EvidenceUploadNotificationEvent(otherId));
    }
}
