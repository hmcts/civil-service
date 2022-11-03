package uk.gov.hmcts.reform.civil.handler.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.event.EvidenceUploadNotificationEvent;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;

import static uk.gov.hmcts.reform.civil.callback.CaseEvent.EVIDENCE_UPLOAD_NOTIFICATION;

@Slf4j
@Service
@RequiredArgsConstructor
public class EvidenceUploadNotificationEventHandler {

    private final CoreCaseDataService coreCaseDataService;

    @EventListener
    public void sendEvidenceUploadNotification(EvidenceUploadNotificationEvent event) {
        coreCaseDataService.triggerEvent(event.getCaseId(), EVIDENCE_UPLOAD_NOTIFICATION);
    }
}
