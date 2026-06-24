package uk.gov.hmcts.reform.civil.scheduler.hearingcvplink;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.MockedStatic;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.config.properties.EventProperties;
import uk.gov.hmcts.reform.civil.event.CvpJoinLinkEvent;
import uk.gov.hmcts.reform.civil.scheduler.common.SchedulerThrottleUtils;

import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HearingCvpLinkScheduledTaskTest {

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @Mock
    private EventProperties eventProperties;

    @InjectMocks
    private HearingCvpLinkScheduledTask task;

    @Test
    void shouldPublishCvpJoinLinkEvent() {
        Long caseId = 123L;
        CaseDetails caseDetails = CaseDetails.builder().id(caseId).build();

        task.accept(caseDetails);

        verify(applicationEventPublisher).publishEvent(new CvpJoinLinkEvent(caseId));
    }

    @Test
    void shouldThrottleUsingTotalCases() {
        Long caseId = 123L;
        CaseDetails caseDetails = CaseDetails.builder().id(caseId).build();
        when(eventProperties.getDispatchDelay()).thenReturn(2000);
        when(eventProperties.getLockDuration()).thenReturn(600000L);

        try (MockedStatic<SchedulerThrottleUtils> schedulerThrottleUtils = mockStatic(
            SchedulerThrottleUtils.class
        )) {
            task.accept(caseDetails, 26);

            verify(applicationEventPublisher).publishEvent(new CvpJoinLinkEvent(caseId));
            schedulerThrottleUtils.verify(() -> SchedulerThrottleUtils.throttle(26, 2000, 600000L));
        }
    }
}
