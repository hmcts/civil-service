package uk.gov.hmcts.reform.civil.scheduler.gadocumentuploadnotify;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.ga.service.search.GaEvidenceUploadNotificationSearchService;
import uk.gov.hmcts.reform.civil.scheduler.common.CivilScheduler;
import uk.gov.hmcts.reform.civil.scheduler.common.ListTaskResult;
import uk.gov.hmcts.reform.civil.scheduler.common.ScheduledTaskRunner;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(prefix = "scheduler.ga-document-upload-notify", name = "enabled", havingValue = "true")
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
        scheduledTaskRunner.run(
            SCHEDULER_NAME,
            () -> {
                List<CaseDetails> applications = searchService.getApplications().stream().toList();
                return new ListTaskResult<>(applications, applications.size());
            },
            gaDocumentUploadNotifyScheduledTask
        );
    }
}
