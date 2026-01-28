package uk.gov.hmcts.reform.civil.service.dashboardnotifications.trialreadycheck;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.AllocatedTrack;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;

import java.util.HashMap;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CP_TRIAL_ARRANGEMENTS_CHECK_DEFENDANT;

@ExtendWith(MockitoExtension.class)
class TrialReadyCheckDefendantDashboardServiceTest {

    private static final String AUTH_TOKEN = "BEARER";

    @Mock
    private DashboardScenariosService dashboardScenariosService;

    @Mock
    private DashboardNotificationsParamsMapper mapper;

    @InjectMocks
    private TrialReadyCheckDefendantDashboardService service;

    @BeforeEach
    void setUp() {
        when(mapper.mapCaseDataToParams(any())).thenReturn(new HashMap<>());
    }

    @Test
    void shouldNotifyDefendantWhenTrialReadyCheckRequired() {
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setRespondent1Represented(YesOrNo.NO);
        caseData.setTrialReadyRespondent1(null);
        caseData.setAllocatedTrack(AllocatedTrack.FAST_CLAIM);
        caseData.setCcdCaseReference(1234L);

        service.notifyCaseTrialReadyCheck(caseData, AUTH_TOKEN);

        verify(dashboardScenariosService).recordScenarios(
            AUTH_TOKEN,
            SCENARIO_AAA6_CP_TRIAL_ARRANGEMENTS_CHECK_DEFENDANT.getScenario(),
            "1234",
            ScenarioRequestParams.builder().params(new HashMap<>()).build()
        );
    }

    @Test
    void shouldNotifyDefendantWhenFastClaimTrack() {
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setRespondent1Represented(YesOrNo.NO);
        caseData.setTrialReadyRespondent1(null);
        caseData.setAllocatedTrack(AllocatedTrack.FAST_CLAIM);
        caseData.setCcdCaseReference(5678L);

        service.notifyCaseTrialReadyCheck(caseData, AUTH_TOKEN);

        verify(dashboardScenariosService).recordScenarios(
            AUTH_TOKEN,
            SCENARIO_AAA6_CP_TRIAL_ARRANGEMENTS_CHECK_DEFENDANT.getScenario(),
            "5678",
            ScenarioRequestParams.builder().params(new HashMap<>()).build()
        );
    }

    @Test
    void shouldUseTrialReadyCheckScenarioWhenTrialReadyNull() {
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setRespondent1Represented(YesOrNo.NO);
        caseData.setTrialReadyRespondent1(null);
        caseData.setAllocatedTrack(AllocatedTrack.FAST_CLAIM);
        caseData.setCcdCaseReference(9012L);

        service.notifyCaseTrialReadyCheck(caseData, AUTH_TOKEN);

        verify(dashboardScenariosService).recordScenarios(
            AUTH_TOKEN,
            SCENARIO_AAA6_CP_TRIAL_ARRANGEMENTS_CHECK_DEFENDANT.getScenario(),
            "9012",
            ScenarioRequestParams.builder().params(new HashMap<>()).build()
        );
    }

    @Test
    void shouldNotNotifyDefendantWhenRepresentedYes() {
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setRespondent1Represented(YesOrNo.YES);
        caseData.setTrialReadyRespondent1(null);
        caseData.setAllocatedTrack(AllocatedTrack.FAST_CLAIM);
        caseData.setCcdCaseReference(3456L);

        service.notifyCaseTrialReadyCheck(caseData, AUTH_TOKEN);

        verifyNoInteractions(dashboardScenariosService);
    }
}
