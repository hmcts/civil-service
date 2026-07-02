package uk.gov.hmcts.reform.civil.scheduler.common;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.sampledata.CaseDetailsBuilder;
import uk.gov.hmcts.reform.civil.service.search.common.ElasticSearchResult;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
class ScheduledTaskProcessorTest {

    @Mock
    private ScheduledEventTracker scheduledEventTracker;

    @Mock(answer = Answers.CALLS_REAL_METHODS)
    private ScheduledTask<CaseDetails, Long> scheduledTask;

    @InjectMocks
    private ScheduledTaskProcessor<CaseDetails, Long> scheduledTaskProcessor;

    @BeforeEach
    void setUp() {
        lenient().when(scheduledTask.getItemId(any())).thenAnswer(invocation -> {
            CaseDetails caseDetails = invocation.getArgument(0);
            return caseDetails != null ? caseDetails.getId() : null;
        });
    }

    @Test
    void shouldProcessAllCases_whenNoFailures() {
        ScheduledTaskEventConfiguration eventConfig = new ScheduledTaskEventConfiguration("JudgmentBuffer");

        CaseDetails case1 = CaseDetailsBuilder.builder().id(1L).build();
        CaseDetails case2 = CaseDetailsBuilder.builder().id(2L).build();
        CaseDetails case3 = CaseDetailsBuilder.builder().id(3L).build();
        List<CaseDetails> cases = List.of(case1, case2, case3);
        ElasticSearchResult searchResult = new ElasticSearchResult(cases.stream(), 3);

        ScheduledTaskOutcome<Long> outcome = scheduledTaskProcessor.performProcessing(eventConfig, scheduledTask, searchResult);

        assertThat(outcome).isNotNull();
        assertThat(outcome.succeededCases().size()).isEqualTo(3);
        assertThat(outcome.failedCases().size()).isEqualTo(0);

        verify(scheduledTask).accept(case1);
        verify(scheduledTask).accept(case2);
        verify(scheduledTask).accept(case3);
        verify(scheduledTask).backPressureConfiguration();
        verify(scheduledTask).maxCasesPerRun();
        verify(scheduledTask, times(3)).getItemId(any());
        verifyNoMoreInteractions(scheduledTask);

        verify(scheduledEventTracker).caseProcessedEvent(eventConfig, String.valueOf(case1.getId()));
        verify(scheduledEventTracker).caseProcessedEvent(eventConfig, String.valueOf(case2.getId()));
        verify(scheduledEventTracker).caseProcessedEvent(eventConfig, String.valueOf(case3.getId()));
        verifyNoMoreInteractions(scheduledEventTracker);
    }

    @Test
    void shouldNotProcess_whenNoCases() {
        ScheduledTaskEventConfiguration eventConfig = new ScheduledTaskEventConfiguration("JudgmentBuffer");

        ScheduledTaskOutcome<Long> outcome = scheduledTaskProcessor.performProcessing(eventConfig, scheduledTask, new ElasticSearchResult(Stream.empty(), 0));

        assertThat(outcome).isNotNull();
        assertThat(outcome.succeededCases()).isEmpty();
        assertThat(outcome.failedCases()).isEmpty();

        verify(scheduledTask).backPressureConfiguration();
        verify(scheduledTask).maxCasesPerRun();
        verify(scheduledTask, never()).getItemId(any());
        verifyNoMoreInteractions(scheduledTask);
        verifyNoInteractions(scheduledEventTracker);
    }

    @Test
    void shouldLimitCasesProcessed_whenTaskDefinesMaxCasesPerRun() {
        CaseDetails case1 = CaseDetailsBuilder.builder().id(1L).build();
        CaseDetails case2 = CaseDetailsBuilder.builder().id(2L).build();
        CaseDetails case3 = CaseDetailsBuilder.builder().id(3L).build();
        RecordingScheduledTask task = new RecordingScheduledTask(2);
        ElasticSearchResult searchResult = new ElasticSearchResult(Stream.of(case1, case2, case3), 3);

        ScheduledTaskEventConfiguration eventConfig = new ScheduledTaskEventConfiguration("JudgmentBuffer");

        ScheduledTaskOutcome<Long> outcome = scheduledTaskProcessor.performProcessing(eventConfig, task, searchResult);

        assertThat(outcome.abortedEarly()).isFalse();
        assertThat(outcome.succeededCases()).containsExactly(1L, 2L);
        assertThat(outcome.failedCases()).isEmpty();
        assertThat(task.processedCases()).containsExactly(1L, 2L);

        verify(scheduledEventTracker).caseProcessedEvent(eventConfig, "1");
        verify(scheduledEventTracker).caseProcessedEvent(eventConfig, "2");
        verify(scheduledEventTracker, never()).caseProcessedEvent(eventConfig, "3");
        verifyNoMoreInteractions(scheduledEventTracker);
    }

