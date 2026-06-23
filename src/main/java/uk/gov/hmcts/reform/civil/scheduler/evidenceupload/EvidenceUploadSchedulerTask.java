package uk.gov.hmcts.reform.civil.scheduler.evidenceupload;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.event.EvidenceUploadNotificationEvent;
import uk.gov.hmcts.reform.civil.scheduler.common.ScheduledTask;

@Component
@AllArgsConstructor
@Slf4j
public class EvidenceUploadSchedulerTask implements ScheduledTask {

    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    public void accept(CaseDetails caseDetails) {
        Long caseId = caseDetails.getId();
        log.info("EvidenceUploadSchedulerTask::accept case {}", caseId);
        applicationEventPublisher.publishEvent(new EvidenceUploadNotificationEvent(caseId));
    }
}
