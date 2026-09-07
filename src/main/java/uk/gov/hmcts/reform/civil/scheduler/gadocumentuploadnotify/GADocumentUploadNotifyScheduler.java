package uk.gov.hmcts.reform.civil.scheduler.gadocumentuploadnotify;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.ga.service.search.GaEvidenceUploadNotificationSearchService;
import uk.gov.hmcts.reform.civil.scheduler.common.CivilScheduler;
import uk.gov.hmcts.reform.civil.scheduler.common.ListTaskResult;
import uk.gov.hmcts.reform.civil.scheduler.common.ScheduledTaskConfiguration;
import uk.gov.hmcts.reform.civil.scheduler.common.ScheduledTaskRunner;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class GADocumentUploadNotifyScheduler implements CivilScheduler {

    public static final String SCHEDULER_NAME = "GADocUploadNotifyScheduler";

    private final GaEvidenceUploadNotificationSearchService searchService;
    private final ScheduledTaskRunner<CaseDetails, Long> scheduledTaskRunner;
    private final GADocumentUploadNotifyScheduledTask gaDocumentUploadNotifyScheduledTask;

    @Override
    public String getName() {
        return SCHEDULER_NAME;
    }

    @Scheduled(cron = "${scheduler.ga-document-upload-notify.cronExpression}")
    @SchedulerLock(name = "GADocumentUploadNotifyScheduler_notifyDocumentUploads",
        lockAtMostFor = "${scheduler.lockAtMostFor}",
        lockAtLeastFor = "${scheduler.lockAtLeastFor}")
    @Override
    public void runScheduledTask() {
        scheduledTaskRunner.run(ScheduledTaskConfiguration.<CaseDetails, Long>builder()
            .schedulerName(SCHEDULER_NAME)
            .searchResultSupplier(() -> {
                List<CaseDetails> applications = searchService.getApplications().stream().toList();
                return new ListTaskResult<>(applications, applications.size());
            })
            .scheduledTask(gaDocumentUploadNotifyScheduledTask)
            .build());
    }
}
