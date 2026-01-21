package uk.gov.hmcts.reform.civil.service.dashboardnotifications.trialreadyrespondent1;

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
import uk.gov.hmcts.reform.dashboard.services.DashboardNotificationService;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;
import uk.gov.hmcts.reform.dashboard.services.TaskListService;

import java.util.HashMap;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CP_TRIAL_ARRANGEMENTS_CHECK_CLAIMANT;

@ExtendWith(MockitoExtension.class)
class TrialReadyCheckRespondent1ClaimantDashboardServiceTest {

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
    private TrialReadyCheckRespondent1ClaimantDashboardService service;

    @BeforeEach
    void setUp() {
        when(mapper.mapCaseDataToParams(any())).thenReturn(new HashMap<>());
    }

    @Test
    void shouldNotifyClaimantWhenTrialReadyCheckRespondent1Required() {
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setApplicant1Represented(YesOrNo.NO);
        caseData.setTrialReadyApplicant(null);
        caseData.setAllocatedTrack(AllocatedTrack.FAST_CLAIM);
        caseData.setCcdCaseReference(1234L);

        service.notifyTrialReadyCheckRespondent1(caseData, AUTH_TOKEN);

        verify(dashboardNotificationService).deleteByReferenceAndCitizenRole("1234", "CLAIMANT");
        verify(taskListService).makeProgressAbleTasksInactiveForCaseIdentifierAndRole("1234", "CLAIMANT");
        verify(dashboardScenariosService).recordScenarios(
            AUTH_TOKEN,
            SCENARIO_AAA6_CP_TRIAL_ARRANGEMENTS_CHECK_CLAIMANT.getScenario(),
            "1234",
            ScenarioRequestParams.builder().params(new HashMap<>()).build()
        );
    }

    @Test
    void shouldNotifyClaimantWhenFastClaimTrack() {
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setApplicant1Represented(YesOrNo.NO);
        caseData.setTrialReadyApplicant(null);
        caseData.setAllocatedTrack(AllocatedTrack.FAST_CLAIM);
        caseData.setCcdCaseReference(5678L);

        service.notifyTrialReadyCheckRespondent1(caseData, AUTH_TOKEN);

        verify(dashboardNotificationService).deleteByReferenceAndCitizenRole("5678", "CLAIMANT");
        verify(taskListService).makeProgressAbleTasksInactiveForCaseIdentifierAndRole("5678", "CLAIMANT");
        verify(dashboardScenariosService).recordScenarios(
            AUTH_TOKEN,
            SCENARIO_AAA6_CP_TRIAL_ARRANGEMENTS_CHECK_CLAIMANT.getScenario(),
            "5678",
            ScenarioRequestParams.builder().params(new HashMap<>()).build()
        );
    }

    @Test
    void shouldUseTrialReadyCheckScenarioWhenTrialReadyNull() {
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setApplicant1Represented(YesOrNo.NO);
        caseData.setTrialReadyApplicant(null);
        caseData.setAllocatedTrack(AllocatedTrack.FAST_CLAIM);
        caseData.setCcdCaseReference(9012L);

        service.notifyTrialReadyCheckRespondent1(caseData, AUTH_TOKEN);

        verify(dashboardScenariosService).recordScenarios(
            AUTH_TOKEN,
            SCENARIO_AAA6_CP_TRIAL_ARRANGEMENTS_CHECK_CLAIMANT.getScenario(),
            "9012",
            ScenarioRequestParams.builder().params(new HashMap<>()).build()
        );
    }

    @Test
    void shouldNotNotifyClaimantWhenRepresentedYes() {
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setApplicant1Represented(YesOrNo.YES);
        caseData.setTrialReadyApplicant(null);
        caseData.setAllocatedTrack(AllocatedTrack.FAST_CLAIM);
        caseData.setCcdCaseReference(3456L);

        service.notifyTrialReadyCheckRespondent1(caseData, AUTH_TOKEN);

        verifyNoInteractions(dashboardNotificationService);
        verifyNoInteractions(taskListService);
        verifyNoInteractions(dashboardScenariosService);
    }
}
