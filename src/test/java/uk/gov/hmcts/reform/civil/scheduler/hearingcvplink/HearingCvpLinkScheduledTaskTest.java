package uk.gov.hmcts.reform.civil.scheduler.hearingcvplink;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.event.CvpJoinLinkEvent;
import uk.gov.hmcts.reform.civil.scheduler.common.SchedulerThrottleService;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class HearingCvpLinkScheduledTaskTest {

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @Mock
    private SchedulerThrottleService schedulerThrottleService;

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

        task.accept(caseDetails, 26);

        verify(applicationEventPublisher).publishEvent(new CvpJoinLinkEvent(caseId));
        verify(schedulerThrottleService).throttle(26);
    }
}
