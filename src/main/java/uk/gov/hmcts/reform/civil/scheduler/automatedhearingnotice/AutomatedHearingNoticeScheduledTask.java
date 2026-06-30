package uk.gov.hmcts.reform.civil.scheduler.automatedhearingnotice;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.event.HearingNoticeSchedulerTaskEvent;

@Component
@RequiredArgsConstructor
@Slf4j
public class AutomatedHearingNoticeScheduledTask {

    private final ApplicationEventPublisher applicationEventPublisher;

    public void accept(String hearingId) {
        log.info("AutomatedHearingNoticeScheduledTask::accept hearing {}", hearingId);
        applicationEventPublisher.publishEvent(new HearingNoticeSchedulerTaskEvent(hearingId));
    }
}
