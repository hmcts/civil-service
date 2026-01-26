package uk.gov.hmcts.reform.civil.service.dashboardnotifications.respondtoquery;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;

import java.util.HashMap;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_QUERY_RESPONDED_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_QUERY_RESPONDED_CLAIMANT_DELETE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_QUERY_RESPONDED_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_QUERY_RESPONDED_DEFENDANT_DELETE;

@ExtendWith(MockitoExtension.class)
class RespondToQueryDashboardServiceTest {

    private static final long CASE_REFERENCE = 1234567890123456L;
    private static final String AUTH_TOKEN = "Bearer";

    @Mock
    private DashboardScenariosService dashboardScenariosService;
    @Mock
    private DashboardNotificationsParamsMapper mapper;

    @InjectMocks
    private RespondToQueryDashboardService service;

    @BeforeEach
    void setup() {
        lenient().when(mapper.mapCaseDataToParams(any())).thenReturn(new HashMap<>());
    }

    @Test
    void shouldCreateNotificationsForApplicantLip() {
        CaseData caseData = lipCaseData(true, false);

        service.notifyRespondToQuery(caseData, AUTH_TOKEN);

        verify(dashboardScenariosService).recordScenarios(
            eq(AUTH_TOKEN),
            eq(SCENARIO_AAA6_QUERY_RESPONDED_CLAIMANT_DELETE.getScenario()),
            eq(String.valueOf(CASE_REFERENCE)),
            any(ScenarioRequestParams.class)
        );
        verify(dashboardScenariosService).recordScenarios(
            eq(AUTH_TOKEN),
            eq(SCENARIO_AAA6_QUERY_RESPONDED_CLAIMANT.getScenario()),
            eq(String.valueOf(CASE_REFERENCE)),
            any(ScenarioRequestParams.class)
        );
        verify(dashboardScenariosService, never()).recordScenarios(
            eq(AUTH_TOKEN),
            eq(SCENARIO_AAA6_QUERY_RESPONDED_DEFENDANT.getScenario()),
            any(),
            any(ScenarioRequestParams.class)
        );
    }

    @Test
    void shouldCreateNotificationsForRespondentLip() {
        CaseData caseData = lipCaseData(false, true);

        service.notifyRespondToQuery(caseData, AUTH_TOKEN);

        verify(dashboardScenariosService).recordScenarios(
            eq(AUTH_TOKEN),
            eq(SCENARIO_AAA6_QUERY_RESPONDED_DEFENDANT_DELETE.getScenario()),
            eq(String.valueOf(CASE_REFERENCE)),
            any(ScenarioRequestParams.class)
        );
        verify(dashboardScenariosService).recordScenarios(
            eq(AUTH_TOKEN),
            eq(SCENARIO_AAA6_QUERY_RESPONDED_DEFENDANT.getScenario()),
            eq(String.valueOf(CASE_REFERENCE)),
            any(ScenarioRequestParams.class)
        );
        verify(dashboardScenariosService, never()).recordScenarios(
            eq(AUTH_TOKEN),
            eq(SCENARIO_AAA6_QUERY_RESPONDED_CLAIMANT.getScenario()),
            any(),
            any(ScenarioRequestParams.class)
        );
    }

    @Test
    void shouldCreateNotificationsForBothPartiesWhenBothLip() {
        CaseData caseData = lipCaseData(true, true);

        service.notifyRespondToQuery(caseData, AUTH_TOKEN);

        verify(dashboardScenariosService).recordScenarios(
            eq(AUTH_TOKEN),
            eq(SCENARIO_AAA6_QUERY_RESPONDED_CLAIMANT_DELETE.getScenario()),
            eq(String.valueOf(CASE_REFERENCE)),
            any(ScenarioRequestParams.class)
        );
        verify(dashboardScenariosService).recordScenarios(
            eq(AUTH_TOKEN),
            eq(SCENARIO_AAA6_QUERY_RESPONDED_DEFENDANT_DELETE.getScenario()),
            eq(String.valueOf(CASE_REFERENCE)),
            any(ScenarioRequestParams.class)
        );
        verify(dashboardScenariosService).recordScenarios(
            eq(AUTH_TOKEN),
            eq(SCENARIO_AAA6_QUERY_RESPONDED_CLAIMANT.getScenario()),
            eq(String.valueOf(CASE_REFERENCE)),
            any(ScenarioRequestParams.class)
        );
        verify(dashboardScenariosService).recordScenarios(
            eq(AUTH_TOKEN),
            eq(SCENARIO_AAA6_QUERY_RESPONDED_DEFENDANT.getScenario()),
            eq(String.valueOf(CASE_REFERENCE)),
            any(ScenarioRequestParams.class)
        );
    }

    @Test
    void shouldNotRecordWhenNoLipParty() {
        CaseData caseData = CaseDataBuilder.builder()
            .ccdCaseReference(CASE_REFERENCE)
            .businessProcess(BusinessProcess.builder().processInstanceId("process").build())
            .applicant1Represented(YesOrNo.YES)
            .respondent1Represented(YesOrNo.YES)
            .build();

        service.notifyRespondToQuery(caseData, AUTH_TOKEN);

        verifyNoMoreInteractions(dashboardScenariosService);
    }

    private CaseData lipCaseData(boolean applicantLip, boolean defendantLip) {
        CaseDataBuilder builder = CaseDataBuilder.builder()
            .ccdCaseReference(CASE_REFERENCE)
            .businessProcess(BusinessProcess.builder().processInstanceId("process").build());
        builder.applicant1Represented(applicantLip ? YesOrNo.NO : YesOrNo.YES);
        builder.respondent1Represented(defendantLip ? YesOrNo.NO : YesOrNo.YES);
        return builder.build();
    }
}
