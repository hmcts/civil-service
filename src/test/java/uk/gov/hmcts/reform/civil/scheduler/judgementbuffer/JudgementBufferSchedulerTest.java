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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

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
            when(searchService.getSearchResults()).thenReturn(new ElasticSearchResult(0, Stream.empty()));

            scheduler.runScheduledTask();

            ScheduledTaskEventConfiguration expectedConfig = new ScheduledTaskEventConfiguration(scheduler.getName());
            verify(scheduledTaskRunner).run(
                eq(expectedConfig),
                any(ElasticSearchResult.class),
                eq(judgementBufferScheduledTask)
            );
        }

        @Test
        void shouldNotRunTaskRunner_whenSchedulerIsEnabledAndFeatureToggleIsDisabled() {
            when(featureToggleService.isJudgmentBufferEnabled()).thenReturn(false);

            scheduler.runScheduledTask();

            verifyNoInteractions(scheduledTaskRunner);
        }
    }
}
