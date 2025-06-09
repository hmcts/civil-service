package uk.gov.hmcts.reform.civil.service.notification.robotics;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.CaseCategory;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.robotics.JsonSchemaValidationService;
import uk.gov.hmcts.reform.civil.service.robotics.RoboticsNotificationService;
import uk.gov.hmcts.reform.civil.service.robotics.mapper.RoboticsDataMapper;
import uk.gov.hmcts.reform.civil.service.robotics.mapper.RoboticsDataMapperForSpec;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DefaultJudgmentRoboticsNotifierTest {

    @Mock
    private RoboticsNotificationService roboticsNotificationService;
    @Mock
    private JsonSchemaValidationService jsonSchemaValidationService;
    @Mock
    private RoboticsDataMapper roboticsDataMapper;
    @Mock
    private RoboticsDataMapperForSpec roboticsDataMapperForSpec;
    @Mock
    private FeatureToggleService featureToggleService;

    @InjectMocks
    DefaultJudgmentRoboticsNotifier defaultJudgmentRoboticsNotifier;

    @Test
    void shouldNotifyDefaultJudgementLip_whenFeatureToggleOn() {
        CaseData data = CaseData.builder()
            .caseAccessCategory(CaseCategory.SPEC_CLAIM)
            .respondent1Represented(YesOrNo.NO)
            .build();

        when(featureToggleService.isPinInPostEnabled()).thenReturn(true);


        defaultJudgmentRoboticsNotifier.sendNotifications(data, false, "auth");

        verify(roboticsNotificationService).notifyJudgementLip(any(), any());
    }

    @Test
    void shouldNotNotifyDefaultJudgementLip_whenFeatureToggleOff() {
        CaseData data = CaseData.builder()
            .caseAccessCategory(CaseCategory.SPEC_CLAIM)
            .respondent1Represented(YesOrNo.NO)
            .build();

        when(featureToggleService.isPinInPostEnabled()).thenReturn(false);

        defaultJudgmentRoboticsNotifier.sendNotifications(data, false, "auth");

        verify(roboticsNotificationService).notifyRobotics(any(), anyBoolean(), any());
    }

    @Test
    void shouldNotNotifyDefaultJudgementLip_whenCaseNotSpec() {
        CaseData data = CaseData.builder()
            .caseAccessCategory(CaseCategory.UNSPEC_CLAIM)
            .respondent1Represented(YesOrNo.YES)
            .build();

        defaultJudgmentRoboticsNotifier.sendNotifications(data, false, "auth");

        verify(roboticsNotificationService).notifyRobotics(any(), anyBoolean(), any());
    }

}
