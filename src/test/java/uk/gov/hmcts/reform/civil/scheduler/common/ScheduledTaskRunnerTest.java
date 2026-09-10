package uk.gov.hmcts.reform.civil.scheduler.common;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.sampledata.CaseDetailsBuilder;
import uk.gov.hmcts.reform.civil.scheduler.common.interceptor.SchedulerInterceptor;
import uk.gov.hmcts.reform.civil.scheduler.common.interceptor.SchedulerInterceptorResolver;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.search.common.ElasticSearchResult;

import java.time.Duration;
import java.util.List;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ScheduledTaskRunnerTest {

    private static final String SCHEDULER_NAME = "JudgmentBuffer";

    @Mock
    private ScheduledTask<CaseDetails, Long> scheduledTask;

    @Mock
    private ScheduledEventTracker scheduledEventTracker;

    @Mock
    private ScheduledTaskProcessor<CaseDetails, Long> scheduledTaskProcessor;

    @Mock
    private FeatureToggleService featureToggleService;

    @Mock
    private SchedulerInterceptorResolver interceptorResolver;

    @InjectMocks
    private ScheduledTaskRunner<CaseDetails, Long> scheduledTaskRunner;

    @BeforeEach
    void setUp() {
        doReturn(List.of()).when(interceptorResolver).resolveInterceptors(any());
    }

    @Test
    void shouldRunScheduledTask_whenFeatureToggleIsEnabled() {
        when(featureToggleService.isSpringSchedulerEnabled(SCHEDULER_NAME)).thenReturn(true);
        CaseDetails case1 = CaseDetailsBuilder.builder().id(1L).build();
        ElasticSearchResult searchResult = new ElasticSearchResult(Stream.of(case1), 1);
        ScheduledTaskOutcome<Long> outcome = new ScheduledTaskOutcome<>(List.of(1L), List.of(), List.of(), false, "", Duration.ZERO, Duration.ZERO);

        when(scheduledTaskProcessor.performProcessing(any(), eq(scheduledTask), eq(searchResult), anyList()))
            .thenReturn(outcome);

        scheduledTaskRunner.run(SCHEDULER_NAME, () -> searchResult, scheduledTask);

        verify(featureToggleService).isSpringSchedulerEnabled(SCHEDULER_NAME);
        verify(scheduledEventTracker).jobStartedEvent(any(), eq(1));
    }

    @Test
    void shouldNotRunScheduledTask_whenFeatureToggleIsDisabled() {
        when(featureToggleService.isSpringSchedulerEnabled(SCHEDULER_NAME)).thenReturn(false);

        scheduledTaskRunner.run(SCHEDULER_NAME, () -> null, scheduledTask);

        verify(featureToggleService).isSpringSchedulerEnabled(SCHEDULER_NAME);
        verifyNoInteractions(scheduledTaskProcessor, scheduledEventTracker, scheduledTask);
    }

    @Test
    void shouldAbort_whenCaseRetrievalFails() {
        when(featureToggleService.isSpringSchedulerEnabled(SCHEDULER_NAME)).thenReturn(true);
        ScheduledTaskEventConfiguration eventConfig = new ScheduledTaskEventConfiguration(SCHEDULER_NAME);

        scheduledTaskRunner.run(SCHEDULER_NAME, () -> null, scheduledTask);

        verify(scheduledEventTracker).jobAbortedEvent(eq(eventConfig), eq("SearchResult cannot be null"), any());
        verifyNoMoreInteractions(scheduledTask);
    }

    @Test
    void shouldHandleZeroCases_whenTotalResultsIsZero() {
        when(featureToggleService.isSpringSchedulerEnabled(SCHEDULER_NAME)).thenReturn(true);
        ScheduledTaskEventConfiguration eventConfig = new ScheduledTaskEventConfiguration(SCHEDULER_NAME);
        ElasticSearchResult searchResult = new ElasticSearchResult(Stream.empty(), 0);

        scheduledTaskRunner.run(SCHEDULER_NAME, () -> searchResult, scheduledTask);

        verify(scheduledEventTracker).jobStartedEvent(eventConfig, 0);
        verify(scheduledEventTracker).jobCompletedNoCasesEvent(eq(eventConfig), any());
        verifyNoMoreInteractions(scheduledTaskProcessor);
    }

    @Test
    void shouldHandleCases_whenCaseRetrievalIsSuccessful() {
        when(featureToggleService.isSpringSchedulerEnabled(SCHEDULER_NAME)).thenReturn(true);
        ScheduledTaskEventConfiguration eventConfig = new ScheduledTaskEventConfiguration(SCHEDULER_NAME);
        CaseDetails case1 = CaseDetailsBuilder.builder().id(1L).build();
        ElasticSearchResult searchResult = new ElasticSearchResult(Stream.of(case1), 1);
        ScheduledTaskOutcome<Long> outcome = new ScheduledTaskOutcome<>(List.of(1L), List.of(), List.of(), false, "", Duration.ZERO, Duration.ZERO);

        when(scheduledTaskProcessor.performProcessing(eq(eventConfig), eq(scheduledTask), eq(searchResult), anyList()))
            .thenReturn(outcome);

        scheduledTaskRunner.run(SCHEDULER_NAME, () -> searchResult, scheduledTask);

        verify(scheduledEventTracker).jobStartedEvent(eventConfig, 1);
        verify(scheduledTaskProcessor).performProcessing(eq(eventConfig), eq(scheduledTask), eq(searchResult), anyList());
        verify(scheduledEventTracker).jobCompletedEvent(eq(eventConfig), any(ScheduledJobReport.class));
    }

    @Test
    void shouldRunProcessor_whenCasesPresent() {
        when(featureToggleService.isSpringSchedulerEnabled(SCHEDULER_NAME)).thenReturn(true);
        CaseDetails case1 = CaseDetailsBuilder.builder().id(1L).build();
        ElasticSearchResult searchResult = new ElasticSearchResult(Stream.of(case1), 1);

        ScheduledTaskEventConfiguration eventConfig = new ScheduledTaskEventConfiguration(SCHEDULER_NAME);
        ScheduledTaskOutcome<Long> outcome = new ScheduledTaskOutcome<>(List.of(1L), List.of(), List.of(), false, "", Duration.ZERO, Duration.ZERO);

        when(scheduledTaskProcessor.performProcessing(eq(eventConfig), eq(scheduledTask), eq(searchResult), anyList()))
            .thenReturn(outcome);

        scheduledTaskRunner.run(SCHEDULER_NAME, () -> searchResult, scheduledTask);

        verify(scheduledTaskProcessor).performProcessing(eq(eventConfig), eq(scheduledTask), eq(searchResult), anyList());
    }

    @Test
    void shouldAbortEarly_whenConsecutiveFailuresThresholdReached() {
        when(featureToggleService.isSpringSchedulerEnabled(SCHEDULER_NAME)).thenReturn(true);
        CaseDetails case1 = CaseDetailsBuilder.builder().id(1L).build();
        CaseDetails case2 = CaseDetailsBuilder.builder().id(2L).build();
        ElasticSearchResult searchResult = new ElasticSearchResult(Stream.of(case1, case2), 2);

        ScheduledTaskEventConfiguration eventConfig = new ScheduledTaskEventConfiguration(SCHEDULER_NAME);
        ScheduledTaskOutcome<Long> outcome = new ScheduledTaskOutcome<>(
            List.of(),
            List.of(1L, 2L),
            List.of(),
            true,
            "Error 2",
            Duration.ofMillis(100),
            Duration.ZERO
        );

        when(scheduledTaskProcessor.performProcessing(eq(eventConfig), eq(scheduledTask), eq(searchResult), anyList()))
            .thenReturn(outcome);

        scheduledTaskRunner.run(SCHEDULER_NAME, () -> searchResult, scheduledTask);

        verify(scheduledEventTracker).jobAbortedEvent(eq(eventConfig), any(ScheduledJobReport.class));
    }

    @Test
    void shouldNotAbortEarly_whenFailuresAreNotConsecutive() {
        when(featureToggleService.isSpringSchedulerEnabled(SCHEDULER_NAME)).thenReturn(true);
        CaseDetails case1 = CaseDetailsBuilder.builder().id(1L).build();
        CaseDetails case2 = CaseDetailsBuilder.builder().id(2L).build();
        ElasticSearchResult searchResult = new ElasticSearchResult(Stream.of(case1, case2), 2);

        ScheduledTaskEventConfiguration eventConfig = new ScheduledTaskEventConfiguration(SCHEDULER_NAME);
        ScheduledTaskOutcome<Long> outcome = new ScheduledTaskOutcome<>(List.of(1L), List.of(2L), List.of(), false, "", Duration.ZERO, Duration.ZERO);

        when(scheduledTaskProcessor.performProcessing(eq(eventConfig), eq(scheduledTask), eq(searchResult), anyList()))
            .thenReturn(outcome);

        scheduledTaskRunner.run(SCHEDULER_NAME, () -> searchResult, scheduledTask);

        verify(scheduledEventTracker).jobCompletedEvent(eq(eventConfig), any(ScheduledJobReport.class));
    }

    @Test
    void shouldRunScheduledTask_whenUsingConfigurationObject() {
        when(featureToggleService.isSpringSchedulerEnabled(SCHEDULER_NAME)).thenReturn(true);
        CaseDetails case1 = CaseDetailsBuilder.builder().id(1L).build();
        ElasticSearchResult searchResult = new ElasticSearchResult(Stream.of(case1), 1);
        ScheduledTaskOutcome<Long> outcome = new ScheduledTaskOutcome<>(List.of(1L), List.of(), List.of(), false, "", Duration.ZERO, Duration.ZERO);

        ScheduledTaskEventConfiguration eventConfig = new ScheduledTaskEventConfiguration(SCHEDULER_NAME);
        when(scheduledTaskProcessor.performProcessing(eq(eventConfig), eq(scheduledTask), eq(searchResult), anyList()))
            .thenReturn(outcome);

        ScheduledTaskConfiguration<CaseDetails, Long> config = ScheduledTaskConfiguration.<CaseDetails, Long>builder()
            .schedulerName(SCHEDULER_NAME)
            .searchResultSupplier(() -> searchResult)
            .scheduledTask(scheduledTask)
            .build();

        scheduledTaskRunner.run(config);

        verify(featureToggleService).isSpringSchedulerEnabled(SCHEDULER_NAME);
        verify(scheduledEventTracker).jobStartedEvent(eventConfig, 1);
        verify(scheduledTaskProcessor).performProcessing(eq(eventConfig), eq(scheduledTask), eq(searchResult), anyList());
        verify(scheduledEventTracker).jobCompletedEvent(eq(eventConfig), any(ScheduledJobReport.class));
    }

    @Test
    void shouldCallInterceptorResolver() {
        // Given
        when(featureToggleService.isSpringSchedulerEnabled(SCHEDULER_NAME)).thenReturn(true);
        CaseDetails case1 = CaseDetailsBuilder.builder().id(1L).build();
        ElasticSearchResult searchResult = new ElasticSearchResult(Stream.of(case1), 1);
        List<SchedulerInterceptor<CaseDetails>> interceptors = List.of((context, chain) -> { });
        doReturn(interceptors).when(interceptorResolver).resolveInterceptors(any());
        when(scheduledTaskProcessor.performProcessing(any(), any(), any(), anyList()))
            .thenReturn(new ScheduledTaskOutcome<>(List.of(1L), List.of(), List.of(), false, "", Duration.ZERO, Duration.ZERO));

        // When
        scheduledTaskRunner.run(SCHEDULER_NAME, () -> searchResult, scheduledTask);

        // Then
        verify(interceptorResolver).resolveInterceptors(any());
        verify(scheduledTaskProcessor).performProcessing(any(), any(), any(), eq(interceptors));
    }
}
