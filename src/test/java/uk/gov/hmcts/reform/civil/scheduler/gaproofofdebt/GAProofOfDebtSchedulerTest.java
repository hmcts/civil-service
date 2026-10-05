package uk.gov.hmcts.reform.civil.scheduler.gaproofofdebt;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.scheduler.common.ScheduledTaskRunner;
import uk.gov.hmcts.reform.civil.service.search.CoscApplicationSearchService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class GAProofOfDebtSchedulerTest {

    @Mock
    private CoscApplicationSearchService searchService;
    @Mock
    private ScheduledTaskRunner<CaseDetails, Long> scheduledTaskRunner;
    @Mock
    private GAProofOfDebtScheduledTask gaProofOfDebtScheduledTask;
    @InjectMocks
    private GAProofOfDebtScheduler scheduler;

    @Test
    void shouldRunGAProofOfDebtTask() {
        scheduler.runScheduledTask();

        assertThat(scheduler.getName()).isEqualTo(GAProofOfDebtScheduler.SCHEDULER_NAME);
        verify(scheduledTaskRunner).run(
            eq(GAProofOfDebtScheduler.SCHEDULER_NAME),
            any(),
            eq(gaProofOfDebtScheduledTask)
        );
    }
}
