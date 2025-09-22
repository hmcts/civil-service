package uk.gov.hmcts.reform.civil.service.notification.robotics;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.robotics.JsonSchemaValidationService;
import uk.gov.hmcts.reform.civil.service.robotics.RoboticsNotificationService;
import uk.gov.hmcts.reform.civil.service.robotics.mapper.RoboticsDataMapper;
import uk.gov.hmcts.reform.civil.service.robotics.mapper.RoboticsDataMapperForSpec;

import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;

@Slf4j
@Component
public class DefaultJudgmentRoboticsNotifier extends RoboticsNotifier {

    protected final FeatureToggleService toggleService;

    public DefaultJudgmentRoboticsNotifier(RoboticsNotificationService roboticsNotificationService,
                                           JsonSchemaValidationService jsonSchemaValidationService, FeatureToggleService toggleService,
                                           RoboticsDataMapper roboticsDataMapper,
                                           RoboticsDataMapperForSpec roboticsDataMapperForSpec) {
        super(roboticsNotificationService, jsonSchemaValidationService, roboticsDataMapper, roboticsDataMapperForSpec, toggleService);
        this.toggleService = toggleService;
    }

    @Override
    public void sendNotifications(CaseData caseData, boolean multiPartyScenario, String authToken) {
        if (SPEC_CLAIM.equals(caseData.getCaseAccessCategory()) && caseData.isRespondent1NotRepresented()
            && toggleService.isPinInPostEnabled()) {
            roboticsNotificationService.notifyJudgementLip(caseData, authToken);
        } else {
            super.sendNotifications(caseData, multiPartyScenario,
                authToken);
        }
    }
}
