package uk.gov.hmcts.reform.civil.scheduler.common;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.task.TaskExecutor;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TestingSupportSchedulerRepositoryTest {

    @Mock
    private CivilScheduler scheduler1;

    @Mock
    private CivilScheduler scheduler2;

    @Mock
    private TaskExecutor taskExecutor;

    @Captor
    private ArgumentCaptor<Runnable> runnableCaptor;

    private TestingSupportSchedulerRepository repository;

    @BeforeEach
    void setUp() {
        when(scheduler1.getName()).thenReturn("scheduler1");
        when(scheduler2.getName()).thenReturn("scheduler2");
        repository = new TestingSupportSchedulerRepository(List.of(scheduler1, scheduler2), taskExecutor);
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
    void shouldReturnSchedulerNames() {
        List<String> names = repository.getSchedulerNames();

        assertThat(names).containsExactlyInAnyOrder("scheduler1", "scheduler2");
    }
}
