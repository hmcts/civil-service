package uk.gov.hmcts.reform.civil.scheduler.automatedhearingnotice;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;
import uk.gov.hmcts.reform.civil.event.HearingNoticeSchedulerTaskEvent;
import uk.gov.hmcts.reform.civil.scheduler.common.DefaultBackPressureConfiguration;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class AutomatedHearingNoticeScheduledTaskTest {

    private static final String HEARING_ID = "hearing-id-1";

    private ApplicationEventPublisher applicationEventPublisher;
    private DefaultBackPressureConfiguration defaultBackPressureConfiguration;
    private AutomatedHearingNoticeScheduledTask task;

    @BeforeEach
    void setUp() {
        applicationEventPublisher = mock(ApplicationEventPublisher.class);
        defaultBackPressureConfiguration = mock(DefaultBackPressureConfiguration.class);
        task = new AutomatedHearingNoticeScheduledTask(applicationEventPublisher, defaultBackPressureConfiguration);
    }

    @Test
    void shouldPublishHearingNoticeSchedulerTaskEvent() {
        task.accept(HEARING_ID);

        verify(applicationEventPublisher).publishEvent(new HearingNoticeSchedulerTaskEvent(HEARING_ID));
    }
}
