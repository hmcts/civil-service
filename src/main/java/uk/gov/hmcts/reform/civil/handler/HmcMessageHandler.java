package uk.gov.hmcts.reform.civil.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.camunda.bpm.engine.RuntimeService;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.config.PaymentsConfiguration;
import uk.gov.hmcts.reform.civil.exceptions.CompleteTaskException;
import uk.gov.hmcts.reform.civil.handler.tasks.BaseExternalTaskHandler;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.service.nexthearingdate.NextHearingDateVariables;
import uk.gov.hmcts.reform.hmc.model.messaging.HmcMessage;
import uk.gov.hmcts.reform.hmc.model.messaging.HmcStatus;

import java.util.List;

import static uk.gov.hmcts.reform.civil.callback.CaseEvent.REVIEW_HEARING_EXCEPTION;
import static uk.gov.hmcts.reform.civil.enums.nexthearingdate.UpdateType.DELETE;
import static uk.gov.hmcts.reform.civil.enums.nexthearingdate.UpdateType.UPDATE;
import static uk.gov.hmcts.reform.hmc.model.messaging.HmcStatus.ADJOURNED;
import static uk.gov.hmcts.reform.hmc.model.messaging.HmcStatus.CANCELLED;
import static uk.gov.hmcts.reform.hmc.model.messaging.HmcStatus.COMPLETED;
import static uk.gov.hmcts.reform.hmc.model.messaging.HmcStatus.EXCEPTION;
import static uk.gov.hmcts.reform.hmc.model.messaging.HmcStatus.LISTED;

@Service
@Slf4j
@RequiredArgsConstructor
public class HmcMessageHandler implements BaseExternalTaskHandler {

    private final CoreCaseDataService coreCaseDataService;
    private final PaymentsConfiguration paymentsConfiguration;
    private final RuntimeService runtimeService;
    private final ObjectMapper objectMapper;

    private static final List<HmcStatus> UPDATE_STATUSES = List.of(LISTED);
    private static final List<HmcStatus> DELETE_STATUSES = List.of(COMPLETED, CANCELLED, ADJOURNED);

    private static final String CAMUNDA_MESAGE = "NEXT_HEARING_DATE_UPDATE";

    public void handleExceptionEvent(HmcMessage hmcMessage) {
        if (isMessageRelevantForService(hmcMessage)) {
            if (EXCEPTION.equals(hmcMessage.getHearingUpdate().getHmcStatus())) {
                log.info("Hearing ID: {} for case {} in EXCEPTION status, triggering REVIEW_HEARING_EXCEPTION event",
                         hmcMessage.getHearingId(),
                         hmcMessage.getCaseId()
                );
                triggerReviewHearingExceptionEvent(hmcMessage.getCaseId(), hmcMessage.getHearingId());
            }
        }
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

    @Override
    public void completeTask(ExternalTask externalTask, ExternalTaskService externalTaskService) throws CompleteTaskException {
        BaseExternalTaskHandler.super.completeTask(externalTask, externalTaskService);
    }

    @Async("asyncHandlerExecutor")
    @EventListener
    public void handleTask(ExternalTask externalTask) {
        NextHearingDateVariables nextHearingDateVariables = objectMapper.convertValue(externalTask.getAllVariables(), NextHearingDateVariables.class);
        HmcStatus hmcStatus = nextHearingDateVariables.getHmcStatus();
        if (UPDATE_STATUSES.contains(hmcStatus)) {
            triggerUpdateNextHearingDateMessage(nextHearingDateVariables);
        } else if (DELETE_STATUSES.contains(hmcStatus)) {
            triggerDeleteMessage(nextHearingDateVariables);
        }
    }

    private void triggerDeleteMessage(NextHearingDateVariables nextHearingDateVariables) {
        NextHearingDateVariables messageVars = NextHearingDateVariables.builder()
            .caseId(nextHearingDateVariables.getCaseId())
            .updateType(DELETE)
            .build();
        triggerMessage(messageVars);
    }

    private void triggerUpdateNextHearingDateMessage(NextHearingDateVariables nextHearingDateVariables) {
        NextHearingDateVariables messageVars = NextHearingDateVariables.builder()
            .hearingId(nextHearingDateVariables.getHearingId())
            .caseId(nextHearingDateVariables.getCaseId())
            .nextHearingDate(nextHearingDateVariables.getNextHearingDate())
            .updateType(UPDATE)
            .build();
        triggerMessage(messageVars);
    }

    private void triggerMessage(NextHearingDateVariables messageVars) {
        runtimeService
            .createMessageCorrelation(CAMUNDA_MESAGE)
            .setVariables(messageVars.toMap(objectMapper))
            .correlateStartMessage();
    }

    @Override
    public int getMaxAttempts() {
        return 1;
    }
}
