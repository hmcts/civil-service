package uk.gov.hmcts.reform.civil.scheduler.gadocumentuploadnotify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.ga.service.search.GaEvidenceUploadNotificationSearchService;
import uk.gov.hmcts.reform.civil.scheduler.common.ListTaskResult;
import uk.gov.hmcts.reform.civil.scheduler.common.ScheduledTaskRunner;
import uk.gov.hmcts.reform.civil.scheduler.common.TaskResult;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.when;
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
        CaseDetails caseDetails = CaseDetails.builder().id(1L).build();
        when(searchService.getApplications()).thenReturn(Set.of(caseDetails));

        scheduler.runScheduledTask();

        assertThat(scheduler.getName()).isEqualTo(GADocumentUploadNotifyScheduler.SCHEDULER_NAME);
        verify(scheduledTaskRunner).run(argThat(config -> {
            TaskResult<CaseDetails> result = config.getSearchResultSupplier().get();
            return GADocumentUploadNotifyScheduler.SCHEDULER_NAME.equals(config.getSchedulerName())
                && gaDocumentUploadNotifyScheduledTask.equals(config.getScheduledTask())
                && result instanceof ListTaskResult
                && result.totalResults() == 1
                && result.itemStream().toList().contains(caseDetails);
        }));
    }
}
