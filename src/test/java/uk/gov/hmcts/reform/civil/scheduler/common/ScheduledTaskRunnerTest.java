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

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

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
        RuntimeException exception = new RuntimeException("Search failed");
        when(searchService.getCases()).thenThrow(exception);

        scheduledTaskRunner.run(eventConfig, searchService::getCases, scheduledTask);

        verify(scheduledEventTracker).jobAbortedEvent(eq(eventConfig), eq("Search failed"));
        verifyNoMoreInteractions(scheduledTask);
    }

    @Test
    void shouldHandleNullCases_whenCaseRetrievalReturnsNull() {
        ScheduledTaskEventConfiguration eventConfig = new ScheduledTaskEventConfiguration("JudgmentBuffer");
        when(searchService.getCases()).thenReturn(null);

        scheduledTaskRunner.run(eventConfig, searchService::getCases, scheduledTask);

        verify(scheduledEventTracker).jobStartedEvent(eq(eventConfig), eq(0));
        verify(scheduledEventTracker).jobCompletedNoCasesEvent(eq(eventConfig));
        verifyNoMoreInteractions(scheduledTask);
    }

    @Test
    void shouldRunProcessor_whenCasesPresent() {
        CaseDetails case1 = CaseDetailsBuilder.builder().id(1L).build();
        Set<CaseDetails> cases = new LinkedHashSet<>(List.of(case1));
        when(searchService.getCases()).thenReturn(cases);

        ScheduledTaskEventConfiguration eventConfig = new ScheduledTaskEventConfiguration("JudgmentBuffer");
        ScheduledTaskOutcome outcome = new ScheduledTaskOutcome(List.of(1L), List.of(), false, "");

        when(scheduledTaskProcessor.performProcessing(eq(eventConfig), eq(scheduledTask), eq(cases)))
            .thenReturn(outcome);

        scheduledTaskRunner.run(eventConfig, searchService::getCases, scheduledTask);

        verify(scheduledEventTracker).jobStartedEvent(eq(eventConfig), eq(1));
        verify(scheduledTaskProcessor).performProcessing(eq(eventConfig), eq(scheduledTask), eq(cases));
        verify(scheduledEventTracker).jobCompletedEvent(eq(eventConfig), eq(cases), eq(List.of(1L)), eq(List.of()));
    }

    @Test
    void shouldAbortEarly_whenConsecutiveFailuresThresholdReached() {
        CaseDetails case1 = CaseDetailsBuilder.builder().id(1L).build();
        CaseDetails case2 = CaseDetailsBuilder.builder().id(2L).build();
        CaseDetails case3 = CaseDetailsBuilder.builder().id(3L).build();
        CaseDetails case4 = CaseDetailsBuilder.builder().id(4L).build();
        Set<CaseDetails> cases = new LinkedHashSet<>(List.of(case1, case2, case3, case4));
        when(searchService.getCases()).thenReturn(cases);

        ScheduledTaskEventConfiguration eventConfig = new ScheduledTaskEventConfiguration("JudgmentBuffer");
        ScheduledTaskOutcome outcome = new ScheduledTaskOutcome(List.of(), List.of(1L, 2L), true, "Error 2");

        when(scheduledTaskProcessor.performProcessing(eq(eventConfig), eq(scheduledTask), eq(cases)))
            .thenReturn(outcome);

        scheduledTaskRunner.run(eventConfig, searchService::getCases, scheduledTask);

        verify(scheduledEventTracker).jobAbortedEvent(eq(eventConfig), eq(cases), eq(List.of()), eq(List.of(1L, 2L)), eq("Error 2"));
    }

    @Test
    void shouldNotAbortEarly_whenFailuresAreNotConsecutive() {
        CaseDetails case1 = CaseDetailsBuilder.builder().id(1L).build();
        CaseDetails case2 = CaseDetailsBuilder.builder().id(2L).build();
        CaseDetails case3 = CaseDetailsBuilder.builder().id(3L).build();
        Set<CaseDetails> cases = new LinkedHashSet<>(List.of(case1, case2, case3));
        when(searchService.getCases()).thenReturn(cases);

        ScheduledTaskEventConfiguration eventConfig = new ScheduledTaskEventConfiguration("JudgmentBuffer");
        ScheduledTaskOutcome outcome = new ScheduledTaskOutcome(List.of(2L), List.of(1L, 3L), false, "");

        when(scheduledTaskProcessor.performProcessing(eq(eventConfig), eq(scheduledTask), eq(cases)))
            .thenReturn(outcome);

        scheduledTaskRunner.run(eventConfig, searchService::getCases, scheduledTask);

        verify(scheduledEventTracker).jobCompletedEvent(eq(eventConfig), eq(cases), eq(List.of(2L)), eq(List.of(1L, 3L)));
    }
}
