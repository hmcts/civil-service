package uk.gov.hmcts.reform.civil.handler.tasks;

import org.camunda.bpm.client.exception.NotFoundException;
import org.camunda.bpm.client.exception.RestException;
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
import uk.gov.hmcts.reform.civil.event.CoscApplicationProcessorEvent;
import uk.gov.hmcts.reform.civil.sampledata.CaseDetailsBuilder;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.search.CoscApplicationSearchService;

import java.util.Map;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import org.mockito.Spy;
import uk.gov.hmcts.reform.civil.config.properties.EventProperties;
import uk.gov.hmcts.reform.civil.service.ExternalTaskCompletionService;

@ExtendWith(SpringExtension.class)
class CoscApplicationProcessorHandlerTest {

    @Mock
    private ExternalTask mockTask;

    @Mock
    private ExternalTaskService externalTaskService;

    @Mock
    private CoscApplicationSearchService searchService;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;
    @Mock
    private FeatureToggleService featureToggleService;
    @Spy
    private EventProperties eventProperties = configuredEventProperties();

    @Spy
    private ExternalTaskCompletionService externalTaskCompletionService = new ExternalTaskCompletionService();

    @InjectMocks
    private CoscApplicationProcessorHandler handler;

    @BeforeEach
    void init() {
        when(mockTask.getTopicName()).thenReturn("test");
        when(mockTask.getWorkerId()).thenReturn("worker");
    }

    @Test
    void shouldInteractWithSearchServiceAndPublishEvent_whenCasesFound() {
        long caseId = 1L;
        Map<String, Object> data = Map.of("data", "some data");
        Set<CaseDetails> caseDetails = Set.of(new CaseDetailsBuilder().id(caseId).data(data).build());

        when(featureToggleService.isSpringSchedulerEnabled("CoscApplicationProcessor")).thenReturn(false);
        when(searchService.getCases()).thenReturn(caseDetails);

        handler.execute(mockTask, externalTaskService);

        verify(applicationEventPublisher).publishEvent(new CoscApplicationProcessorEvent(caseId));
        verify(externalTaskService).complete(mockTask, null);
    }

    @Test
    void shouldNotEmitEvent_WhenNoCasesFound() {
        when(featureToggleService.isSpringSchedulerEnabled("CoscApplicationProcessor")).thenReturn(false);
        when(searchService.getCases()).thenReturn(Set.of());

        handler.execute(mockTask, externalTaskService);

        verifyNoInteractions(applicationEventPublisher);
    }

    @Test
    void shouldNotProcessLegacyTaskWhenSpringSchedulerIsEnabled() {
        when(featureToggleService.isSpringSchedulerEnabled("CoscApplicationProcessor")).thenReturn(true);

        handler.execute(mockTask, externalTaskService);

        verifyNoInteractions(searchService);
        verifyNoInteractions(applicationEventPublisher);
        verify(externalTaskService).complete(mockTask, null);
    }

    @Test
    void shouldCallHandleFailureMethod_whenExceptionFromBusinessLogic() {
        String errorMessage = "there was an error";

        when(featureToggleService.isSpringSchedulerEnabled("CoscApplicationProcessor")).thenReturn(false);
        when(mockTask.getRetries()).thenReturn(null);
        when(searchService.getCases()).thenAnswer(invocation -> {
            throw new Exception(errorMessage);
        });

        handler.execute(mockTask, externalTaskService);

        verify(externalTaskService, never()).complete(mockTask, null);
        verify(externalTaskService).handleFailure(
            eq(mockTask),
            eq(errorMessage),
            anyString(),
            eq(2),
            anyLong()
        );
    }

    @Test
    void shouldNotCallHandleFailureMethod_whenExceptionOnCompleteCall() {
        String errorMessage = "there was an error";

        doThrow(new NotFoundException(errorMessage, new RestException("", "", 404)))
            .when(externalTaskService).complete(mockTask, null);

        handler.execute(mockTask, externalTaskService);

        verify(externalTaskService, never()).handleFailure(
            any(ExternalTask.class),
            anyString(),
            anyString(),
            anyInt(),
            anyLong()
        );
    }

    private static EventProperties configuredEventProperties() {
        EventProperties properties = new EventProperties();
        properties.setRetryCount(3);
        return properties;
    }

}
