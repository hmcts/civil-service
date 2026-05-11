package uk.gov.hmcts.reform.civil.scheduler.common;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.sampledata.CaseDetailsBuilder;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
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
        Set<CaseDetails> cases = new LinkedHashSet<>(List.of(case1, case2, case3));

        ScheduledTaskOutcome outcome = scheduledTaskProcessor.performProcessing(eventConfig, scheduledTask, cases);

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
    void shouldAbortEarly_whenConsecutiveFailuresThresholdReached() {
        ReflectionTestUtils.setField(scheduledTaskProcessor, "circuitBreakerThreshold", 2);

        CaseDetails case1 = CaseDetailsBuilder.builder().id(1L).build();
        CaseDetails case2 = CaseDetailsBuilder.builder().id(2L).build();
        CaseDetails case3 = CaseDetailsBuilder.builder().id(3L).build();
        CaseDetails case4 = CaseDetailsBuilder.builder().id(4L).build();
        Set<CaseDetails> cases = new LinkedHashSet<>(List.of(case1, case2, case3, case4));

        // Fail first two cases
        RuntimeException error1 = new RuntimeException("Error 1");
        RuntimeException error2 = new RuntimeException("Error 2");
        doThrow(error1).when(scheduledTask).accept(case1);
        doThrow(error2).when(scheduledTask).accept(case2);

        ScheduledTaskEventConfiguration eventConfig = new ScheduledTaskEventConfiguration("JudgmentBuffer");

        ScheduledTaskOutcome outcome = scheduledTaskProcessor.performProcessing(eventConfig, scheduledTask, cases);

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
}
