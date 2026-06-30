package uk.gov.hmcts.reform.civil.scheduler.common;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.search.common.ElasticSearchResult;

import java.util.function.Consumer;
import java.util.stream.Stream;

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
    private ArgumentCaptor<Consumer<CaseDetails>> taskCaptor;
    @InjectMocks
    private ElasticSearchSchedulerRunner runner;

    @Test
    void shouldRunScheduledTaskWhenSpringSchedulerFeatureToggleIsEnabled() {
        when(featureToggleService.isSpringSchedulerEnabled(SCHEDULER_NAME)).thenReturn(true);
        CaseDetails caseDetails = CaseDetails.builder().id(123L).build();
        ElasticSearchResult searchResult = new ElasticSearchResult(Stream.of(caseDetails), 1);
        ScheduledCaseTask scheduledCaseTask = mock(ScheduledCaseTask.class);

        runner.run(SCHEDULER_NAME, () -> searchResult, scheduledCaseTask::accept);

        verify(scheduledTaskRunner).run(
            eq(new ScheduledTaskEventConfiguration(SCHEDULER_NAME)),
            eq(searchResult),
            taskCaptor.capture()
        );
        taskCaptor.getValue().accept(caseDetails);
        verify(scheduledCaseTask).accept(caseDetails, 1);
    }

    @Test
    void shouldPassZeroTotalCasesWhenSearchResultIsNull() {
        when(featureToggleService.isSpringSchedulerEnabled(SCHEDULER_NAME)).thenReturn(true);
        CaseDetails caseDetails = CaseDetails.builder().id(123L).build();
        ScheduledCaseTask scheduledCaseTask = mock(ScheduledCaseTask.class);

        runner.run(SCHEDULER_NAME, () -> null, scheduledCaseTask::accept);

        verify(scheduledTaskRunner).run(
            eq(new ScheduledTaskEventConfiguration(SCHEDULER_NAME)),
            eq(null),
            taskCaptor.capture()
        );
        taskCaptor.getValue().accept(caseDetails);
        verify(scheduledCaseTask).accept(caseDetails, 0);
    }

    @Test
    void shouldNotRunScheduledTaskWhenSpringSchedulerFeatureToggleIsDisabled() {
        when(featureToggleService.isSpringSchedulerEnabled(SCHEDULER_NAME)).thenReturn(false);
        ScheduledCaseTask scheduledCaseTask = mock(ScheduledCaseTask.class);

        runner.run(SCHEDULER_NAME, () -> new ElasticSearchResult(Stream.empty(), 0), scheduledCaseTask::accept);

        verifyNoInteractions(scheduledTaskRunner, scheduledCaseTask);
    }

    private interface ScheduledCaseTask {
        void accept(CaseDetails caseDetails, int totalCases);
    }
}
