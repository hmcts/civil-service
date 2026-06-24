package uk.gov.hmcts.reform.civil.scheduler.hearingcvplink;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.config.properties.EventProperties;
import uk.gov.hmcts.reform.civil.event.CvpJoinLinkEvent;
import uk.gov.hmcts.reform.civil.scheduler.common.ScheduledTask;

@Component
@RequiredArgsConstructor
@Slf4j
public class HearingCvpLinkScheduledTask implements ScheduledTask {

    private static final int SMALL_BATCH = 25;

    private final ApplicationEventPublisher applicationEventPublisher;
    private final EventProperties eventProperties;

    @Override
    public void accept(CaseDetails caseDetails) {
        accept(caseDetails, 0);
    }

    void accept(CaseDetails caseDetails, int totalCases) {
        Long caseId = caseDetails.getId();
        log.info("HearingCvpLinkScheduledTask::accept case {}", caseId);
        applicationEventPublisher.publishEvent(new CvpJoinLinkEvent(caseId));
        throttle(totalCases);
    }

    @SuppressWarnings("java:S2142")
    private void throttle(int totalCases) {
        long effectiveDelay = calculateEffectiveDelay(
            totalCases,
            eventProperties.getLockDuration(),
            eventProperties.getDispatchDelay()
        );
        if (effectiveDelay == 0) {
            return;
        }
        try {
            Thread.sleep(effectiveDelay);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private long calculateEffectiveDelay(long count, long lock, long delay) {
        if (count <= 1 || delay <= 0 || lock <= 0) {
            return 0;
        }

        if (count <= SMALL_BATCH && delay < 2000L) {
            return 0;
        }

        long maxExecutionTimeMs = (long) (lock * 0.8);
        long maxDelay = maxExecutionTimeMs / count;
        return Math.min(maxDelay, delay);
    }
}
