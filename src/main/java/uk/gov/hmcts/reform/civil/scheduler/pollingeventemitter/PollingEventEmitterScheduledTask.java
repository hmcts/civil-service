package uk.gov.hmcts.reform.civil.scheduler.pollingeventemitter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.config.properties.EventProperties;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.scheduler.common.ScheduledTask;
import uk.gov.hmcts.reform.civil.scheduler.common.SchedulerThrottleUtils;
import uk.gov.hmcts.reform.civil.service.EventEmitterService;

import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
@Slf4j
public class PollingEventEmitterScheduledTask implements ScheduledTask {

    static final long POLLING_WINDOW_MS = TimeUnit.SECONDS.toMillis(
        PollingEventEmitterScheduler.FIFTY_MINUTES_IN_SECONDS
    );

    private final CaseDetailsConverter caseDetailsConverter;
    private final EventEmitterService eventEmitterService;
    private final EventProperties eventProperties;

    @Override
    public void accept(CaseDetails caseDetails) {
        accept(caseDetails, 1, eventProperties.getDispatchDelay());
    }

    public void accept(CaseDetails caseDetails, long totalCases, long delayMs) {
        CaseData caseData = caseDetailsConverter.toCaseData(caseDetails);
        log.info(
            "Emitting {} camunda event for case through poller: {}",
            caseData.getBusinessProcess().getCamundaEvent(),
            caseData.getCcdCaseReference()
        );

        eventEmitterService.emitBusinessProcessCamundaEvent(caseData, true);
        SchedulerThrottleUtils.throttle(totalCases, delayMs, POLLING_WINDOW_MS);
    }
}
