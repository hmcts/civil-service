package uk.gov.hmcts.reform.civil.scheduler.common;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.sampledata.CaseDetailsBuilder;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.search.common.ElasticSearchResult;

import java.time.Duration;
import java.util.List;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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

    @InjectMocks
    private ScheduledTaskRunner<CaseDetails, Long> scheduledTaskRunner;

    @Test
    void shouldRunScheduledTask_whenFeatureToggleIsEnabled() {
        when(featureToggleService.isSpringSchedulerEnabled(SCHEDULER_NAME)).thenReturn(true);
        CaseDetails case1 = CaseDetailsBuilder.builder().id(1L).build();
        ElasticSearchResult searchResult = new ElasticSearchResult(Stream.of(case1), 1);
        ScheduledTaskOutcome<Long> outcome = new ScheduledTaskOutcome<>(List.of(1L), List.of(), false, "", Duration.ZERO);

        when(scheduledTaskProcessor.performProcessing(any(), eq(scheduledTask), eq(searchResult)))
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
        ScheduledTaskEventConfiguration eventConfig = new ScheduledTaskEventConfiguration("JudgmentBuffer");

        scheduledTaskRunner.run(eventConfig, null, scheduledTask);

        verify(scheduledEventTracker).jobAbortedEvent(eventConfig, "SearchResult cannot be null");
        verifyNoMoreInteractions(scheduledTask);
    }

    @Test
    void shouldHandleZeroCases_whenTotalResultsIsZero() {
        ScheduledTaskEventConfiguration eventConfig = new ScheduledTaskEventConfiguration("JudgmentBuffer");
        ElasticSearchResult searchResult = new ElasticSearchResult(Stream.empty(), 0);

        scheduledTaskRunner.run(eventConfig, searchResult, scheduledTask);

        verify(scheduledEventTracker).jobStartedEvent(eventConfig, 0);
        verify(scheduledEventTracker).jobCompletedNoCasesEvent(eventConfig);
        verifyNoMoreInteractions(scheduledTaskProcessor);
    }

    @Test
    void shouldHandleCases_whenCaseRetrievalIsSuccessful() {
        ScheduledTaskEventConfiguration eventConfig = new ScheduledTaskEventConfiguration("JudgmentBuffer");
        CaseDetails case1 = CaseDetailsBuilder.builder().id(1L).build();
        ElasticSearchResult searchResult = new ElasticSearchResult(Stream.of(case1), 1);
        ScheduledTaskOutcome<Long> outcome = new ScheduledTaskOutcome<>(List.of(1L), List.of(), false, "", Duration.ZERO);

        when(scheduledTaskProcessor.performProcessing(eventConfig, scheduledTask, searchResult))
            .thenReturn(outcome);

        scheduledTaskRunner.run(eventConfig, searchResult, scheduledTask);

        verify(scheduledEventTracker).jobStartedEvent(eventConfig, 1);
        verify(scheduledTaskProcessor).performProcessing(eventConfig, scheduledTask, searchResult);
        verify(scheduledEventTracker).jobCompletedEvent(eventConfig, 1, 1, 0, Duration.ZERO);
    }

    @Test
    void shouldRunProcessor_whenCasesPresent() {
        CaseDetails case1 = CaseDetailsBuilder.builder().id(1L).build();
        ElasticSearchResult searchResult = new ElasticSearchResult(Stream.of(case1), 1);

        ScheduledTaskEventConfiguration eventConfig = new ScheduledTaskEventConfiguration("JudgmentBuffer");
        ScheduledTaskOutcome<Long> outcome = new ScheduledTaskOutcome<>(List.of(1L), List.of(), false, "", Duration.ZERO);

        when(scheduledTaskProcessor.performProcessing(eventConfig, scheduledTask, searchResult))
            .thenReturn(outcome);

        scheduledTaskRunner.run(eventConfig, searchResult, scheduledTask);

        verify(scheduledTaskProcessor).performProcessing(eventConfig, scheduledTask, searchResult);
    }

    @Test
    void shouldAbortEarly_whenConsecutiveFailuresThresholdReached() {
        CaseDetails case1 = CaseDetailsBuilder.builder().id(1L).build();
        CaseDetails case2 = CaseDetailsBuilder.builder().id(2L).build();
        ElasticSearchResult searchResult = new ElasticSearchResult(Stream.of(case1, case2), 2);

        ScheduledTaskEventConfiguration eventConfig = new ScheduledTaskEventConfiguration("JudgmentBuffer");
        ScheduledTaskOutcome<Long> outcome = new ScheduledTaskOutcome<>(
            List.of(),
            List.of(1L, 2L),
            true,
            "Error 2",
            Duration.ofMillis(100)
        );

        when(scheduledTaskProcessor.performProcessing(eventConfig, scheduledTask, searchResult))
            .thenReturn(outcome);

        scheduledTaskRunner.run(eventConfig, searchResult, scheduledTask);

        verify(scheduledEventTracker).jobAbortedEvent(eventConfig, 2, 0, 2, "Error 2", Duration.ofMillis(100));
    }

    @Test
    void shouldNotAbortEarly_whenFailuresAreNotConsecutive() {
        CaseDetails case1 = CaseDetailsBuilder.builder().id(1L).build();
        CaseDetails case2 = CaseDetailsBuilder.builder().id(2L).build();
        ElasticSearchResult searchResult = new ElasticSearchResult(Stream.of(case1, case2), 2);

        ScheduledTaskEventConfiguration eventConfig = new ScheduledTaskEventConfiguration("JudgmentBuffer");
        ScheduledTaskOutcome<Long> outcome = new ScheduledTaskOutcome<>(List.of(1L), List.of(2L), false, "", Duration.ZERO);

        when(scheduledTaskProcessor.performProcessing(eventConfig, scheduledTask, searchResult))
            .thenReturn(outcome);

        scheduledTaskRunner.run(eventConfig, searchResult, scheduledTask);

        verify(scheduledEventTracker).jobCompletedEvent(eventConfig, 2, 1, 1, Duration.ZERO);
    }
}
