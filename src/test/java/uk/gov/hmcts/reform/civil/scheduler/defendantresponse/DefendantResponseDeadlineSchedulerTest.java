package uk.gov.hmcts.reform.civil.scheduler.defendantresponse;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.scheduler.common.ScheduledTaskEventConfiguration;
import uk.gov.hmcts.reform.civil.scheduler.common.ScheduledTaskRunner;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.search.DefendantResponseDeadlineCheckSearchService;
import uk.gov.hmcts.reform.civil.service.search.common.ElasticSearchResult;

import java.util.stream.Stream;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DefendantResponseDeadlineSchedulerTest {

    @Mock
    private DefendantResponseDeadlineCheckSearchService searchService;

    @Mock
    private ScheduledTaskRunner scheduledTaskRunner;

    @Mock
    private DefendantResponseDeadlineTask defendantResponseDeadlineTask;

    @Mock
    private FeatureToggleService featureToggleService;

    @InjectMocks
    private DefendantResponseDeadlineScheduler scheduler;

    @Test
    void shouldRunTaskRunner_whenDeadlineCheckIsCalledAndFeatureIsEnabled() {
        when(featureToggleService.isSpringSchedulerEnabled(DefendantResponseDeadlineScheduler.SCHEDULER_NAME))
            .thenReturn(true);
        ScheduledTaskEventConfiguration expectedConfig = new ScheduledTaskEventConfiguration(scheduler.getName());

        ElasticSearchResult elasticSearchResult = new ElasticSearchResult(Stream.empty(), 0);
        when(searchService.getElasticSearchResult()).thenReturn(elasticSearchResult);

        scheduler.runScheduledTask();

        verify(scheduledTaskRunner).run(
            expectedConfig,
            elasticSearchResult,
            defendantResponseDeadlineTask
        );
    }

    @Test
    void shouldNotRunTaskRunner_whenDeadlineCheckIsCalledAndFeatureIsDisabled() {
        when(featureToggleService.isSpringSchedulerEnabled(DefendantResponseDeadlineScheduler.SCHEDULER_NAME))
            .thenReturn(false);

        scheduler.runScheduledTask();

        verifyNoInteractions(scheduledTaskRunner);
        verifyNoInteractions(searchService);
        verifyNoInteractions(defendantResponseDeadlineTask);
    }
}
