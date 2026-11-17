package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.trialarrangementsnotifyotherparty;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardNotificationDispatcher;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardNotificationRegistry;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskIds;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;

import java.util.HashMap;
import java.util.List;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskContext;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardCaseType;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.trialarrangementsnotifyotherparty.TrialArrangementsNotifyOtherPartyClaimantDashboardService;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.trialarrangementsnotifyotherparty.TrialArrangementsNotifyOtherPartyDefendantDashboardService;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.refEq;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CP_TRIAL_ARRANGEMENTS_NOTIFY_OTHER_PARTY_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CP_TRIAL_ARRANGEMENTS_NOTIFY_OTHER_PARTY_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CP_TRIAL_ARRANGEMENTS_NOTIFY_OTHER_PARTY_LR_CLAIMANT;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrialArrangementsNotifyOtherPartyDashboardTaskTest {

    private static final String AUTH_TOKEN = "Bearer token";

    @Mock
    private TrialArrangementsNotifyOtherPartyClaimantDashboardService claimantDashboardService;
    @Mock
    private TrialArrangementsNotifyOtherPartyDefendantDashboardService defendantDashboardService;
    @Mock
    private DashboardTaskContext context;

    private final CaseData caseData = CaseData.builder().ccdCaseReference(1L).build();

    @BeforeEach
    void setupContext() {
        when(context.caseData()).thenReturn(caseData);
        when(context.authToken()).thenReturn(AUTH_TOKEN);
    }

    @Test
    void claimantTaskShouldDelegateToDashboardService() {
        TrialArrangementsNotifyOtherPartyClaimantDashboardTask task = new TrialArrangementsNotifyOtherPartyClaimantDashboardTask(claimantDashboardService);

        task.execute(context);

        verify(claimantDashboardService).notifyTrialArrangementsNotifyOtherParty(caseData, AUTH_TOKEN);
    }

    @Test
    void defendantTaskShouldDelegateToDashboardService() {
        TrialArrangementsNotifyOtherPartyDefendantDashboardTask task = new TrialArrangementsNotifyOtherPartyDefendantDashboardTask(defendantDashboardService);

        task.execute(context);

        verify(defendantDashboardService).notifyTrialArrangementsNotifyOtherParty(caseData, AUTH_TOKEN);
    }

    @ParameterizedTest
    @CsvSource(value = {"NO,YES", "NO,NO", "NO,NULL", "YES,YES", "YES,NO", "YES,NULL"}, nullValues = "NULL")
    void shouldNotifyOnlyClaimantForDefendantFlow(YesOrNo claimantRepresented, YesOrNo defendantReady) {
        when(context.caseType()).thenReturn(DashboardCaseType.CIVIL);
        caseData.setApplicant1Represented(claimantRepresented);
        caseData.setRespondent1Represented(YesOrNo.NO);
        caseData.setTrialReadyRespondent1(defendantReady);
        caseData.setBusinessProcess(BusinessProcess.ready(CaseEvent.RESPONDENT1_TRIAL_READY_NOTIFY_OTHERS));
        DashboardScenariosService scenarios = mock(DashboardScenariosService.class);
        DashboardNotificationsParamsMapper mapper = mock(DashboardNotificationsParamsMapper.class);
        when(mapper.mapCaseDataToParams(caseData)).thenReturn(new HashMap<>());
        TrialArrangementsNotifyOtherPartyDashboardTaskContributor contributor =
            new TrialArrangementsNotifyOtherPartyDashboardTaskContributor(
                new TrialArrangementsNotifyOtherPartyClaimantDashboardTask(
                    new TrialArrangementsNotifyOtherPartyClaimantDashboardService(scenarios, mapper)),
                new TrialArrangementsNotifyOtherPartyDefendantDashboardTask(
                    new TrialArrangementsNotifyOtherPartyDefendantDashboardService(scenarios, mapper)));
        DashboardNotificationRegistry registry = new DashboardNotificationRegistry(List.of(contributor));

        new DashboardNotificationDispatcher(registry).dispatch(DashboardTaskIds.TRIAL_ARRANGEMENTS_NOTIFY_OTHER_PARTY, context);

        String scenario = claimantRepresented == YesOrNo.NO
            ? SCENARIO_AAA6_CP_TRIAL_ARRANGEMENTS_NOTIFY_OTHER_PARTY_CLAIMANT.getScenario()
            : SCENARIO_AAA6_CP_TRIAL_ARRANGEMENTS_NOTIFY_OTHER_PARTY_LR_CLAIMANT.getScenario();
        verify(scenarios).recordScenarios(eq(AUTH_TOKEN), eq(scenario), eq("1"),
            refEq(new ScenarioRequestParams(new HashMap<>())));
        verifyNoMoreInteractions(scenarios);
    }

    @ParameterizedTest
    @EnumSource(value = CaseEvent.class, names = {"APPLICANT_TRIAL_READY_NOTIFY_OTHERS", "GENERATE_TRIAL_READY_DOCUMENT_APPLICANT"})
    void shouldNotifyOnlyDefendantForApplicantFlowsEvenWhenBothPartiesConfirmed(CaseEvent event) {
        when(context.caseType()).thenReturn(DashboardCaseType.CIVIL);
        caseData.setApplicant1Represented(YesOrNo.NO);
        caseData.setRespondent1Represented(YesOrNo.NO);
        caseData.setTrialReadyRespondent1(YesOrNo.YES);
        caseData.setBusinessProcess(BusinessProcess.ready(event));
        DashboardScenariosService scenarios = mock(DashboardScenariosService.class);
        DashboardNotificationsParamsMapper mapper = mock(DashboardNotificationsParamsMapper.class);
        when(mapper.mapCaseDataToParams(caseData)).thenReturn(new HashMap<>());
        TrialArrangementsNotifyOtherPartyDashboardTaskContributor contributor =
            new TrialArrangementsNotifyOtherPartyDashboardTaskContributor(
                new TrialArrangementsNotifyOtherPartyClaimantDashboardTask(
                    new TrialArrangementsNotifyOtherPartyClaimantDashboardService(scenarios, mapper)),
                new TrialArrangementsNotifyOtherPartyDefendantDashboardTask(
                    new TrialArrangementsNotifyOtherPartyDefendantDashboardService(scenarios, mapper)));
        DashboardNotificationRegistry registry = new DashboardNotificationRegistry(List.of(contributor));

        new DashboardNotificationDispatcher(registry).dispatch(DashboardTaskIds.TRIAL_ARRANGEMENTS_NOTIFY_OTHER_PARTY, context);

        verify(scenarios).recordScenarios(eq(AUTH_TOKEN),
            eq(SCENARIO_AAA6_CP_TRIAL_ARRANGEMENTS_NOTIFY_OTHER_PARTY_DEFENDANT.getScenario()), eq("1"),
            refEq(new ScenarioRequestParams(new HashMap<>())));
        verifyNoMoreInteractions(scenarios);
    }
}
