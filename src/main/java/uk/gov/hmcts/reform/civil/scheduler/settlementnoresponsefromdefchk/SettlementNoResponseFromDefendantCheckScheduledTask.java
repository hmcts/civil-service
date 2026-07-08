package uk.gov.hmcts.reform.civil.scheduler.settlementnoresponsefromdefchk;

import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.event.SettlementNoResponseFromDefendantEvent;
import uk.gov.hmcts.reform.civil.scheduler.common.DefaultBackPressureConfiguration;
import uk.gov.hmcts.reform.civil.scheduler.common.ScheduledTask;
import uk.gov.hmcts.reform.civil.scheduler.common.ScheduledTaskBackPressureConfiguration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SettlementNoResponseFromDefendantCheckScheduledTask implements ScheduledTask<CaseDetails, Long> {

    private final ApplicationEventPublisher applicationEventPublisher;
    private final DefaultBackPressureConfiguration defaultBackPressureConfiguration;

    @Override
    public Long getItemId(CaseDetails caseDetails) {
        return caseDetails.getId();
    }

    @Override
    public void accept(CaseDetails caseDetails) {
        Long caseId = caseDetails.getId();
        log.info("SettlementNoResponseFromDefendantCheckScheduledTask::accept case {}", caseId);

        try {
            applicationEventPublisher.publishEvent(new SettlementNoResponseFromDefendantEvent(caseId));
        } catch (Exception e) {
            log.error("Updating case with id: '{}' failed", caseId, e);
        }

    }

    @Override
    public ScheduledTaskBackPressureConfiguration backPressureConfiguration() {
        return defaultBackPressureConfiguration.getDefaultBackPressure();
    }
}
