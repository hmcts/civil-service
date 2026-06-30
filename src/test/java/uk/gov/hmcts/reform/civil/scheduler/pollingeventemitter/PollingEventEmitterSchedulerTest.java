package uk.gov.hmcts.reform.civil.scheduler.pollingeventemitter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.scheduler.common.ScheduledTaskEventConfiguration;
import uk.gov.hmcts.reform.civil.scheduler.common.ScheduledTaskRunner;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.search.CaseReadyBusinessProcessSearchService;
import uk.gov.hmcts.reform.civil.service.search.common.ElasticSearchResult;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PollingEventEmitterSchedulerTest {

    private static final long CASE_ID = 123L;
    private static final String SCHEDULER_NAME = "PollingEventEmitter";

    @Mock
    private CaseReadyBusinessProcessSearchService searchService;

    @Mock
    private ScheduledTaskRunner scheduledTaskRunner;

    @Mock
    private PollingEventEmitterScheduledTask pollingEventEmitterScheduledTask;

    @Mock
    private FeatureToggleService featureToggleService;

    private PollingEventEmitterScheduler scheduler;

    @BeforeEach
    void setUp() {
        scheduler = new PollingEventEmitterScheduler(
            searchService,
            scheduledTaskRunner,
            pollingEventEmitterScheduledTask,
            featureToggleService
        );
    }

    @Test
    void shouldRunPollingEventEmitterScheduler() {
        when(featureToggleService.isSpringSchedulerEnabled(SCHEDULER_NAME)).thenReturn(true);
        CaseDetails caseDetails = CaseDetails.builder().id(CASE_ID).build();
        ElasticSearchResult searchResult = new ElasticSearchResult(List.of(caseDetails).stream(), 1);
        when(searchService.getElasticSearchResult()).thenReturn(searchResult);

        scheduler.runScheduledTask();

        verify(scheduledTaskRunner).run(
            new ScheduledTaskEventConfiguration("PollingEventEmitter"),
            searchResult,
            pollingEventEmitterScheduledTask
        );
    }

    @Test
    void shouldPassNullSearchResultToRunner() {
        when(featureToggleService.isSpringSchedulerEnabled(SCHEDULER_NAME)).thenReturn(true);
        when(searchService.getElasticSearchResult()).thenReturn(null);

        scheduler.runScheduledTask();

        verify(scheduledTaskRunner).run(
            new ScheduledTaskEventConfiguration("PollingEventEmitter"),
            null,
            pollingEventEmitterScheduledTask
        );
        verify(pollingEventEmitterScheduledTask, never()).accept(any());
    }

    @Test
    void shouldNotRunPollingEventEmitterSchedulerWhenSpringSchedulerFeatureToggleIsDisabled() {
        when(featureToggleService.isSpringSchedulerEnabled(SCHEDULER_NAME)).thenReturn(false);

        scheduler.runScheduledTask();

        verifyNoInteractions(searchService, scheduledTaskRunner, pollingEventEmitterScheduledTask);
    }
}
