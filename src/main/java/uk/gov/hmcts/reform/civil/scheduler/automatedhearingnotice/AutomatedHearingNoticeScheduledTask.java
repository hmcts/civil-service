package uk.gov.hmcts.reform.civil.scheduler.automatedhearingnotice;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.config.properties.EventProperties;
import uk.gov.hmcts.reform.civil.event.HearingNoticeSchedulerTaskEvent;
import uk.gov.hmcts.reform.civil.scheduler.common.SchedulerThrottleUtils;

@Component
@RequiredArgsConstructor
@Slf4j
public class AutomatedHearingNoticeScheduledTask {

    private final ApplicationEventPublisher applicationEventPublisher;
    private final EventProperties eventProperties;

    public void accept(String hearingId, long totalFound) {
        log.info("AutomatedHearingNoticeScheduledTask::accept hearing {}", hearingId);
        applicationEventPublisher.publishEvent(new HearingNoticeSchedulerTaskEvent(hearingId));
        SchedulerThrottleUtils.throttle(
            totalFound,
            eventProperties.getDispatchDelay(),
            eventProperties.getLockDuration()
        );
    }
}
