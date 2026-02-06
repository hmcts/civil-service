package uk.gov.hmcts.reform.civil.service.dashboardnotifications.caseproceedsoffline;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.genapplication.CaseLink;
import uk.gov.hmcts.reform.civil.model.genapplication.GeneralApplication;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;
import uk.gov.hmcts.reform.dashboard.services.DashboardNotificationService;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;

import java.util.HashMap;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_GENERAL_APPLICATION_INITIATE_APPLICATION_INACTIVE_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CASE_PROCEED_IN_CASE_MAN_CLAIMANT;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.wrapElements;

@ExtendWith(MockitoExtension.class)
class ApplicationsProceedOfflineDashboardServiceTest {

    @Mock
    private DashboardScenariosService dashboardScenariosService;
    @Mock
    private DashboardNotificationService dashboardNotificationService;
    @Mock
    private DashboardNotificationsParamsMapper mapper;
    @Mock
    private FeatureToggleService featureToggleService;

    private TestDashboardService dashboardService;

    private static CaseData baseCaseData() {
        CaseLink caseLink = new CaseLink();
        caseLink.setCaseReference("123");

        GeneralApplication generalApplication = new GeneralApplication();
        generalApplication.setCaseLink(caseLink);

        List<Element<GeneralApplication>> generalApplications = wrapElements(generalApplication);

        return CaseDataBuilder.builder()
            .ccdCaseReference(1234L)
            .build()
            .toBuilder()
            .ccdState(CaseState.PROCEEDS_IN_HERITAGE_SYSTEM)
            .generalApplications(generalApplications)
            .build();
    }

    @BeforeEach
    void setup() {
        dashboardService = new TestDashboardService(
            dashboardScenariosService,
            dashboardNotificationService,
            mapper,
            featureToggleService
        );
        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);
    }

    @Test
    void shouldRecordInactiveScenarioWhenNoLiveApplications() {
        final CaseData caseData = baseCaseData();
        dashboardService.lip = true;
        dashboardService.applicationStates = List.of("Application Closed");
        stubMapper();

        dashboardService.notify(caseData, "auth");

        verify(dashboardNotificationService).deleteByReferenceAndCitizenRole("1234", "Claimant");
        verify(dashboardScenariosService).recordScenarios(
            eq("auth"),
            eq(SCENARIO_AAA6_GENERAL_APPLICATION_INITIATE_APPLICATION_INACTIVE_CLAIMANT.getScenario()),
            eq("1234"),
            any(ScenarioRequestParams.class)
        );
        verify(dashboardScenariosService, never()).recordScenarios(
            eq("auth"),
            eq(SCENARIO_AAA6_CASE_PROCEED_IN_CASE_MAN_CLAIMANT.getScenario()),
            any(),
            any()
        );
    }

    @Test
    void shouldRecordActiveScenarioWhenLiveApplicationsPresent() {
        final CaseData caseData = baseCaseData();
        dashboardService.lip = true;
        dashboardService.applicationStates = List.of("Awaiting Judge");
        stubMapper();

        dashboardService.notify(caseData, "auth");

        verify(dashboardScenariosService).recordScenarios(
            eq("auth"),
            eq(SCENARIO_AAA6_GENERAL_APPLICATION_INITIATE_APPLICATION_INACTIVE_CLAIMANT.getScenario()),
            eq("1234"),
            any(ScenarioRequestParams.class)
        );
        verify(dashboardScenariosService).recordScenarios(
            eq("auth"),
            eq(SCENARIO_AAA6_CASE_PROCEED_IN_CASE_MAN_CLAIMANT.getScenario()),
            eq("1234"),
            any(ScenarioRequestParams.class)
        );
    }

    @Test
    void shouldSkipWhenNotLip() {
        CaseData caseData = baseCaseData();
        dashboardService.lip = false;

        dashboardService.notify(caseData, "auth");

        verifyNoInteractions(dashboardNotificationService);
        verifyNoInteractions(dashboardScenariosService);
    }

    @Test
    void shouldSkipWhenStateNotProceedingOffline() {
        CaseData caseData = baseCaseData().toBuilder()
            .ccdState(CaseState.JUDICIAL_REFERRAL)
            .build();
        dashboardService.lip = true;

        dashboardService.notify(caseData, "auth");

        verifyNoInteractions(dashboardNotificationService);
        verifyNoInteractions(dashboardScenariosService);
    }

    @Test
    void shouldSkipWhenFeatureDisabled() {
        CaseData caseData = baseCaseData();
        dashboardService.lip = true;
        when(featureToggleService.isLipVLipEnabled()).thenReturn(false);

        dashboardService.notify(caseData, "auth");

        verifyNoInteractions(dashboardNotificationService);
        verifyNoInteractions(dashboardScenariosService);
    }

    private void stubMapper() {
        when(mapper.mapCaseDataToParams(any())).thenReturn(new HashMap<>());
    }

    private static class TestDashboardService extends ApplicationsProceedOfflineDashboardService {

        boolean lip = true;
        List<String> applicationStates = List.of();

        TestDashboardService(DashboardScenariosService dashboardScenariosService,
                             DashboardNotificationService dashboardNotificationService,
                             DashboardNotificationsParamsMapper mapper,
                             FeatureToggleService featureToggleService) {
            super(dashboardScenariosService, dashboardNotificationService, mapper, featureToggleService);
        }

        @Override
        protected boolean isLip(CaseData caseData) {
            return lip;
        }

        @Override
        protected String inactiveScenarioId() {
            return SCENARIO_AAA6_GENERAL_APPLICATION_INITIATE_APPLICATION_INACTIVE_CLAIMANT.getScenario();
        }

        @Override
        protected String activeScenarioId() {
            return SCENARIO_AAA6_CASE_PROCEED_IN_CASE_MAN_CLAIMANT.getScenario();
        }

        @Override
        protected String partyLabel() {
            return "Claimant";
        }

        @Override
        protected List<String> partyApplicationStates(CaseData caseData) {
            return applicationStates;
        }
    }
}
