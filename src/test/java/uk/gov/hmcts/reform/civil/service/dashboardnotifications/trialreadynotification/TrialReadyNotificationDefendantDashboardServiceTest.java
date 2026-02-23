package uk.gov.hmcts.reform.civil.service.dashboardnotifications.trialreadynotification;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.sdo.ClaimsTrack;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.service.sdo.SdoCaseClassificationService;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;

import java.util.HashMap;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CP_TRIAL_ARRANGEMENTS_REQUIRED_DEFENDANT;

@ExtendWith(MockitoExtension.class)
class TrialReadyNotificationDefendantDashboardServiceTest {

    private static final String AUTH_TOKEN = "BEARER";

    @Mock
    private DashboardScenariosService dashboardScenariosService;
    @Mock
    private DashboardNotificationsParamsMapper mapper;
    @Mock
    private SdoCaseClassificationService sdoCaseClassificationService;

    @InjectMocks
    private TrialReadyNotificationDefendantDashboardService service;

    @BeforeEach
    void setup() {
        when(mapper.mapCaseDataToParams(any())).thenReturn(new HashMap<>());
    }

    @Test
    void shouldRecordScenarioWhenEligible() {
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setRespondent1Represented(YesOrNo.NO);
        caseData.setCcdCaseReference(1234L);
        caseData.setDrawDirectionsOrderRequired(YesOrNo.NO);
        caseData.setClaimsTrack(ClaimsTrack.fastTrack);
        caseData.setTrialReadyRespondent1(null);

        when(sdoCaseClassificationService.isFastTrack(caseData)).thenReturn(true);

        service.notifyTrialReadyNotification(caseData, AUTH_TOKEN);

        verify(dashboardScenariosService).recordScenarios(
            AUTH_TOKEN,
            SCENARIO_AAA6_CP_TRIAL_ARRANGEMENTS_REQUIRED_DEFENDANT.getScenario(),
            "1234",
            ScenarioRequestParams.builder().params(new HashMap<>()).build()
        );
    }

    @Test
    void shouldNotRecordScenarioWhenDefendantRepresented() {
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setRespondent1Represented(YesOrNo.YES);
        caseData.setCcdCaseReference(1234L);
        caseData.setDrawDirectionsOrderRequired(YesOrNo.NO);
        caseData.setClaimsTrack(ClaimsTrack.fastTrack);
        caseData.setTrialReadyRespondent1(null);

        service.notifyTrialReadyNotification(caseData, AUTH_TOKEN);

        verifyNoInteractions(dashboardScenariosService);
    }

    @Test
    void shouldNotRecordScenarioWhenNotFastTrack() {
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setRespondent1Represented(YesOrNo.NO);
        caseData.setCcdCaseReference(1234L);
        caseData.setDrawDirectionsOrderRequired(YesOrNo.YES);
        caseData.setDrawDirectionsOrderSmallClaims(YesOrNo.NO);
        caseData.setTrialReadyRespondent1(null);

        when(sdoCaseClassificationService.isFastTrack(caseData)).thenReturn(false);

        service.notifyTrialReadyNotification(caseData, AUTH_TOKEN);

        verifyNoInteractions(dashboardScenariosService);
    }

    @Test
    void shouldNotRecordScenarioWhenTrialReadyAlreadySet() {
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setRespondent1Represented(YesOrNo.NO);
        caseData.setCcdCaseReference(1234L);
        caseData.setDrawDirectionsOrderRequired(YesOrNo.NO);
        caseData.setClaimsTrack(ClaimsTrack.fastTrack);
        caseData.setTrialReadyRespondent1(YesOrNo.YES);

        when(sdoCaseClassificationService.isFastTrack(caseData)).thenReturn(true);

        service.notifyTrialReadyNotification(caseData, AUTH_TOKEN);

        verifyNoInteractions(dashboardScenariosService);
    }
}

