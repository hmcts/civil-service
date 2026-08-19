package uk.gov.hmcts.reform.civil.scheduler.common;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.task.TaskExecutor;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SchedulerRegistryTest {

    @Mock
    private CivilScheduler scheduler1;

    @Mock
    private CivilScheduler scheduler2;

    @Mock
    private CivilScheduler scheduler1Duplicate;

    @Mock
    private TaskExecutor taskExecutor;

    @Mock
    private FeatureToggleService featureToggleService;

    @Captor
    private ArgumentCaptor<Runnable> runnableCaptor;

    private SchedulerRegistry repository;

    @BeforeEach
    void setUp() {
        when(scheduler1.getName()).thenReturn("scheduler1");
        when(scheduler2.getName()).thenReturn("scheduler2");
        lenient().when(featureToggleService.isSpringSchedulerEnabled(anyString())).thenReturn(true);
        repository = new SchedulerRegistry(List.of(scheduler1, scheduler2), taskExecutor, featureToggleService);
    }

    @Test
    void shouldRunScheduler_whenSchedulerExists() {
        boolean result = repository.runScheduler("scheduler1");

        assertThat(result).isTrue();
        verify(taskExecutor).execute(runnableCaptor.capture());

        // Execute the runnable to verify it triggers the task
        runnableCaptor.getValue().run();

        verify(scheduler1).runScheduledTask();
        verify(scheduler2, never()).runScheduledTask();
    }

    @Test
    void shouldNotRunAnything_whenSchedulerDoesNotExist() {
        boolean result = repository.runScheduler("nonExistent");

        assertThat(result).isFalse();
        verify(scheduler1, never()).runScheduledTask();
        verify(scheduler2, never()).runScheduledTask();
    }

    @Test
    void shouldNotRunAnything_whenSchedulerIsNotActive() {
        when(featureToggleService.isSpringSchedulerEnabled("scheduler1")).thenReturn(false);

        boolean result = repository.runScheduler("scheduler1");

        assertThat(result).isFalse();
        verify(taskExecutor, never()).execute(any(Runnable.class));
        verify(scheduler1, never()).runScheduledTask();
        verify(scheduler2, never()).runScheduledTask();
    }

    @Test
    void shouldReturnSchedulerNames() {
        assertThat(repository.getSchedulerNames()).containsExactlyInAnyOrder("scheduler1", "scheduler2");
    }

    @Test
    void shouldReturnActiveSchedulerNamesOnly() {
        when(featureToggleService.isSpringSchedulerEnabled("scheduler1")).thenReturn(true);
        when(featureToggleService.isSpringSchedulerEnabled("scheduler2")).thenReturn(false);

        assertThat(repository.getSchedulerNames()).containsExactlyInAnyOrder("scheduler1");
    }

    @Test
    void shouldIgnoreDuplicateNamedSchedulers() {
        when(scheduler1Duplicate.getName()).thenReturn("scheduler1");
        repository = new SchedulerRegistry(
            List.of(scheduler1, scheduler2, scheduler1Duplicate),
            taskExecutor,
            featureToggleService
        );
        assertThat(repository.getSchedulerNames()).containsExactlyInAnyOrder("scheduler1", "scheduler2");

        boolean result = repository.runScheduler("scheduler1");

        assertThat(result).isTrue();
        verify(taskExecutor).execute(runnableCaptor.capture());

        // Execute the runnable to verify it triggers the task
        runnableCaptor.getValue().run();

        verify(scheduler1).runScheduledTask();
        verify(scheduler2, never()).runScheduledTask();
        verify(scheduler1Duplicate, never()).runScheduledTask();
    }
}
