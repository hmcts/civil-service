package uk.gov.hmcts.reform.civil.scheduler.judgementbuffer;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.scheduler.common.ScheduledTaskEventConfiguration;
import uk.gov.hmcts.reform.civil.scheduler.common.ScheduledTaskRunner;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.civil.scheduler.judgementbuffer.JudgementBufferScheduler.SCHEDULER_NAME;

@ExtendWith(MockitoExtension.class)
class JudgementBufferSchedulerTest {

    @Mock
    private ScheduledTaskRunner scheduledTaskRunner;

    @Mock
    private JudgementBufferScheduledTask judgementBufferScheduledTask;

    @InjectMocks
    private JudgementBufferScheduler scheduler;

    @Nested
    class Execute {

        @Test
        void shouldRunTaskRunner_whenSchedulerIsEnabled() {
            ScheduledTaskEventConfiguration expectedConfig = new ScheduledTaskEventConfiguration(SCHEDULER_NAME);

            scheduler.issueJudgement();

            verify(scheduledTaskRunner).run(
                eq(expectedConfig),
                any(),
                eq(judgementBufferScheduledTask)
            );
        }
    }
}
