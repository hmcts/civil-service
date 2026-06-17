package uk.gov.hmcts.reform.civil.scheduler.common;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.sampledata.CaseDetailsBuilder;

import uk.gov.hmcts.reform.civil.service.search.common.ElasticSearchResult;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
class ScheduledTaskProcessorTest {

    @Mock
    private ScheduledEventTracker scheduledEventTracker;

    @Mock
    private ScheduledTask scheduledTask;

    @InjectMocks
    private ScheduledTaskProcessor scheduledTaskProcessor;

    @Test
    void shouldProcessAllCases_whenNoFailures() {
        ScheduledTaskEventConfiguration eventConfig = new ScheduledTaskEventConfiguration("JudgmentBuffer");

        CaseDetails case1 = CaseDetailsBuilder.builder().id(1L).build();
        CaseDetails case2 = CaseDetailsBuilder.builder().id(2L).build();
        CaseDetails case3 = CaseDetailsBuilder.builder().id(3L).build();
        List<CaseDetails> cases = List.of(case1, case2, case3);
        ElasticSearchResult searchResult = new ElasticSearchResult(cases.stream(), 3);

        ScheduledTaskOutcome outcome = scheduledTaskProcessor.performProcessing(eventConfig, scheduledTask, searchResult);

        assertThat(outcome).isNotNull();
        assertThat(outcome.succeededCases().size()).isEqualTo(3);
        assertThat(outcome.failedCases().size()).isEqualTo(0);

        verify(scheduledTask).accept(case1);
        verify(scheduledTask).accept(case2);
        verify(scheduledTask).accept(case3);
        verifyNoMoreInteractions(scheduledTask);

        verify(scheduledEventTracker).caseProcessedEvent(eventConfig, case1.getId());
        verify(scheduledEventTracker).caseProcessedEvent(eventConfig, case2.getId());
        verify(scheduledEventTracker).caseProcessedEvent(eventConfig, case3.getId());
        verifyNoMoreInteractions(scheduledEventTracker);
    }

    @Test
    void shouldNotProcess_whenNoCases() {
        ScheduledTaskEventConfiguration eventConfig = new ScheduledTaskEventConfiguration("JudgmentBuffer");

        ScheduledTaskOutcome outcome = scheduledTaskProcessor.performProcessing(eventConfig, scheduledTask, new ElasticSearchResult(Stream.empty(), 0));

        assertThat(outcome).isNotNull();
        assertThat(outcome.succeededCases().size()).isEqualTo(0);
        assertThat(outcome.failedCases().size()).isEqualTo(0);

        verifyNoInteractions(scheduledTask);
        verifyNoInteractions(scheduledEventTracker);
    }

    @Test
    void shouldAbortEarly_whenConsecutiveFailuresThresholdReached() {
        ReflectionTestUtils.setField(scheduledTaskProcessor, "circuitBreakerThreshold", 2);

        CaseDetails case1 = CaseDetailsBuilder.builder().id(1L).build();
        CaseDetails case2 = CaseDetailsBuilder.builder().id(2L).build();
        CaseDetails case3 = CaseDetailsBuilder.builder().id(3L).build();
        CaseDetails case4 = CaseDetailsBuilder.builder().id(4L).build();
        List<CaseDetails> cases = List.of(case1, case2, case3, case4);

        // Fail first two cases
        RuntimeException error1 = new RuntimeException("Error 1");
        RuntimeException error2 = new RuntimeException("Error 2");
        doThrow(error1).when(scheduledTask).accept(case1);
        doThrow(error2).when(scheduledTask).accept(case2);

        ScheduledTaskEventConfiguration eventConfig = new ScheduledTaskEventConfiguration("JudgmentBuffer");
        ElasticSearchResult searchResult = new ElasticSearchResult(cases.stream(), 4);

        ScheduledTaskOutcome outcome = scheduledTaskProcessor.performProcessing(eventConfig, scheduledTask, searchResult);

        assertThat(outcome.abortedEarly()).isTrue();
        assertThat(outcome.abortReason()).isEqualTo("Error 2");
        assertThat(outcome.succeededCases()).isEmpty();
        assertThat(outcome.failedCases()).containsExactly(1L, 2L);

        verify(scheduledTask).accept(case1);
        verify(scheduledTask).accept(case2);

        verify(scheduledEventTracker).caseFailedEvent(eventConfig, 1L, error1);
        verify(scheduledEventTracker).caseFailedEvent(eventConfig, 2L, error2);
        verifyNoMoreInteractions(scheduledEventTracker);
    }

