package uk.gov.hmcts.reform.civil.scheduler.hearingcvplink;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.event.CvpJoinLinkEvent;
import uk.gov.hmcts.reform.civil.scheduler.common.DefaultBackPressureConfiguration;
import uk.gov.hmcts.reform.civil.scheduler.common.ScheduledTask;
import uk.gov.hmcts.reform.civil.scheduler.common.ScheduledTaskBackPressureConfiguration;

@Component
@RequiredArgsConstructor
@Slf4j
public class HearingCvpLinkScheduledTask implements ScheduledTask {

    private final ApplicationEventPublisher applicationEventPublisher;
    private final DefaultBackPressureConfiguration defaultBackPressureConfiguration;

    @Override
    public void accept(CaseDetails caseDetails) {
        Long caseId = caseDetails.getId();
        log.info("HearingCvpLinkScheduledTask::accept case {}", caseId);
        applicationEventPublisher.publishEvent(new CvpJoinLinkEvent(caseId));
    }

    @Override
    public ScheduledTaskBackPressureConfiguration backPressureConfiguration() {
        return defaultBackPressureConfiguration.getDefaultBackPressure();
    }
}
