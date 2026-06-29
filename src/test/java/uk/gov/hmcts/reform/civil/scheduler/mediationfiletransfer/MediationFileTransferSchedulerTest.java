package uk.gov.hmcts.reform.civil.scheduler.mediationfiletransfer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.civil.scheduler.common.ScheduledEventTracker;
import uk.gov.hmcts.reform.civil.scheduler.common.ScheduledTaskEventConfiguration;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import java.util.List;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MediationFileTransferSchedulerTest {

    private static final String SCHEDULER_NAME = "GenerateCsvAndSendToMmt";

    @Mock
    private MediationFileTransferScheduledTask scheduledTask;
    @Mock
    private ScheduledEventTracker eventTracker;
    @Mock
    private FeatureToggleService featureToggleService;

    private MediationFileTransferScheduler scheduler;
    private ScheduledTaskEventConfiguration eventConfiguration;

    @BeforeEach
    void setUp() {
        scheduler = new MediationFileTransferScheduler(scheduledTask, eventTracker, featureToggleService);
        ReflectionTestUtils.setField(scheduler, "circuitBreakerThreshold", 2);
        eventConfiguration = new ScheduledTaskEventConfiguration(SCHEDULER_NAME);
    }

    @Test
    void shouldRunCsvAndJsonMediationFileTransferTasks() {
        when(featureToggleService.isSpringSchedulerEnabled(SCHEDULER_NAME)).thenReturn(true);
        when(scheduledTask.generateCsvAndTransfer(2)).thenReturn(
            new MediationFileTransferResult(List.of("1"), List.of("1"), List.of(), false, null)
        );
        when(scheduledTask.generateJsonAndTransfer(2)).thenReturn(
            new MediationFileTransferResult(List.of("2"), List.of("2"), List.of(), false, null)
        );

        scheduler.runScheduledTask();

        verify(eventTracker).jobStartedEvent(eventConfiguration, 2);
        verify(eventTracker).caseProcessedEvent(eventConfiguration, "1");
        verify(eventTracker).caseProcessedEvent(eventConfiguration, "2");
        verify(eventTracker).jobCompletedEvent(eventConfiguration, 2, 2, 0);
    }

    @Test
    void shouldTrackCompletedNoCasesWhenNoCasesAreFound() {
        when(featureToggleService.isSpringSchedulerEnabled(SCHEDULER_NAME)).thenReturn(true);
        when(scheduledTask.generateCsvAndTransfer(2)).thenReturn(
            new MediationFileTransferResult(List.of(), List.of(), List.of(), false, null)
        );
        when(scheduledTask.generateJsonAndTransfer(2)).thenReturn(
            new MediationFileTransferResult(List.of(), List.of(), List.of(), false, null)
        );

        scheduler.runScheduledTask();

        verify(eventTracker).jobStartedEvent(eventConfiguration, 0);
        verify(eventTracker).jobCompletedNoCasesEvent(eventConfiguration);
        verify(eventTracker, never()).jobCompletedEvent(eventConfiguration, 0, 0, 0);
    }

    @Test
    void shouldTrackFailuresAndAbortWhenTaskResultAborted() {
        RuntimeException exception = new RuntimeException("failed");
        when(featureToggleService.isSpringSchedulerEnabled(SCHEDULER_NAME)).thenReturn(true);
        when(scheduledTask.generateCsvAndTransfer(2)).thenReturn(
            new MediationFileTransferResult(
                List.of("1", "2"),
                List.of("1"),
                List.of(new MediationFileTransferResult.FailedCase("2", exception)),
                true,
                "failed"
            )
        );
        when(scheduledTask.generateJsonAndTransfer(2)).thenReturn(
            new MediationFileTransferResult(List.of(), List.of(), List.of(), false, null)
        );

        scheduler.runScheduledTask();

        verify(eventTracker).caseProcessedEvent(eventConfiguration, "1");
        verify(eventTracker).caseFailedEvent(eventConfiguration, "2", exception);
        verify(eventTracker).jobAbortedEvent(eventConfiguration, 2, 1, 1, "failed");
    }

    @Test
    void shouldNotRunWhenSpringSchedulerFeatureToggleIsDisabled() {
        when(featureToggleService.isSpringSchedulerEnabled(SCHEDULER_NAME)).thenReturn(false);

        scheduler.runScheduledTask();

        verifyNoInteractions(scheduledTask, eventTracker);
    }
}
