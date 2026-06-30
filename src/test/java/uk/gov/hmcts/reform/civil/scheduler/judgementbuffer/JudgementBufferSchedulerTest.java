package uk.gov.hmcts.reform.civil.scheduler.judgementbuffer;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.scheduler.common.ScheduledTaskEventConfiguration;
import uk.gov.hmcts.reform.civil.scheduler.common.ScheduledTaskRunner;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.search.JudgementBufferExpiredSearchService;
import uk.gov.hmcts.reform.civil.service.search.common.ElasticSearchResult;

import java.util.stream.Stream;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.scheduler.judgementbuffer.JudgementBufferScheduler.SCHEDULER_NAME;

@ExtendWith(MockitoExtension.class)
class JudgementBufferSchedulerTest {

    @Mock
    private JudgementBufferExpiredSearchService searchService;

    @Mock
    private ScheduledTaskRunner scheduledTaskRunner;

    @Mock
    private JudgementBufferScheduledTask judgementBufferScheduledTask;

    @Mock
    private FeatureToggleService featureToggleService;

    @InjectMocks
    private JudgementBufferScheduler scheduler;

    @Nested
    class Execute {

        @Test
        void shouldRunTaskRunner_whenSchedulerIsEnabledAndFeatureToggleIsEnabled() {
            when(featureToggleService.isJudgmentBufferEnabled()).thenReturn(true);
            when(featureToggleService.isSpringSchedulerEnabled(SCHEDULER_NAME)).thenReturn(true);

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
            when(featureToggleService.isJudgmentBufferEnabled()).thenReturn(false);

            scheduler.runScheduledTask();

            verifyNoInteractions(scheduledTaskRunner);
        }

        @Test
        void shouldNotRunTaskRunner_whenSpringSchedulerIsDisabled() {
            when(featureToggleService.isJudgmentBufferEnabled()).thenReturn(true);
            when(featureToggleService.isSpringSchedulerEnabled(SCHEDULER_NAME)).thenReturn(false);

            scheduler.runScheduledTask();

            verifyNoInteractions(scheduledTaskRunner);
        }
    }
}
