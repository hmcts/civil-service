package uk.gov.hmcts.reform.civil.service.notification.robotics;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.robotics.RoboticsNotificationService;

import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;

@Component
public class DefaultJudgmentRoboticsNotifier extends RoboticsNotifier {

    public DefaultJudgmentRoboticsNotifier(RoboticsNotificationService roboticsNotificationService) {
        super(roboticsNotificationService);
    }

    @Override
    public void sendNotifications(CaseData caseData, boolean multiPartyScenario, String authToken) {
        if (SPEC_CLAIM.equals(caseData.getCaseAccessCategory()) && caseData.isRespondent1NotRepresented()) {
            roboticsNotificationService.notifyJudgementLip(caseData, authToken);
        } else {
            super.sendNotifications(caseData, multiPartyScenario,
                authToken);
        }
    }
}
