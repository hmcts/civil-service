package uk.gov.hmcts.reform.civil.scheduler.defendantresponse;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.scheduler.common.DefaultBackPressureConfiguration;
import uk.gov.hmcts.reform.civil.scheduler.common.ScheduledTask;
import uk.gov.hmcts.reform.civil.scheduler.common.ScheduledTaskBackPressureConfiguration;

@Component
@AllArgsConstructor
@Slf4j
public class DefendantResponseDeadlineTask implements ScheduledTask {

    private final DefaultBackPressureConfiguration defaultBackPressureConfiguration;

    @Override
    public void accept(CaseDetails caseDetails) {
        log.info("DefendantResponseDeadlineTask::accept case {}", caseDetails.getId());
        //Add logic to publish event for defendant response deadline
    }

    @Override
    public ScheduledTaskBackPressureConfiguration backPressureConfiguration() {
        return defaultBackPressureConfiguration.getDefaultBackPressure();
    }
}
