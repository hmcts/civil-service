package uk.gov.hmcts.reform.civil.scheduler.automatedhearingnotice;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;
import uk.gov.hmcts.reform.civil.event.HearingNoticeSchedulerTaskEvent;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class AutomatedHearingNoticeScheduledTaskTest {

    private static final String HEARING_ID = "hearing-id-1";

    private ApplicationEventPublisher applicationEventPublisher;
    private AutomatedHearingNoticeScheduledTask task;

    @BeforeEach
    void setUp() {
        applicationEventPublisher = mock(ApplicationEventPublisher.class);
        task = new AutomatedHearingNoticeScheduledTask(applicationEventPublisher);
    }

    @Test
    void shouldPublishHearingNoticeSchedulerTaskEvent() {
        task.accept(HEARING_ID);

        verify(applicationEventPublisher).publishEvent(new HearingNoticeSchedulerTaskEvent(HEARING_ID));
    }
}
