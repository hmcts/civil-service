package uk.gov.hmcts.reform.civil.scheduler.defendantresponse;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.scheduler.common.DefaultBackPressureConfiguration;
import uk.gov.hmcts.reform.civil.scheduler.common.ScheduledTask;
import uk.gov.hmcts.reform.civil.scheduler.common.ScheduledTaskBackPressureConfiguration;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;

import static uk.gov.hmcts.reform.civil.callback.CaseEvent.DEFENDANT_RESPONSE_DEADLINE_CHECK;

@Component
@AllArgsConstructor
@Slf4j
public class DefendantResponseDeadlineTask implements ScheduledTask<CaseDetails, Long> {

    private final CoreCaseDataService coreCaseDataService;
    private final DefaultBackPressureConfiguration defaultBackPressureConfiguration;

    @Override
    public Long getItemId(CaseDetails caseDetails) {
        return caseDetails.getId();
    }

    @Override
    public void accept(CaseDetails caseDetails) {
        Long caseId = caseDetails.getId();
        log.info("DefendantResponseDeadlineTask::accept case {}", caseId);
        coreCaseDataService.triggerEvent(caseId, DEFENDANT_RESPONSE_DEADLINE_CHECK);
    }

    @Override
    public ScheduledTaskBackPressureConfiguration backPressureConfiguration() {
        return defaultBackPressureConfiguration.getDefaultBackPressure();
    }
}
