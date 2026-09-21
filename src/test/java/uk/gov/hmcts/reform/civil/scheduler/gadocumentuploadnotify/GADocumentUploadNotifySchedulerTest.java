package uk.gov.hmcts.reform.civil.scheduler.gadocumentuploadnotify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.ga.service.search.GaEvidenceUploadNotificationSearchService;
import uk.gov.hmcts.reform.civil.scheduler.common.ScheduledTaskRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class GADocumentUploadNotifySchedulerTest {

    @Mock
    private GaEvidenceUploadNotificationSearchService searchService;
    @Mock
    private ScheduledTaskRunner<CaseDetails, Long> scheduledTaskRunner;
    @Mock
    private GADocumentUploadNotifyScheduledTask gaDocumentUploadNotifyScheduledTask;
    @InjectMocks
    private GADocumentUploadNotifyScheduler scheduler;

    @Test
    void shouldRunGADocumentUploadNotifyTask() {
        scheduler.runScheduledTask();

        assertThat(scheduler.getName()).isEqualTo(GADocumentUploadNotifyScheduler.SCHEDULER_NAME);
        verify(scheduledTaskRunner).run(
            eq(GADocumentUploadNotifyScheduler.SCHEDULER_NAME),
            any(),
            eq(gaDocumentUploadNotifyScheduledTask)
        );
    }
}
