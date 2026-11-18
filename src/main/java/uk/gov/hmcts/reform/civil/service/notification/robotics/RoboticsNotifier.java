package uk.gov.hmcts.reform.civil.service.notification.robotics;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.robotics.RoboticsNotificationService;

import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.isMultiPartyScenario;

@Component
@RequiredArgsConstructor
public class RoboticsNotifier {

    protected final RoboticsNotificationService roboticsNotificationService;

    public void notifyRobotics(CaseData caseData, String authToken) {
        sendNotifications(caseData, isMultiPartyScenario(caseData), authToken);
    }

    protected void sendNotifications(CaseData caseData, boolean multiPartyScenario, String authToken) {
        roboticsNotificationService.notifyRobotics(caseData, multiPartyScenario,
            authToken
        );
    }
}