    @Test
    void shouldNotAbortEarly_whenFailuresAreNotConsecutive() {
        ReflectionTestUtils.setField(scheduledTaskProcessor, "circuitBreakerThreshold", 2);

        CaseDetails case1 = CaseDetails.builder().id(101L).build();
        CaseDetails case2 = CaseDetails.builder().id(102L).build();
        CaseDetails case3 = CaseDetails.builder().id(103L).build();
        List<CaseDetails> cases = List.of(case1, case2, case3);

        // Fail case 1, succeed case 2, fail case 3
        RuntimeException error1 = new RuntimeException("Error 1");
        doThrow(error1).when(scheduledTask).accept(argThat(c -> c != null && c.getId().equals(101L)));
        doNothing().when(scheduledTask).accept(argThat(c -> c != null && c.getId().equals(102L)));
        RuntimeException error3 = new RuntimeException("Error 3");
        doThrow(error3).when(scheduledTask).accept(argThat(c -> c != null && c.getId().equals(103L)));

        ScheduledTaskEventConfiguration eventConfig = new ScheduledTaskEventConfiguration("JudgmentBuffer");
        ElasticSearchResult searchResult = new ElasticSearchResult(cases.stream().sequential(), 3);

        ScheduledTaskOutcome outcome = scheduledTaskProcessor.performProcessing(eventConfig, scheduledTask, searchResult);

        assertThat(outcome.abortedEarly()).isFalse();
        assertThat(outcome.succeededCases()).containsExactly(102L);
        assertThat(outcome.failedCases()).containsExactly(101L, 103L);

        verify(scheduledEventTracker).caseFailedEvent(eventConfig, 101L, error1);
        verify(scheduledEventTracker).caseProcessedEvent(eventConfig, 102L);
        verify(scheduledEventTracker).caseFailedEvent(eventConfig, 103L, error3);
    }

    @Test
    void shouldHandleExceptionWithNullMessage() {
        ReflectionTestUtils.setField(scheduledTaskProcessor, "circuitBreakerThreshold", 1);

        CaseDetails case1 = CaseDetailsBuilder.builder().id(1L).build();
        ElasticSearchResult searchResult = new ElasticSearchResult(Stream.of(case1), 1);

        RuntimeException errorWithoutMessage = new RuntimeException();
        doThrow(errorWithoutMessage).when(scheduledTask).accept(case1);

        ScheduledTaskEventConfiguration eventConfig = new ScheduledTaskEventConfiguration("JudgmentBuffer");
        ScheduledTaskOutcome outcome = scheduledTaskProcessor.performProcessing(eventConfig, scheduledTask, searchResult);

        assertThat(outcome.abortedEarly()).isTrue();
        assertThat(outcome.abortReason()).isEqualTo("RuntimeException");
        assertThat(outcome.failedCases()).containsExactly(1L);
    }

    @Test
    void shouldContinueProcessing_whenFailuresOccurButThresholdNotReached() {
        ReflectionTestUtils.setField(scheduledTaskProcessor, "circuitBreakerThreshold", 3);

        CaseDetails case1 = CaseDetailsBuilder.builder().id(1L).build();
        CaseDetails case2 = CaseDetailsBuilder.builder().id(2L).build();
        CaseDetails case3 = CaseDetailsBuilder.builder().id(3L).build();
        List<CaseDetails> cases = List.of(case1, case2, case3);

        // Fail first two cases, succeed third
        RuntimeException error1 = new RuntimeException("Error 1");
        RuntimeException error2 = new RuntimeException("Error 2");
        doThrow(error1).when(scheduledTask).accept(case1);
        doThrow(error2).when(scheduledTask).accept(case2);

        ScheduledTaskEventConfiguration eventConfig = new ScheduledTaskEventConfiguration("JudgmentBuffer");
        ElasticSearchResult searchResult = new ElasticSearchResult(cases.stream(), 3);

        ScheduledTaskOutcome outcome = scheduledTaskProcessor.performProcessing(eventConfig, scheduledTask, searchResult);

        assertThat(outcome.abortedEarly()).isFalse();
        assertThat(outcome.succeededCases()).containsExactly(3L);
        assertThat(outcome.failedCases()).containsExactly(1L, 2L);
    }

    @Test
    void shouldShortCircuit_whenCircuitBreakerIsTriggered() {
        ReflectionTestUtils.setField(scheduledTaskProcessor, "circuitBreakerThreshold", 1);

        CaseDetails case1 = CaseDetailsBuilder.builder().id(1L).build();
        CaseDetails case2 = CaseDetailsBuilder.builder().id(2L).build();
        List<CaseDetails> cases = List.of(case1, case2);

        doThrow(new RuntimeException("Error 1")).when(scheduledTask).accept(case1);

        ScheduledTaskEventConfiguration eventConfig = new ScheduledTaskEventConfiguration("JudgmentBuffer");
        ElasticSearchResult searchResult = new ElasticSearchResult(cases.stream(), 2);

        ScheduledTaskOutcome outcome = scheduledTaskProcessor.performProcessing(eventConfig, scheduledTask, searchResult);

        assertThat(outcome.abortedEarly()).isTrue();
        assertThat(outcome.failedCases()).containsExactly(1L);
        assertThat(outcome.succeededCases()).isEmpty();

        // Verify that case 2 was NEVER even attempted
        verify(scheduledTask, never()).accept(case2);
        verify(scheduledEventTracker, never()).caseProcessedEvent(any(), eq(2L));
        verify(scheduledEventTracker, never()).caseFailedEvent(any(), eq(2L), any());
    }
}
