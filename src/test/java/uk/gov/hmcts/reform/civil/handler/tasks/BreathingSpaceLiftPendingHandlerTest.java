package uk.gov.hmcts.reform.civil.handler.tasks;

import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.config.properties.EventProperties;
import uk.gov.hmcts.reform.civil.event.BreathingSpaceLiftPendingEvent;
import uk.gov.hmcts.reform.civil.service.ExternalTaskCompletionService;
import uk.gov.hmcts.reform.civil.service.search.breathingspace.BreathingSpaceLiftPendingSearchService;
import uk.gov.hmcts.reform.civil.service.search.common.ElasticSearchResult;

import java.util.stream.Stream;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BreathingSpaceLiftPendingHandlerTest {

    @Mock
    private ExternalTask mockTask;

    @Mock
    private ExternalTaskService externalTaskService;

    @Mock
    private BreathingSpaceLiftPendingSearchService searchService;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @Spy
    private EventProperties eventProperties = configuredEventProperties();

    @Spy
    private ExternalTaskCompletionService externalTaskCompletionService = new ExternalTaskCompletionService();

    @InjectMocks
    private BreathingSpaceLiftPendingHandler handler;

    @BeforeEach
    void init() {
        when(mockTask.getTopicName()).thenReturn("BREATHING_SPACE_LIFT_PENDING");
    }

    @Test
    void shouldPublishLiftPendingEvent_whenCasesFound() {
        CaseDetails caseDetails = CaseDetails.builder().id(1L).build();
        when(searchService.getElasticSearchResult())
            .thenReturn(new ElasticSearchResult(Stream.of(caseDetails), 1));

        handler.execute(mockTask, externalTaskService);

        verify(applicationEventPublisher).publishEvent(new BreathingSpaceLiftPendingEvent(1L));
        verify(externalTaskService).complete(mockTask, null);
    }

    @Test
    void shouldNotPublishLiftPendingEvent_whenNoCasesFound() {
        when(searchService.getElasticSearchResult())
            .thenReturn(new ElasticSearchResult(Stream.empty(), 0));

        handler.execute(mockTask, externalTaskService);

        verifyNoInteractions(applicationEventPublisher);
        verify(externalTaskService).complete(mockTask, null);
    }

    private static EventProperties configuredEventProperties() {
        EventProperties properties = new EventProperties();
        properties.setRetryCount(3);
        return properties;
    }
}
