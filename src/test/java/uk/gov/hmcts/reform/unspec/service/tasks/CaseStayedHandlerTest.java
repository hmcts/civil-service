package uk.gov.hmcts.reform.unspec.service.tasks;

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
import uk.gov.hmcts.reform.unspec.event.MoveCaseToStayedEvent;
import uk.gov.hmcts.reform.unspec.service.search.CaseStayedSearchService;
import uk.gov.hmcts.reform.unspec.service.tasks.handler.CaseStayedHandler;

import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class CaseStayedHandlerTest {

    @Mock
    private ExternalTask mockExternalTask;

    @Mock
    private ExternalTaskService externalTaskService;

    @Mock
    private CaseStayedSearchService searchService;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @InjectMocks
    private CaseStayedHandler caseStayedFinder;

    @BeforeEach
    void init() {
        when(mockExternalTask.getTopicName()).thenReturn("test");
        when(mockExternalTask.getWorkerId()).thenReturn("worker");
    }

    @Test
    void shouldEmitMoveCaseToStayedEvent_whenCasesFound() {
        long caseId = 1L;
        Map<String, Object> data = Map.of("data", "some data");
        List<CaseDetails> caseDetails = List.of(CaseDetails.builder()
                                                    .id(caseId)
                                                    .data(data)
                                                    .build());

        when(searchService.getCases()).thenReturn(caseDetails);

        caseStayedFinder.execute(mockExternalTask, externalTaskService);

        verify(applicationEventPublisher).publishEvent(new MoveCaseToStayedEvent(caseId));
        verify(externalTaskService).complete(mockExternalTask);
    }

    @Test
    void shouldNotEmitMoveCaseToStayedEvent_WhenNoCasesFound() {
        when(searchService.getCases()).thenReturn(List.of());

        caseStayedFinder.execute(mockExternalTask, externalTaskService);

        verifyNoInteractions(applicationEventPublisher);
    }

    @Test
    void shouldCatchError_whenException() {
        String errorMessage = "there was an error";

        when(mockExternalTask.getRetries()).thenReturn(null);
        when(searchService.getCases()).thenAnswer(invocation -> {
            throw new Exception(errorMessage);
        });

        caseStayedFinder.execute(mockExternalTask, externalTaskService);

        verify(externalTaskService).handleFailure(mockExternalTask, "worker", errorMessage, 2, 500L);
    }
}
