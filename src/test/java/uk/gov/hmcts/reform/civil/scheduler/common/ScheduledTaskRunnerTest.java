package uk.gov.hmcts.reform.civil.scheduler.common;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.sampledata.CaseDetailsBuilder;
import uk.gov.hmcts.reform.civil.service.search.ElasticSearchService;

import java.util.Set;
import java.util.LinkedHashSet;
import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
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

    @InjectMocks
    private ScheduledTaskRunner scheduledTaskRunner;

    @Test
    void shouldEmitJudgmentBufferEvents_whenConfigurationProvided() {
        ReflectionTestUtils.setField(scheduledTaskRunner, "circuitBreakerThreshold", 5);

        CaseDetails case1 = CaseDetailsBuilder.builder().id(1L).build();
        CaseDetails case2 = CaseDetailsBuilder.builder().id(2L).build();
        when(searchService.getCases()).thenReturn(new LinkedHashSet<>(List.of(case1, case2)));

        // Throw exception for first case, succeed for second
        RuntimeException exception = new RuntimeException("Lock conflict");
        doThrow(exception).when(scheduledTask).accept(case1);

        ScheduledTaskEventConfiguration eventConfig = new ScheduledTaskEventConfiguration("JudgmentBuffer");

        scheduledTaskRunner.run(eventConfig, searchService::getCases, scheduledTask);

        verify(scheduledEventTracker).jobStartedEvent(eq(eventConfig), eq(2));

        verify(scheduledEventTracker).caseFailedEvent(eq(eventConfig), eq(case1), eq(exception));

        verify(scheduledEventTracker).caseProcessedEvent(eq(eventConfig), eq(2L));

        verify(scheduledEventTracker).jobCompletedEvent(eq(eventConfig), eq(Set.of(case1, case2)), eq(List.of(2L)), eq(List.of(1L)));
    }

    @Test
    void shouldAbortEarly_whenConsecutiveFailuresThresholdReached() {
        CaseDetails case1 = CaseDetailsBuilder.builder().id(1L).build();
        CaseDetails case2 = CaseDetailsBuilder.builder().id(2L).build();
        CaseDetails case3 = CaseDetailsBuilder.builder().id(3L).build();
        CaseDetails case4 = CaseDetailsBuilder.builder().id(4L).build();
        Set<CaseDetails> cases = new LinkedHashSet<>(List.of(case1, case2, case3, case4));
        when(searchService.getCases()).thenReturn(cases);

        ReflectionTestUtils.setField(scheduledTaskRunner, "circuitBreakerThreshold", 2);

        // Fail first two cases
        doThrow(new RuntimeException("Error 1")).when(scheduledTask).accept(case1);
        doThrow(new RuntimeException("Error 2")).when(scheduledTask).accept(case2);

        ScheduledTaskEventConfiguration eventConfig = new ScheduledTaskEventConfiguration("JudgmentBuffer");

        scheduledTaskRunner.run(eventConfig, searchService::getCases, scheduledTask);

        verify(scheduledEventTracker).jobAbortedEvent(eq(eventConfig), eq(cases), eq(List.of()), eq(List.of(1L, 2L)), eq("Error 2"));

        // Verify case3 and case4 were NOT processed
        verify(scheduledTask).accept(case1);
        verify(scheduledTask).accept(case2);
        verifyNoMoreInteractions(scheduledTask);
    }

    @Test
    void shouldNotAbortEarly_whenFailuresAreNotConsecutive() {
        CaseDetails case1 = CaseDetailsBuilder.builder().id(1L).build();
        CaseDetails case2 = CaseDetailsBuilder.builder().id(2L).build();
        CaseDetails case3 = CaseDetailsBuilder.builder().id(3L).build();
        Set<CaseDetails> cases = new LinkedHashSet<>(List.of(case1, case2, case3));
        when(searchService.getCases()).thenReturn(cases);

        ReflectionTestUtils.setField(scheduledTaskRunner, "circuitBreakerThreshold", 2);

        // Fail first, succeed second, fail third
        RuntimeException error1 = new RuntimeException("Error 1");
        doThrow(error1).when(scheduledTask).accept(case1);
        // case2 succeeds (default)
        RuntimeException error3 = new RuntimeException("Error 3");
        doThrow(error3).when(scheduledTask).accept(case3);

        ScheduledTaskEventConfiguration eventConfig = new ScheduledTaskEventConfiguration("JudgmentBuffer");

        scheduledTaskRunner.run(eventConfig, searchService::getCases, scheduledTask);

        verify(scheduledEventTracker).jobCompletedEvent(eq(eventConfig), eq(cases), eq(List.of(2L)), eq(List.of(1L, 3L)));

        // Verify caseFailedEvent was called for each failure
        verify(scheduledEventTracker).caseFailedEvent(eq(eventConfig), eq(case1), eq(error1));
        verify(scheduledEventTracker).caseFailedEvent(eq(eventConfig), eq(case3), eq(error3));

        // All cases should have been processed
        verify(scheduledTask).accept(case1);
        verify(scheduledTask).accept(case2);
        verify(scheduledTask).accept(case3);
    }
}
