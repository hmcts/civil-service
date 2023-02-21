package uk.gov.hmcts.reform.civil.handler.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.event.BundleCreationTriggerEvent;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;

import static uk.gov.hmcts.reform.civil.callback.CaseEvent.BUNDLE_CREATION_NOTIFICATION;

@Slf4j
@Service
@RequiredArgsConstructor
public class BundleCreationTriggerEventHandler {

    private final CoreCaseDataService coreCaseDataService;

    /**
     * This method will send notification to applicant and respondent solicitors.
     * This method will not throw any exception but log warning if there is any error while
     @@ -20,7 +32,25 @@ public class BundleCreationTriggerEventHandler {
      * @param event EvidenceUploadNotificationEvent
     */
    @EventListener
    public void sendBundleCreationTriggerNotification(BundleCreationTriggerEvent event) {
        coreCaseDataService.triggerEvent(event.getCaseId(), BUNDLE_CREATION_NOTIFICATION);
    }
}
