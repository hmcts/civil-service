package uk.gov.hmcts.reform.civil.handler.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.event.BundleCreationTriggerEvent;
import uk.gov.hmcts.reform.civil.service.stitching.BundleCreationService;

@Slf4j
@Service
@RequiredArgsConstructor
public class BundleCreationTriggerEventHandler {

    private final BundleCreationService bundleCreationService;
    /**
     * This method will send notification to applicant and respondent solicitors.
     * This method will not throw any exception but log warning if there is any error while
     * sending notification because these email notifications are not business critical
     * and provided as a courtesy, as the user can log in and see the new uploads.
     *
     * @param event EvidenceUploadNotificationEvent
     */
    @EventListener
    public void sendBundleCreationTrigger(BundleCreationTriggerEvent event) {
        bundleCreationService.CreateBundleDoucment(event);
    }
}
