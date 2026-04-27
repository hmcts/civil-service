package uk.gov.hmcts.reform.civil.scheduler.issuejudgement;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.civil.scheduler.common.ScheduledTaskEventConfiguration;
import uk.gov.hmcts.reform.civil.scheduler.common.ScheduledTaskRunner;
import uk.gov.hmcts.reform.civil.service.search.JudgmentRequestedSearchService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.civil.scheduler.issuejudgement.JudgementBufferScheduler.SCHEDULER_NAME;

@ExtendWith(MockitoExtension.class)
class JudgementBufferSchedulerTest {

    @Mock
    private JudgmentRequestedSearchService searchService;

    @Mock
    private ScheduledTaskRunner scheduledTaskRunner;

    @Mock
    private JudgementBufferScheduledTask judgementBufferScheduledTask;

    @InjectMocks
    private JudgementBufferScheduler scheduler;

    @Nested
    class Execute {

        @BeforeEach
        void setup() {
            ReflectionTestUtils.setField(scheduler, "isSchedulerEnabled", true);
        }

        @Test
        void shouldRunTaskRunner_whenSchedulerIsEnabled() {
            ScheduledTaskEventConfiguration expectedConfig = new ScheduledTaskEventConfiguration(SCHEDULER_NAME);

            scheduler.issueJudgement();

            verify(scheduledTaskRunner).run(
                expectedConfig,
                searchService::getCases,
                judgementBufferScheduledTask
            );
        }

        @Test
        void shouldNotExecute_whenSchedulerIsDisabled() {
            ReflectionTestUtils.setField(scheduler, "isSchedulerEnabled", false);

            scheduler.issueJudgement();

            verify(searchService, never()).getCases();
            verify(scheduledTaskRunner, never()).run(any(), any(), any());
        }
    }
}
