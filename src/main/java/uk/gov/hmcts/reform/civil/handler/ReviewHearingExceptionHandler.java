package uk.gov.hmcts.reform.civil.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.config.PaymentsConfiguration;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.hmc.model.messaging.HmcMessage;

import static uk.gov.hmcts.reform.civil.callback.CaseEvent.REVIEW_HEARING_EXCEPTION;
import static uk.gov.hmcts.reform.hmc.model.messaging.HmcStatus.EXCEPTION;

@Service
@Slf4j
@RequiredArgsConstructor
public class ReviewHearingExceptionHandler {

    private final CoreCaseDataService coreCaseDataService;
    private final PaymentsConfiguration paymentsConfiguration;

    public boolean handleExceptionEvent(HmcMessage hmcMessage) {
        if (isMessageRelevantForService(hmcMessage)) {
            if (EXCEPTION.equals(hmcMessage.getHearingUpdate().getHmcStatus())) {
                log.info("Hearing ID: {} for case {} in EXCEPTION status, triggering REVIEW_HEARING_EXCEPTION event",
                         hmcMessage.getHearingId(),
                         hmcMessage.getCaseId()
                );
                return triggerReviewHearingExceptionEvent(hmcMessage.getCaseId(), hmcMessage.getHearingId());
            }
        }
        return true;
    }

    private boolean triggerReviewHearingExceptionEvent(Long caseId, String hearingId) {
        // trigger event for WA
        try {
            coreCaseDataService.triggerEvent(caseId, REVIEW_HEARING_EXCEPTION);
            log.info(
                "Triggered REVIEW_HEARING_EXCEPTION event for Case ID {}, and Hearing ID {}.",
                caseId, hearingId);
            return true;
        } catch (Exception e) {
            log.info("Error triggering CCD event {}", e.getMessage());
        }
        return false;
    }

    private boolean isMessageRelevantForService(HmcMessage hmcMessage) {
        return paymentsConfiguration.getSpecSiteId().equals(hmcMessage.getHmctsServiceCode())
            || paymentsConfiguration.getSiteId().equals(hmcMessage.getHmctsServiceCode());
    }
}
