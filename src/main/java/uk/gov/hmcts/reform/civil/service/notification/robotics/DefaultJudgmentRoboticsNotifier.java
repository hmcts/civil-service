package uk.gov.hmcts.reform.civil.service.notification.robotics;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.robotics.JsonSchemaValidationService;
import uk.gov.hmcts.reform.civil.service.robotics.RoboticsNotificationService;
import uk.gov.hmcts.reform.civil.service.robotics.mapper.RoboticsDataMapperForUnspec;
import uk.gov.hmcts.reform.civil.service.robotics.mapper.RoboticsDataMapperForSpec;
import uk.gov.hmcts.reform.civil.service.robotics.utils.RoboticsDataUtil;

import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;

@Slf4j
@Component
public class DefaultJudgmentRoboticsNotifier extends RoboticsNotifier {

    public DefaultJudgmentRoboticsNotifier(RoboticsNotificationService roboticsNotificationService,
                                           JsonSchemaValidationService jsonSchemaValidationService,
                                           RoboticsDataMapperForUnspec roboticsDataMapper,
                                           RoboticsDataMapperForSpec roboticsDataMapperForSpec) {
        super(roboticsNotificationService, jsonSchemaValidationService, roboticsDataMapper, roboticsDataMapperForSpec);
    }

    @Override
    public void notifyRobotics(CaseData caseData, String authToken) {
        // DJ RPA is not handed-offline / adhoc BS transfer — omit Breathing Space from the payload.
        log.info(
            "Notifying RPA for default judgment for case: {} (Breathing Space omitted from payload)",
            caseData.getLegacyCaseReference()
        );
        super.notifyRobotics(RoboticsDataUtil.withoutBreathingSpace(caseData), authToken);
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
