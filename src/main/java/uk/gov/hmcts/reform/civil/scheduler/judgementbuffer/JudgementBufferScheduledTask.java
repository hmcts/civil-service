package uk.gov.hmcts.reform.civil.scheduler.judgementbuffer;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.scheduler.common.DefaultBackPressureConfiguration;
import uk.gov.hmcts.reform.civil.scheduler.common.ScheduledTask;
import uk.gov.hmcts.reform.civil.scheduler.common.ScheduledTaskBackPressureConfiguration;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;

@Component
@Slf4j
@AllArgsConstructor
public class JudgementBufferScheduledTask implements ScheduledTask<CaseDetails, Long> {

    private final CoreCaseDataService coreCaseDataService;
    private final DefaultBackPressureConfiguration defaultBackPressureConfiguration;

    @Override
    public Long getItemId(CaseDetails caseDetails) {
        return caseDetails.getId();
    }

    @Override
    public void accept(CaseDetails caseDetails) {
        Long caseId = caseDetails.getId();
        log.info("JudgementBufferScheduledTask::accept case {}", caseId);
        coreCaseDataService.triggerEvent(caseId, CaseEvent.DEFAULT_JUDGEMENT_GRANTED_SPEC);
    }

    @Override
    public ScheduledTaskBackPressureConfiguration backPressureConfiguration() {
        return defaultBackPressureConfiguration.getDefaultBackPressure();
    }
}
