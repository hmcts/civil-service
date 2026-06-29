package uk.gov.hmcts.reform.civil.scheduler.automatedhearingnotice;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;
import uk.gov.hmcts.reform.civil.config.properties.EventProperties;
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
        EventProperties eventProperties = new EventProperties();
        eventProperties.setDispatchDelay(0);
        eventProperties.setLockDuration(1980000);
        task = new AutomatedHearingNoticeScheduledTask(applicationEventPublisher, eventProperties);
    }

    @Test
    void shouldPublishHearingNoticeSchedulerTaskEvent() {
        task.accept(HEARING_ID, 1);

        verify(applicationEventPublisher).publishEvent(new HearingNoticeSchedulerTaskEvent(HEARING_ID));
    }
}
