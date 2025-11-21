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
import uk.gov.hmcts.reform.civil.model.genapplication.GADetailsRespondentSol;
import uk.gov.hmcts.reform.civil.model.genapplication.GeneralApplication;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
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
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_GENERAL_APPLICATION_INITIATE_APPLICATION_INACTIVE_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_UPDATE_CASE_PROCEED_IN_CASE_MAN_DEFENDANT;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.wrapElements;

@ExtendWith(MockitoExtension.class)
class ApplicationsProceedOfflineDefendantDashboardServiceTest {

    private static final String AUTH_TOKEN = "BEARER";

    @Mock
    private DashboardScenariosService dashboardScenariosService;
    @Mock
    private DashboardNotificationService dashboardNotificationService;
    @Mock(lenient = true)
    private DashboardNotificationsParamsMapper mapper;

    @InjectMocks
    private ApplicationsProceedOfflineDefendantDashboardService service;

    @BeforeEach
    void setup() {
        when(mapper.mapCaseDataToParams(any())).thenReturn(new HashMap<>());
    }

    @Test
    void shouldRecordScenariosWhenEligible() {
        List<Element<GeneralApplication>> applications = wrapElements(
            GeneralApplication.builder().caseLink(CaseLink.builder().caseReference("123").build()).build()
        );
        List<Element<GADetailsRespondentSol>> respondentDetails = wrapElements(
            GADetailsRespondentSol.builder().caseState("Awaiting Respondent Response").build()
        );

        CaseData caseData = CaseDataBuilder.builder().build().toBuilder()
            .ccdState(CaseState.PROCEEDS_IN_HERITAGE_SYSTEM)
            .ccdCaseReference(5555L)
            .respondent1Represented(uk.gov.hmcts.reform.civil.enums.YesOrNo.NO)
            .generalApplications(applications)
            .respondentSolGaAppDetails(respondentDetails)
            .build();

        service.notify(caseData, AUTH_TOKEN);

        verify(dashboardNotificationService).deleteByReferenceAndCitizenRole("5555", "Defendant");
        verify(dashboardScenariosService).recordScenarios(
            eq(AUTH_TOKEN),
            eq(SCENARIO_AAA6_GENERAL_APPLICATION_INITIATE_APPLICATION_INACTIVE_DEFENDANT.getScenario()),
            eq("5555"),
            any(ScenarioRequestParams.class)
        );
        verify(dashboardScenariosService).recordScenarios(
            eq(AUTH_TOKEN),
            eq(SCENARIO_AAA6_UPDATE_CASE_PROCEED_IN_CASE_MAN_DEFENDANT.getScenario()),
            eq("5555"),
            any(ScenarioRequestParams.class)
        );
    }

    @Test
    void shouldOnlyRecordInitiateScenarioWhenNoLiveApplications() {
        List<Element<GeneralApplication>> applications = wrapElements(
            GeneralApplication.builder().caseLink(CaseLink.builder().caseReference("123").build()).build()
        );
        List<Element<GADetailsRespondentSol>> respondentDetails = wrapElements(
            GADetailsRespondentSol.builder().caseState("Application Dismissed").build()
        );

        CaseData caseData = CaseDataBuilder.builder().build().toBuilder()
            .ccdState(CaseState.PROCEEDS_IN_HERITAGE_SYSTEM)
            .ccdCaseReference(5555L)
            .respondent1Represented(uk.gov.hmcts.reform.civil.enums.YesOrNo.NO)
            .generalApplications(applications)
            .respondentSolGaAppDetails(respondentDetails)
            .build();

        service.notify(caseData, AUTH_TOKEN);

        verify(dashboardScenariosService, never()).recordScenarios(
            eq(AUTH_TOKEN),
            eq(SCENARIO_AAA6_UPDATE_CASE_PROCEED_IN_CASE_MAN_DEFENDANT.getScenario()),
            any(),
            any()
        );
    }

    @Test
    void shouldNotRecordWhenNotLip() {
        CaseData caseData = CaseDataBuilder.builder().build().toBuilder()
            .ccdState(CaseState.PROCEEDS_IN_HERITAGE_SYSTEM)
            .ccdCaseReference(5555L)
            .respondent1Represented(uk.gov.hmcts.reform.civil.enums.YesOrNo.YES)
            .build();

        service.notify(caseData, AUTH_TOKEN);

        verify(dashboardNotificationService, never()).deleteByReferenceAndCitizenRole(any(), any());
        verify(dashboardScenariosService, never()).recordScenarios(any(), any(), any(), any());
    }
}
