package uk.gov.hmcts.reform.civil.scheduler.automatedhearingnotice;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.event.HearingNoticeSchedulerTaskEvent;
import uk.gov.hmcts.reform.civil.scheduler.common.DefaultBackPressureConfiguration;
import uk.gov.hmcts.reform.civil.scheduler.common.ScheduledTask;
import uk.gov.hmcts.reform.civil.scheduler.common.ScheduledTaskBackPressureConfiguration;

@Component
@RequiredArgsConstructor
@Slf4j
public class AutomatedHearingNoticeScheduledTask implements ScheduledTask<String, String> {

    private final ApplicationEventPublisher applicationEventPublisher;
    private final DefaultBackPressureConfiguration defaultBackPressureConfiguration;

    @Override
    public String getItemId(String hearingId) {
        return hearingId;
    }

    @Override
    public void accept(String hearingId) {
        log.info("AutomatedHearingNoticeScheduledTask::accept hearing {}", hearingId);
        applicationEventPublisher.publishEvent(new HearingNoticeSchedulerTaskEvent(hearingId));
    }

    @Override
    public ScheduledTaskBackPressureConfiguration backPressureConfiguration() {
        return defaultBackPressureConfiguration.getDefaultBackPressure();
    }
}
