package uk.gov.hmcts.reform.civil.service.notification.robotics;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.robotics.RoboticsNotificationService;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RoboticsNotifierTest {

    private static final String TOKEN = "auth";

    @Mock
    private RoboticsNotificationService roboticsNotificationService;
    @InjectMocks
    private RoboticsNotifier roboticsNotifier;

    @Test
    void routesSinglePartyCaseToNotificationService() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build();
        roboticsNotifier.notifyRobotics(caseData, TOKEN);
        verify(roboticsNotificationService).notifyRobotics(caseData, false, TOKEN);
    }
}
