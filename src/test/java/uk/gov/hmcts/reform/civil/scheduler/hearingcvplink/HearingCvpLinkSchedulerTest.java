package uk.gov.hmcts.reform.civil.scheduler.hearingcvplink;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.scheduler.common.ElasticSearchSchedulerRunner;
import uk.gov.hmcts.reform.civil.service.search.CaseHearingDateSearchService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class HearingCvpLinkSchedulerTest {

    private static final String SCHEDULER_NAME = "HearingCvpLink";

    @Mock
    private CaseHearingDateSearchService searchService;

    @Mock
    private HearingCvpLinkScheduledTask hearingCvpLinkScheduledTask;

    @Mock
    private ElasticSearchSchedulerRunner elasticSearchSchedulerRunner;

    @InjectMocks
    private HearingCvpLinkScheduler scheduler;

    @Test
    void shouldRunTaskRunnerWhenScheduledTaskRuns() {
        scheduler.runScheduledTask();

        verify(elasticSearchSchedulerRunner).run(
            eq(SCHEDULER_NAME),
            any(),
            any()
        );
    }
}
