package uk.gov.hmcts.reform.unspec.handler.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.unspec.event.RoboticsEvent;
import uk.gov.hmcts.reform.unspec.service.robotics.RoboticsNotificationService;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoboticsEventHandler {

    private final RoboticsNotificationService roboticsNotificationService;

    @EventListener
    public void notifyRobotics(RoboticsEvent event) {
        roboticsNotificationService.notifyRobotics(event.getCaseData());
    }
}