    @Test
    void shouldApplyDynamicBackPressure_whenFailuresRecover() {
        CapturingScheduledTaskProcessor processor = new CapturingScheduledTaskProcessor(scheduledEventTracker);
        ReflectionTestUtils.setField(processor, "circuitBreakerThreshold", 5);
        CaseDetails case1 = CaseDetailsBuilder.builder().id(1L).build();
        CaseDetails case2 = CaseDetailsBuilder.builder().id(2L).build();
        CaseDetails case3 = CaseDetailsBuilder.builder().id(3L).build();
        RuntimeException error = new RuntimeException("temporary downstream failure");
        RecordingScheduledTask task = new RecordingScheduledTask(
            Long.MAX_VALUE,
            new ScheduledTaskBackPressureConfiguration(
                Duration.ZERO,
                Duration.ofMillis(50),
                Duration.ofMillis(10),
                Duration.ZERO,
                Duration.ofMillis(5),
                Duration.ofSeconds(1)
            ),
            Map.of(1L, error)
        );

        ScheduledTaskEventConfiguration eventConfig = new ScheduledTaskEventConfiguration("JudgmentBuffer");
        ElasticSearchResult searchResult = new ElasticSearchResult(Stream.of(case1, case2, case3), 3);

        ScheduledTaskOutcome<Long> outcome = processor.performProcessing(eventConfig, task, searchResult);

        assertThat(outcome.abortedEarly()).isFalse();
        assertThat(outcome.failedCases()).containsExactly(1L);
        assertThat(outcome.succeededCases()).containsExactly(2L, 3L);
        assertThat(processor.delays()).containsExactly(Duration.ofMillis(10), Duration.ofMillis(5));

        verify(scheduledEventTracker).caseFailedEvent(eventConfig, "1", error);
        verify(scheduledEventTracker).caseProcessedEvent(eventConfig, "2");
        verify(scheduledEventTracker).caseProcessedEvent(eventConfig, "3");
    }

    @Test
    void shouldAbortProcessing_whenInterruptedDuringBackPressureDelay() {
        InterruptingScheduledTaskProcessor processor = new InterruptingScheduledTaskProcessor(scheduledEventTracker);
        CaseDetails case1 = CaseDetailsBuilder.builder().id(1L).build();
        RecordingScheduledTask task = new RecordingScheduledTask(
            Long.MAX_VALUE,
            new ScheduledTaskBackPressureConfiguration(
                Duration.ofMillis(1),
                Duration.ofMillis(1),
                Duration.ZERO,
                Duration.ZERO,
                Duration.ZERO,
                Duration.ZERO
            ),
            Map.of()
        );

        try {
            ScheduledTaskOutcome<Long> outcome = processor.performProcessing(
                new ScheduledTaskEventConfiguration("JudgmentBuffer"),
                task,
                new ElasticSearchResult(Stream.of(case1), 1)
            );

            assertThat(outcome.abortedEarly()).isTrue();
            assertThat(outcome.abortReason()).isEqualTo(
                "Scheduled task interrupted while applying backpressure"
            );
            assertThat(outcome.succeededCases()).isEmpty();
            assertThat(outcome.failedCases()).isEmpty();
            assertThat(task.processedCases()).isEmpty();
            assertThat(Thread.currentThread().isInterrupted()).isTrue();
            verifyNoInteractions(scheduledEventTracker);
        } finally {
            Thread.interrupted();
        }
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

        ScheduledTaskOutcome<Long> outcome = scheduledTaskProcessor.performProcessing(eventConfig, scheduledTask, searchResult);

        assertThat(outcome.abortedEarly()).isTrue();
        assertThat(outcome.abortReason()).isEqualTo("Error 2");
        assertThat(outcome.succeededCases()).isEmpty();
        assertThat(outcome.failedCases()).containsExactly(1L, 2L);

        verify(scheduledTask).accept(case1);
        verify(scheduledTask).accept(case2);

        verify(scheduledEventTracker).caseFailedEvent(eventConfig, "1", error1);
        verify(scheduledEventTracker).caseFailedEvent(eventConfig, "2", error2);
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
        doThrow(error1).when(scheduledTask).accept(argThat((CaseDetails c) -> c != null && c.getId().equals(101L)));
        doNothing().when(scheduledTask).accept(argThat((CaseDetails c) -> c != null && c.getId().equals(102L)));
        RuntimeException error3 = new RuntimeException("Error 3");
        doThrow(error3).when(scheduledTask).accept(argThat((CaseDetails c) -> c != null && c.getId().equals(103L)));

        ScheduledTaskEventConfiguration eventConfig = new ScheduledTaskEventConfiguration("JudgmentBuffer");
        ElasticSearchResult searchResult = new ElasticSearchResult(cases.stream(), 3);

        ScheduledTaskOutcome<Long> outcome = scheduledTaskProcessor.performProcessing(eventConfig, scheduledTask, searchResult);

        assertThat(outcome.abortedEarly()).isFalse();
        assertThat(outcome.succeededCases()).containsExactly(102L);
        assertThat(outcome.failedCases()).containsExactly(101L, 103L);

        verify(scheduledEventTracker).caseFailedEvent(eventConfig, "101", error1);
        verify(scheduledEventTracker).caseProcessedEvent(eventConfig, "102");
        verify(scheduledEventTracker).caseFailedEvent(eventConfig, "103", error3);
    }

