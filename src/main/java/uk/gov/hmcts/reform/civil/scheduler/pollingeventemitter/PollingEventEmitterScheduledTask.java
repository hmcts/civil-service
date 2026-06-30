package uk.gov.hmcts.reform.civil.scheduler.pollingeventemitter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.scheduler.common.DefaultBackPressureConfiguration;
import uk.gov.hmcts.reform.civil.scheduler.common.ScheduledTask;
import uk.gov.hmcts.reform.civil.scheduler.common.ScheduledTaskBackPressureConfiguration;
import uk.gov.hmcts.reform.civil.service.EventEmitterService;

@Component
@RequiredArgsConstructor
@Slf4j
public class PollingEventEmitterScheduledTask implements ScheduledTask {

    private final CaseDetailsConverter caseDetailsConverter;
    private final EventEmitterService eventEmitterService;
    private final DefaultBackPressureConfiguration defaultBackPressureConfiguration;

    @Value("${polling.emitter.multiple.cases.delay.seconds:30}")
    private long multiCasesExecutionDelayInSeconds;

    @Override
    public void accept(CaseDetails caseDetails) {
        CaseData caseData = caseDetailsConverter.toCaseData(caseDetails);
        log.info(
            "Emitting {} camunda event for case through poller: {}",
            caseData.getBusinessProcess().getCamundaEvent(),
            caseData.getCcdCaseReference()
        );

        eventEmitterService.emitBusinessProcessCamundaEvent(caseData, true);
    }

    @Override
    public long maxCasesPerRun() {
        return Math.max(1L, PollingEventEmitterScheduler.FIFTY_MINUTES_IN_SECONDS / getDelaySeconds());
    }

    @Override
    public ScheduledTaskBackPressureConfiguration backPressureConfiguration() {
        return defaultBackPressureConfiguration.getDefaultBackPressure();
    }

    private long getDelaySeconds() {
        return Math.max(1L, multiCasesExecutionDelayInSeconds);
    }
}
