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
import uk.gov.hmcts.reform.civil.service.search.ElasticSearchService;
import uk.gov.hmcts.reform.civil.service.search.common.ElasticSearchResult;

import java.util.List;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ScheduledTaskRunnerTest {

    @Mock
    private ElasticSearchService searchService;

    @Mock
    private ScheduledTask scheduledTask;

    @Mock
    private ScheduledEventTracker scheduledEventTracker;

    @Mock
    private ScheduledTaskProcessor scheduledTaskProcessor;

    @InjectMocks
    private ScheduledTaskRunner scheduledTaskRunner;

    @Test
    void shouldAbort_whenCaseRetrievalFails() {
        ScheduledTaskEventConfiguration eventConfig = new ScheduledTaskEventConfiguration("JudgmentBuffer");

        scheduledTaskRunner.run(eventConfig, null, scheduledTask);

        verify(scheduledEventTracker).jobAbortedEvent(eq(eventConfig), eq("SearchResult cannot be null"));
        verifyNoMoreInteractions(scheduledTask);
    }

    @Test
    void shouldHandleZeroCases_whenTotalResultsIsZero() {
        ScheduledTaskEventConfiguration eventConfig = new ScheduledTaskEventConfiguration("JudgmentBuffer");
        ElasticSearchResult searchResult = new ElasticSearchResult(Stream.empty(), 0);

        scheduledTaskRunner.run(eventConfig, searchResult, scheduledTask);

        verify(scheduledEventTracker).jobStartedEvent(eq(eventConfig), eq(0));
        verify(scheduledEventTracker).jobCompletedNoCasesEvent(eq(eventConfig));
        verifyNoMoreInteractions(scheduledTaskProcessor);
    }

    @Test
    void shouldHandleCases_whenCaseRetrievalIsSuccessful() {
        ScheduledTaskEventConfiguration eventConfig = new ScheduledTaskEventConfiguration("JudgmentBuffer");
        CaseDetails case1 = CaseDetailsBuilder.builder().id(1L).build();
        ElasticSearchResult searchResult = new ElasticSearchResult(Stream.of(case1), 1);
        ScheduledTaskOutcome outcome = new ScheduledTaskOutcome(List.of(1L), List.of(), false, "");

        when(scheduledTaskProcessor.performProcessing(eq(eventConfig), eq(scheduledTask), eq(searchResult)))
            .thenReturn(outcome);

        scheduledTaskRunner.run(eventConfig, searchResult, scheduledTask);

        verify(scheduledEventTracker).jobStartedEvent(eq(eventConfig), eq(1));
        verify(scheduledTaskProcessor).performProcessing(eq(eventConfig), eq(scheduledTask), eq(searchResult));
        verify(scheduledEventTracker).jobCompletedEvent(eq(eventConfig), eq(1), eq(1), eq(0));
    }

    @Test
    void shouldRunProcessor_whenCasesPresent() {
        CaseDetails case1 = CaseDetailsBuilder.builder().id(1L).build();
        ElasticSearchResult searchResult = new ElasticSearchResult(Stream.of(case1), 1);

        ScheduledTaskEventConfiguration eventConfig = new ScheduledTaskEventConfiguration("JudgmentBuffer");
        ScheduledTaskOutcome outcome = new ScheduledTaskOutcome(List.of(1L), List.of(), false, "");

        when(scheduledTaskProcessor.performProcessing(eq(eventConfig), eq(scheduledTask), any(ElasticSearchResult.class)))
            .thenReturn(outcome);

        scheduledTaskRunner.run(eventConfig, searchResult, scheduledTask);

        verify(scheduledTaskProcessor).performProcessing(eq(eventConfig), eq(scheduledTask), any(ElasticSearchResult.class));
    }

    @Test
    void shouldAbortEarly_whenConsecutiveFailuresThresholdReached() {
        CaseDetails case1 = CaseDetailsBuilder.builder().id(1L).build();
        CaseDetails case2 = CaseDetailsBuilder.builder().id(2L).build();
        ElasticSearchResult searchResult = new ElasticSearchResult(Stream.of(case1, case2), 2);

        ScheduledTaskEventConfiguration eventConfig = new ScheduledTaskEventConfiguration("JudgmentBuffer");
        ScheduledTaskOutcome outcome = new ScheduledTaskOutcome(List.of(), List.of(1L, 2L), true, "Error 2");

        when(scheduledTaskProcessor.performProcessing(eq(eventConfig), eq(scheduledTask), any(ElasticSearchResult.class)))
            .thenReturn(outcome);

        scheduledTaskRunner.run(eventConfig, searchResult, scheduledTask);

        verify(scheduledEventTracker).jobAbortedEvent(eq(eventConfig), eq(2), eq(0), eq(2), eq("Error 2"));
    }

    @Test
    void shouldNotAbortEarly_whenFailuresAreNotConsecutive() {
        CaseDetails case1 = CaseDetailsBuilder.builder().id(1L).build();
        CaseDetails case2 = CaseDetailsBuilder.builder().id(2L).build();
        ElasticSearchResult searchResult = new ElasticSearchResult(Stream.of(case1, case2), 2);

        ScheduledTaskEventConfiguration eventConfig = new ScheduledTaskEventConfiguration("JudgmentBuffer");
        ScheduledTaskOutcome outcome = new ScheduledTaskOutcome(List.of(1L), List.of(2L), false, "");

        when(scheduledTaskProcessor.performProcessing(eq(eventConfig), eq(scheduledTask), any(ElasticSearchResult.class)))
            .thenReturn(outcome);

        scheduledTaskRunner.run(eventConfig, searchResult, scheduledTask);

        verify(scheduledEventTracker).jobCompletedEvent(eq(eventConfig), eq(2), eq(1), eq(1));
    }
}
