package uk.gov.hmcts.reform.unspec.handler.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.unspec.event.RoboticsEvent;
import uk.gov.hmcts.reform.unspec.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.unspec.service.robotics.RoboticsNotificationService;

import static org.mockito.Mockito.verify;

@ExtendWith(SpringExtension.class)
class RoboticsEventHandlerTest {

    @Mock
    private RoboticsNotificationService roboticsNotificationService;

    @InjectMocks
    private RoboticsEventHandler handler;

    @Test
    void shouldCallTriggerEventWithExpectedParams_WhenRoboticsEvent() {
        RoboticsEvent event = new RoboticsEvent(CaseDataBuilder.builder().atStateRespondedToClaim().build());

        handler.notifyRobotics(event);

        verify(roboticsNotificationService).notifyRobotics(event.getCaseData());
    }
}
