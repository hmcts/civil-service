package uk.gov.hmcts.reform.civil.service.dashboardnotifications.caseproceedsoffline;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.genapplication.CaseLink;
import uk.gov.hmcts.reform.civil.model.genapplication.GeneralApplication;
import uk.gov.hmcts.reform.civil.model.genapplication.GeneralApplicationsDetails;
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
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CASE_PROCEED_IN_CASE_MAN_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_GENERAL_APPLICATION_INITIATE_APPLICATION_INACTIVE_CLAIMANT;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.wrapElements;

@ExtendWith(MockitoExtension.class)
class ApplicationsProceedOfflineClaimantDashboardServiceTest {

    private static final String AUTH_TOKEN = "BEARER";

    @Mock
    private DashboardScenariosService dashboardScenariosService;
    @Mock
    private DashboardNotificationService dashboardNotificationService;
    @Mock(lenient = true)
    private DashboardNotificationsParamsMapper mapper;
    @Mock
    private FeatureToggleService featureToggleService;

    @InjectMocks
    private ApplicationsProceedOfflineClaimantDashboardService service;

    @BeforeEach
    void setup() {
        when(mapper.mapCaseDataToParams(any())).thenReturn(new HashMap<>());
        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);
    }

    @Test
    void shouldRecordScenariosWhenEligible() {
        CaseLink caseLink = CaseLink.builder().caseReference("123").build();
        GeneralApplication application = GeneralApplication.builder()
            .caseLink(caseLink)
            .build();

        GeneralApplicationsDetails claimantDetail = GeneralApplicationsDetails.builder()
            .caseState("Awaiting Respondent Response")
            .build();

        List<Element<GeneralApplication>> applications = wrapElements(application);
        List<Element<GeneralApplicationsDetails>> claimantDetails = wrapElements(claimantDetail);

        CaseData caseData = CaseDataBuilder.builder()
            .ccdCaseReference(1234L)
            .applicant1Represented(uk.gov.hmcts.reform.civil.enums.YesOrNo.NO)
            .build()
            .toBuilder()
            .ccdState(CaseState.PROCEEDS_IN_HERITAGE_SYSTEM)
            .generalApplications(applications)
            .claimantGaAppDetails(claimantDetails)
            .build();

        service.notify(caseData, AUTH_TOKEN);

        verify(dashboardNotificationService).deleteByReferenceAndCitizenRole("1234", "Claimant");
        verify(dashboardScenariosService).recordScenarios(
            eq(AUTH_TOKEN),
            eq(SCENARIO_AAA6_GENERAL_APPLICATION_INITIATE_APPLICATION_INACTIVE_CLAIMANT.getScenario()),
            eq("1234"),
            any(ScenarioRequestParams.class)
        );
        verify(dashboardScenariosService).recordScenarios(
            eq(AUTH_TOKEN),
            eq(SCENARIO_AAA6_CASE_PROCEED_IN_CASE_MAN_CLAIMANT.getScenario()),
            eq("1234"),
            any(ScenarioRequestParams.class)
        );
    }

    @Test
    void shouldOnlyRecordInitiateScenarioWhenNoLiveApplications() {
        CaseLink caseLink = CaseLink.builder().caseReference("123").build();
        GeneralApplication application = GeneralApplication.builder()
            .caseLink(caseLink)
            .build();

        GeneralApplicationsDetails claimantDetail = GeneralApplicationsDetails.builder()
            .caseState("Application Closed")
            .build();

        List<Element<GeneralApplication>> applications = wrapElements(application);
        List<Element<GeneralApplicationsDetails>> claimantDetails = wrapElements(claimantDetail);

        CaseData caseData = CaseDataBuilder.builder()
            .ccdCaseReference(1234L)
            .applicant1Represented(uk.gov.hmcts.reform.civil.enums.YesOrNo.NO)
            .build()
            .toBuilder()
            .ccdState(CaseState.PROCEEDS_IN_HERITAGE_SYSTEM)
            .generalApplications(applications)
            .claimantGaAppDetails(claimantDetails)
            .build();

        service.notify(caseData, AUTH_TOKEN);

        verify(dashboardScenariosService, never()).recordScenarios(
            eq(AUTH_TOKEN),
            eq(SCENARIO_AAA6_CASE_PROCEED_IN_CASE_MAN_CLAIMANT.getScenario()),
            any(),
            any()
        );
    }

    @Test
    void shouldNotRecordWhenNotLip() {
        CaseData caseData = CaseDataBuilder.builder()
            .ccdCaseReference(1234L)
            .applicant1Represented(uk.gov.hmcts.reform.civil.enums.YesOrNo.YES)
            .build()
            .toBuilder()
            .ccdState(CaseState.PROCEEDS_IN_HERITAGE_SYSTEM)
            .build();

        service.notify(caseData, AUTH_TOKEN);

        verify(dashboardNotificationService, never()).deleteByReferenceAndCitizenRole(any(), any());
        verify(dashboardScenariosService, never()).recordScenarios(any(), any(), any(), any());
    }

    @Test
    void shouldNotRecordWhenFeatureDisabled() {
        CaseData caseData = CaseDataBuilder.builder()
            .ccdCaseReference(1234L)
            .applicant1Represented(uk.gov.hmcts.reform.civil.enums.YesOrNo.NO)
            .build()
            .toBuilder()
            .ccdState(CaseState.PROCEEDS_IN_HERITAGE_SYSTEM)
            .build();
        when(featureToggleService.isLipVLipEnabled()).thenReturn(false);

        service.notify(caseData, AUTH_TOKEN);

        verify(dashboardNotificationService, never()).deleteByReferenceAndCitizenRole(any(), any());
        verify(dashboardScenariosService, never()).recordScenarios(any(), any(), any(), any());
    }
}
