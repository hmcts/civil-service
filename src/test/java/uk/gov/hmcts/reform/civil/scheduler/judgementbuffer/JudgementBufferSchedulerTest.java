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

import java.util.function.Supplier;

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

        @SuppressWarnings("unchecked")
        @Test
        void shouldRunTaskRunner_whenSchedulerIsEnabledAndFeatureToggleIsEnabled() {
            ScheduledTaskEventConfiguration expectedConfig = new ScheduledTaskEventConfiguration(scheduler.getName());
            when(featureToggleService.isJudgmentBufferEnabled()).thenReturn(true);

            scheduler.runScheduledTask();

            verify(scheduledTaskRunner).run(
                eq(expectedConfig),
                any(Supplier.class),
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
