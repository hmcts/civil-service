package uk.gov.hmcts.reform.civil.scheduler.evidenceupload;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.scheduler.common.ScheduledTaskRunner;
import uk.gov.hmcts.reform.civil.service.search.EvidenceUploadNotificationSearchService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.civil.scheduler.evidenceupload.EvidenceUploadScheduler.SCHEDULER_NAME;

@ExtendWith(MockitoExtension.class)
class EvidenceUploadSchedulerTest {

    @Mock
    private EvidenceUploadNotificationSearchService searchService;

    @Mock
    private ScheduledTaskRunner<CaseDetails, Long> scheduledTaskRunner;

    @Mock
    private EvidenceUploadSchedulerTask evidenceUploadSchedulerTask;

    @InjectMocks
    private EvidenceUploadScheduler scheduler;

    @Test
    void shouldRunTaskRunner_whenSchedulerIsEnabled() {
        scheduler.runScheduledTask();

        verify(scheduledTaskRunner).run(
            eq(SCHEDULER_NAME),
            any(),
            eq(evidenceUploadSchedulerTask)
        );
    }
}
