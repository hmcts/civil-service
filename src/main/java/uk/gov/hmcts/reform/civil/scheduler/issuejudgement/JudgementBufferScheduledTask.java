package uk.gov.hmcts.reform.civil.scheduler.issuejudgement;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.scheduler.common.ScheduledTask;

@Slf4j
@Component
public class JudgementBufferScheduledTask implements ScheduledTask {

    @Override
    public void accept(CaseDetails caseDetails) {
        log.info("Processing case {}", caseDetails.getId());
    }
}
