package uk.gov.hmcts.reform.civil.scheduler.common;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.search.common.ElasticSearchResult;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ElasticSearchSchedulerRunnerTest {

    private static final String SCHEDULER_NAME = "TestScheduler";

    @Mock
    private ScheduledTaskRunner scheduledTaskRunner;
    @Mock
    private FeatureToggleService featureToggleService;
    @Captor
    private ArgumentCaptor<ScheduledTask> taskCaptor;
    @InjectMocks
    private ElasticSearchSchedulerRunner runner;

    @Test
    void shouldRunScheduledTaskWhenSpringSchedulerFeatureToggleIsEnabled() {
        when(featureToggleService.isSpringSchedulerEnabled(SCHEDULER_NAME)).thenReturn(true);
        ElasticSearchResult searchResult = new ElasticSearchResult(Stream.empty(), 1);
        ScheduledTask scheduledTask = mock(ScheduledTask.class);

        runner.run(SCHEDULER_NAME, () -> searchResult, scheduledTask);

        verify(scheduledTaskRunner).run(
            eq(new ScheduledTaskEventConfiguration(SCHEDULER_NAME)),
            eq(searchResult),
            taskCaptor.capture()
        );
        assertThat(taskCaptor.getValue()).isSameAs(scheduledTask);
    }

    @Test
    void shouldPassNullSearchResultToRunner() {
        when(featureToggleService.isSpringSchedulerEnabled(SCHEDULER_NAME)).thenReturn(true);
        ScheduledTask scheduledTask = mock(ScheduledTask.class);

        runner.run(SCHEDULER_NAME, () -> null, scheduledTask);

        verify(scheduledTaskRunner).run(
            eq(new ScheduledTaskEventConfiguration(SCHEDULER_NAME)),
            eq(null),
            taskCaptor.capture()
        );
        assertThat(taskCaptor.getValue()).isSameAs(scheduledTask);
    }

    @Test
    void shouldNotRunScheduledTaskWhenSpringSchedulerFeatureToggleIsDisabled() {
        when(featureToggleService.isSpringSchedulerEnabled(SCHEDULER_NAME)).thenReturn(false);
        ScheduledTask scheduledTask = mock(ScheduledTask.class);

        runner.run(SCHEDULER_NAME, () -> new ElasticSearchResult(Stream.empty(), 0), scheduledTask);

        verifyNoInteractions(scheduledTaskRunner, scheduledTask);
    }
}
