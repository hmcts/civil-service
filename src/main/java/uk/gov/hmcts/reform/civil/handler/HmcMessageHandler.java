package uk.gov.hmcts.reform.civil.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.config.PaymentsConfiguration;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.hmc.model.messaging.HmcMessage;
import uk.gov.hmcts.reform.hmc.model.messaging.HmcStatus;

import java.util.List;

import static uk.gov.hmcts.reform.civil.callback.CaseEvent.REVIEW_HEARING_EXCEPTION;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.UPDATE_NEXT_HEARING_DETAILS;
import static uk.gov.hmcts.reform.hmc.model.messaging.HmcStatus.ADJOURNED;
import static uk.gov.hmcts.reform.hmc.model.messaging.HmcStatus.AWAITING_ACTUALS;
import static uk.gov.hmcts.reform.hmc.model.messaging.HmcStatus.CANCELLED;
import static uk.gov.hmcts.reform.hmc.model.messaging.HmcStatus.COMPLETED;
import static uk.gov.hmcts.reform.hmc.model.messaging.HmcStatus.EXCEPTION;
import static uk.gov.hmcts.reform.hmc.model.messaging.HmcStatus.LISTED;

@Service
@Slf4j
@RequiredArgsConstructor
public class HmcMessageHandler {

    private static final List<HmcStatus> NEXT_HEARING_DETAILS_UPDATE_STATUSES =
        List.of(LISTED, AWAITING_ACTUALS, COMPLETED, CANCELLED, ADJOURNED);

    private final CoreCaseDataService coreCaseDataService;
    private final PaymentsConfiguration paymentsConfiguration;

    public void handleMessage(HmcMessage hmcMessage) {
        if (!isMessageRelevantForService(hmcMessage)) {
            log.info("HMC message not relevant for service - Service code: {}", hmcMessage.getHmctsServiceCode());
            return;
        }

        if (NEXT_HEARING_DETAILS_UPDATE_STATUSES.contains(hmcMessage.getHearingUpdate().getHmcStatus())) {
            triggerCaseEvent(UPDATE_NEXT_HEARING_DETAILS, hmcMessage.getCaseId(), hmcMessage.getHearingId());
        } else if (EXCEPTION.equals(hmcMessage.getHearingUpdate().getHmcStatus())) {
            triggerCaseEvent(REVIEW_HEARING_EXCEPTION, hmcMessage.getCaseId(), hmcMessage.getHearingId());
        } else {
            log.info("HMC message status {} is not supported by handler.", hmcMessage.getHearingUpdate().getHmcStatus());
        }
    }

    private void triggerCaseEvent(CaseEvent event, Long caseId, String hearingId) {
        // trigger event for WA
        try {
            log.info("Triggering {} event for Case ID {} and Hearing ID {}.", event.name(), caseId, hearingId);
            coreCaseDataService.triggerEvent(caseId, event);
        } catch (Exception e) {
            log.error("Error triggering {} event: {}", event, e.getMessage());
            throw e;
        }
    }

    private boolean isMessageRelevantForService(HmcMessage hmcMessage) {
        return paymentsConfiguration.getSpecSiteId().equals(hmcMessage.getHmctsServiceCode())
            || paymentsConfiguration.getSiteId().equals(hmcMessage.getHmctsServiceCode());
    }
}
