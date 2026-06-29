package uk.gov.hmcts.reform.civil.scheduler.hearingfee;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.scheduler.common.ScheduledTaskEventConfiguration;
import uk.gov.hmcts.reform.civil.scheduler.common.ScheduledTaskRunner;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.search.HearingFeeDueSearchService;
import uk.gov.hmcts.reform.civil.service.search.common.ElasticSearchResult;

import java.util.stream.Stream;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HearingFeeSchedulerTest {

    @Mock
    private HearingFeeDueSearchService searchService;

    @Mock
    private ScheduledTaskRunner scheduledTaskRunner;

    @Mock
    private HearingFeeSchedulerTask judgementBufferScheduledTask;

    @Mock
    private FeatureToggleService featureToggleService;

    @InjectMocks
    private HearingFeeScheduler scheduler;

    @Nested
    class Execute {

        @Test
        void shouldRunTaskRunner_whenSchedulerIsEnabledAndFeatureToggleIsEnabled() {
            when(featureToggleService.isSpringSchedulerEnabled()).thenReturn(true);

            ScheduledTaskEventConfiguration expectedConfig = new ScheduledTaskEventConfiguration(scheduler.getName());
            ElasticSearchResult elasticSearchResult = new ElasticSearchResult(Stream.empty(), 0);
            when(searchService.getElasticSearchResult()).thenReturn(elasticSearchResult);

            scheduler.runScheduledTask();

            verify(scheduledTaskRunner).run(
                expectedConfig,
                elasticSearchResult,
                judgementBufferScheduledTask
            );
        }

        @Test
        void shouldNotRunTaskRunner_whenSchedulerIsEnabledAndFeatureToggleIsDisabled() {
            when(featureToggleService.isSpringSchedulerEnabled()).thenReturn(false);

            scheduler.runScheduledTask();

            verifyNoInteractions(scheduledTaskRunner);
        }
    }
}
