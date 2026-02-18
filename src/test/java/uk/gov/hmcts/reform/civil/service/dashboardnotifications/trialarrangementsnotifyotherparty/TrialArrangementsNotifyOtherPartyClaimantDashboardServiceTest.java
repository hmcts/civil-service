package uk.gov.hmcts.reform.civil.service.dashboardnotifications.trialarrangementsnotifyotherparty;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;
import uk.gov.hmcts.reform.dashboard.services.DashboardNotificationService;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;
import uk.gov.hmcts.reform.dashboard.services.TaskListService;

import java.util.HashMap;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CP_TRIAL_ARRANGEMENTS_NOTIFY_OTHER_PARTY_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CP_TRIAL_ARRANGEMENTS_NOTIFY_OTHER_PARTY_LR_CLAIMANT;

@ExtendWith(MockitoExtension.class)
class TrialArrangementsNotifyOtherPartyClaimantDashboardServiceTest {

    private static final String AUTH_TOKEN = "BEARER";

    @Mock
    private DashboardScenariosService dashboardScenariosService;
    @Mock
    private DashboardNotificationService dashboardNotificationService;
    @Mock
    private TaskListService taskListService;
    @Mock
    private DashboardNotificationsParamsMapper mapper;

    @InjectMocks
    private TrialArrangementsNotifyOtherPartyClaimantDashboardService service;

    @BeforeEach
    void setUp() {
        when(mapper.mapCaseDataToParams(any())).thenReturn(new HashMap<>());
    }

    @Test
    void shouldNotifyClaimantWhenTrialArrangementsNotifyOtherPartyAndUnrepresented() {
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setApplicant1Represented(YesOrNo.NO);
        caseData.setCcdCaseReference(1234L);

        service.notifyTrialArrangementsNotifyOtherParty(caseData, AUTH_TOKEN);

        verify(dashboardNotificationService).deleteByReferenceAndCitizenRole("1234", "CLAIMANT");
        verify(taskListService).makeProgressAbleTasksInactiveForCaseIdentifierAndRole("1234", "CLAIMANT");
        verify(dashboardScenariosService).recordScenarios(
            AUTH_TOKEN,
            SCENARIO_AAA6_CP_TRIAL_ARRANGEMENTS_NOTIFY_OTHER_PARTY_CLAIMANT.getScenario(),
            "1234",
            ScenarioRequestParams.builder().params(new HashMap<>()).build()
        );
    }

    @Test
    void shouldUseClaimantScenarioWhenUnrepresented() {
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setApplicant1Represented(YesOrNo.NO);
        caseData.setCcdCaseReference(5678L);

        service.notifyTrialArrangementsNotifyOtherParty(caseData, AUTH_TOKEN);

        verify(dashboardScenariosService).recordScenarios(
            AUTH_TOKEN,
            SCENARIO_AAA6_CP_TRIAL_ARRANGEMENTS_NOTIFY_OTHER_PARTY_CLAIMANT.getScenario(),
            "5678",
            ScenarioRequestParams.builder().params(new HashMap<>()).build()
        );
    }

    @Test
    void shouldUseLRClaimantScenarioWhenRepresented() {
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setApplicant1Represented(YesOrNo.YES);
        caseData.setCcdCaseReference(9012L);

        service.notifyTrialArrangementsNotifyOtherParty(caseData, AUTH_TOKEN);

        verify(dashboardScenariosService).recordScenarios(
            AUTH_TOKEN,
            SCENARIO_AAA6_CP_TRIAL_ARRANGEMENTS_NOTIFY_OTHER_PARTY_LR_CLAIMANT.getScenario(),
            "9012",
            ScenarioRequestParams.builder().params(new HashMap<>()).build()
        );
        verifyNoInteractions(dashboardNotificationService);
        verifyNoInteractions(taskListService);
    }

    @Test
    void shouldRecordScenarioButNotDeleteOrMakeInactiveWhenRepresented() {
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setApplicant1Represented(YesOrNo.YES);
        caseData.setCcdCaseReference(3456L);

        service.notifyTrialArrangementsNotifyOtherParty(caseData, AUTH_TOKEN);

        verify(dashboardScenariosService).recordScenarios(
            AUTH_TOKEN,
            SCENARIO_AAA6_CP_TRIAL_ARRANGEMENTS_NOTIFY_OTHER_PARTY_LR_CLAIMANT.getScenario(),
            "3456",
            ScenarioRequestParams.builder().params(new HashMap<>()).build()
        );
        verifyNoInteractions(dashboardNotificationService);
        verifyNoInteractions(taskListService);
    }
}