    @Test
    void shouldHandleExceptionWithNullMessage() {
        ReflectionTestUtils.setField(scheduledTaskProcessor, "circuitBreakerThreshold", 1);

        CaseDetails case1 = CaseDetailsBuilder.builder().id(1L).build();
        ElasticSearchResult searchResult = new ElasticSearchResult(Stream.of(case1), 1);

        RuntimeException errorWithoutMessage = new RuntimeException();
        doThrow(errorWithoutMessage).when(scheduledTask).accept(case1);

        ScheduledTaskEventConfiguration eventConfig = new ScheduledTaskEventConfiguration("JudgmentBuffer");
        ScheduledTaskOutcome<Long> outcome = scheduledTaskProcessor.performProcessing(eventConfig, scheduledTask, searchResult);

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

        ScheduledTaskOutcome<Long> outcome = scheduledTaskProcessor.performProcessing(eventConfig, scheduledTask, searchResult);

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

        ScheduledTaskOutcome<Long> outcome = scheduledTaskProcessor.performProcessing(eventConfig, scheduledTask, searchResult);

        assertThat(outcome.abortedEarly()).isTrue();
        assertThat(outcome.failedCases()).containsExactly(1L);
        assertThat(outcome.succeededCases()).isEmpty();

        // Verify that case 2 was NEVER even attempted
        verify(scheduledTask, never()).accept(case2);
        verify(scheduledEventTracker, never()).caseProcessedEvent(any(), eq(2L));
        verify(scheduledEventTracker, never()).caseFailedEvent(any(), eq(2L), any());
    }

    private static class RecordingScheduledTask implements ScheduledTask<CaseDetails, Long> {

        private final long maxCasesPerRun;
        private final ScheduledTaskBackPressureConfiguration backPressureConfiguration;
        private final Map<Long, RuntimeException> failures;
        private final List<Long> processedCases = new ArrayList<>();

        RecordingScheduledTask(long maxCasesPerRun) {
            this(
                maxCasesPerRun,
                ScheduledTaskBackPressureConfiguration.disabled(),
                Map.of()
            );
        }

        RecordingScheduledTask(long maxCasesPerRun,
                               ScheduledTaskBackPressureConfiguration backPressureConfiguration,
                               Map<Long, RuntimeException> failures) {
            this.maxCasesPerRun = maxCasesPerRun;
            this.backPressureConfiguration = backPressureConfiguration;
            this.failures = failures;
        }

        @Override
        public Long getItemId(CaseDetails caseDetails) {
            return caseDetails.getId();
        }

        @Override
        public void accept(CaseDetails caseDetails) {
            processedCases.add(caseDetails.getId());
            RuntimeException failure = failures.get(caseDetails.getId());
            if (failure != null) {
                throw failure;
            }
        }

        @Override
        public long maxCasesPerRun() {
            return maxCasesPerRun;
        }

        @Override
        public ScheduledTaskBackPressureConfiguration backPressureConfiguration() {
            return backPressureConfiguration;
        }

        List<Long> processedCases() {
            return processedCases;
        }
    }

    private static class CapturingScheduledTaskProcessor extends ScheduledTaskProcessor<CaseDetails, Long> {

        private final List<Duration> delays = new ArrayList<>();

        CapturingScheduledTaskProcessor(ScheduledEventTracker eventTracker) {
            super(eventTracker);
        }

        @Override
        void sleep(Duration delay) {
            delays.add(delay);
        }

        List<Duration> delays() {
            return delays;
        }
    }

    private static class InterruptingScheduledTaskProcessor extends ScheduledTaskProcessor<CaseDetails, Long> {

        InterruptingScheduledTaskProcessor(ScheduledEventTracker eventTracker) {
            super(eventTracker);
        }

        @Override
        void sleep(Duration delay) throws InterruptedException {
            throw new InterruptedException();
        }
    }
}
