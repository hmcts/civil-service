package uk.gov.hmcts.reform.civil.service.notification.robotics;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.networknt.schema.ValidationMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.CaseCategory;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.robotics.RoboticsCaseData;
import uk.gov.hmcts.reform.civil.model.robotics.RoboticsCaseDataSpec;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.robotics.JsonSchemaValidationService;
import uk.gov.hmcts.reform.civil.service.robotics.RoboticsNotificationService;
import uk.gov.hmcts.reform.civil.service.robotics.exception.JsonSchemaValidationException;
import uk.gov.hmcts.reform.civil.service.robotics.mapper.RoboticsDataMapper;
import uk.gov.hmcts.reform.civil.service.robotics.mapper.RoboticsDataMapperForSpec;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RoboticsNotifierTest {

    private static final String TOKEN = "auth";
    private static final String LEGACY_REFERENCE = "AC13029";

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
    private RoboticsNotifier roboticsNotifier;

    @Test
    void shouldNotExecute_ifRpaDisabled() {
        when(featureToggleService.isRPAEmailEnabled()).thenReturn(false);

        roboticsNotifier.notifyRobotics(null, null);

        verifyNoInteractions(jsonSchemaValidationService);
        verifyNoInteractions(roboticsDataMapper);
        verifyNoInteractions(roboticsNotificationService);
    }

    @Test
    void shouldSendNotifications_whenValidSpecClaim() throws JsonProcessingException {
        RoboticsCaseDataSpec roboticsCaseDataSpec = RoboticsCaseDataSpec.builder().build();
        CaseData caseData = CaseData.builder().legacyCaseReference(LEGACY_REFERENCE)
            .caseAccessCategory(CaseCategory.SPEC_CLAIM).build();

        when(featureToggleService.isRPAEmailEnabled()).thenReturn(true);
        when(roboticsDataMapperForSpec.toRoboticsCaseData(eq(caseData), eq(TOKEN))).thenReturn(roboticsCaseDataSpec);
        when(jsonSchemaValidationService.validate(roboticsCaseDataSpec.toJsonString())).thenReturn(Set.of());

        roboticsNotifier.notifyRobotics(caseData, TOKEN);

        verify(roboticsNotificationService).notifyRobotics(any(), anyBoolean(), any());
    }

    @Test
    void shouldThrowJsonSchemaValidationException_whenInvalidSpecClaim() throws JsonProcessingException {
        RoboticsCaseDataSpec roboticsCaseDataSpec = RoboticsCaseDataSpec.builder().build();
        CaseData caseData = CaseData.builder().legacyCaseReference(LEGACY_REFERENCE)
            .caseAccessCategory(CaseCategory.SPEC_CLAIM).build();

        when(featureToggleService.isRPAEmailEnabled()).thenReturn(true);
        when(roboticsDataMapperForSpec.toRoboticsCaseData(eq(caseData), eq(TOKEN))).thenReturn(roboticsCaseDataSpec);
        when(jsonSchemaValidationService.validate(roboticsCaseDataSpec.toJsonString())).thenReturn(Set.of(
            ValidationMessage.builder().message("whoops").build()
        ));

        assertThrows(JsonSchemaValidationException.class, () -> roboticsNotifier.notifyRobotics(caseData, TOKEN));
    }

    @Test
    void shouldSendNotifications_whenValidUnspecClaim() throws JsonProcessingException {
        CaseData caseData = CaseData.builder().legacyCaseReference(LEGACY_REFERENCE)
            .caseAccessCategory(CaseCategory.UNSPEC_CLAIM).build();
        RoboticsCaseData roboticsCaseData = RoboticsCaseData.builder().build();

        when(featureToggleService.isRPAEmailEnabled()).thenReturn(true);
        when(roboticsDataMapper.toRoboticsCaseData(eq(caseData), eq(TOKEN))).thenReturn(roboticsCaseData);
        when(jsonSchemaValidationService.validate(roboticsCaseData.toJsonString())).thenReturn(Set.of());

        roboticsNotifier.notifyRobotics(caseData, TOKEN);

        verify(roboticsNotificationService).notifyRobotics(any(), anyBoolean(), any());
    }

    @Test
    void shouldThrowJsonSchemaValidationException_whenInvalidUnspecClaim() throws JsonProcessingException {
        RoboticsCaseData roboticsCaseData = RoboticsCaseData.builder().build();
        CaseData caseData = CaseData.builder().legacyCaseReference(LEGACY_REFERENCE)
            .caseAccessCategory(CaseCategory.UNSPEC_CLAIM).build();

        when(featureToggleService.isRPAEmailEnabled()).thenReturn(true);
        when(roboticsDataMapper.toRoboticsCaseData(eq(caseData), eq(TOKEN))).thenReturn(roboticsCaseData);
        when(jsonSchemaValidationService.validate(roboticsCaseData.toJsonString())).thenReturn(Set.of(
            ValidationMessage.builder().message("whoops").build()
        ));

        assertThrows(JsonSchemaValidationException.class, () -> roboticsNotifier.notifyRobotics(caseData, TOKEN));
    }
}
