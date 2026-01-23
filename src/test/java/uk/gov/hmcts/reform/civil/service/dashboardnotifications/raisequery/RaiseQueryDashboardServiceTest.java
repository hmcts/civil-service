package uk.gov.hmcts.reform.civil.service.dashboardnotifications.raisequery;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.CaseRole;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.querymanagement.CaseMessage;
import uk.gov.hmcts.reform.civil.model.querymanagement.CaseQueriesCollection;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.CoreCaseUserService;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.service.querymanagement.QueryManagementCamundaService;
import uk.gov.hmcts.reform.civil.service.querymanagement.QueryManagementVariables;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_QUERY_RAISED_BY_OTHER_PARTY_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_QUERY_RAISED_BY_OTHER_PARTY_CLAIMANT_DELETE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_QUERY_RAISED_BY_OTHER_PARTY_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_QUERY_RAISED_BY_OTHER_PARTY_DEFENDANT_DELETE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_VIEW_AVAILABLE_MESSAGES_TO_THE_COURT;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.element;

@ExtendWith(MockitoExtension.class)
class RaiseQueryDashboardServiceTest {

    private static final String AUTH_TOKEN = "Bearer";
    private static final long CASE_REFERENCE = 1234567890123456L;
    private static final String CASE_REFERENCE_STRING = String.valueOf(CASE_REFERENCE);
    private static final String PROCESS_INSTANCE_ID = "process-id";
    private static final String DEFENDANT_QUERY_ID = "def-query-id";
    private static final String CLAIMANT_QUERY_ID = "clm-query-id";

    @Mock
    private DashboardScenariosService dashboardScenariosService;
    @Mock
    private DashboardNotificationsParamsMapper mapper;
    @Mock
    private CoreCaseUserService coreCaseUserService;
    @Mock
    private QueryManagementCamundaService runtimeService;

    @InjectMocks
    private RaiseQueryDashboardService service;

    @BeforeEach
    void setUp() {
        lenient().when(mapper.mapCaseDataToParams(any())).thenReturn(new HashMap<>());
    }

    @Test
    void shouldNotifyClaimantWhenDefendantRaisesQuery() {
        String createdBy = "defendant-user";
        mockProcessVariables(DEFENDANT_QUERY_ID);
        CaseData caseData = lipCaseData(queries(caseMessage(DEFENDANT_QUERY_ID, createdBy)));
        when(coreCaseUserService.getUserCaseRoles(CASE_REFERENCE_STRING, createdBy))
            .thenReturn(List.of(CaseRole.DEFENDANT.getFormattedName()));

        service.notifyRaiseQuery(caseData, AUTH_TOKEN);

        verify(dashboardScenariosService).recordScenarios(
            eq(AUTH_TOKEN),
            eq(SCENARIO_AAA6_QUERY_RAISED_BY_OTHER_PARTY_CLAIMANT.getScenario()),
            eq(CASE_REFERENCE_STRING),
            any(ScenarioRequestParams.class)
        );
        verify(dashboardScenariosService).recordScenarios(
            eq(AUTH_TOKEN),
            eq(SCENARIO_AAA6_VIEW_AVAILABLE_MESSAGES_TO_THE_COURT.getScenario()),
            eq(CASE_REFERENCE_STRING),
            any(ScenarioRequestParams.class)
        );
        verifyDeletionScenariosRecorded();
        verify(dashboardScenariosService, never()).recordScenarios(
            eq(AUTH_TOKEN),
            eq(SCENARIO_AAA6_QUERY_RAISED_BY_OTHER_PARTY_DEFENDANT.getScenario()),
            anyString(),
            any(ScenarioRequestParams.class)
        );
    }

    @Test
    void shouldNotifyDefendantWhenClaimantRaisesQueryAndNotFirstQuery() {
        String createdBy = "claimant-user";
        mockProcessVariables(CLAIMANT_QUERY_ID);
        CaseData caseData = lipCaseData(queries(
            caseMessage(CLAIMANT_QUERY_ID, createdBy),
            caseMessage("another-query", "another-user")
        ));
        when(coreCaseUserService.getUserCaseRoles(CASE_REFERENCE_STRING, createdBy))
            .thenReturn(List.of(CaseRole.CLAIMANT.getFormattedName()));

        service.notifyRaiseQuery(caseData, AUTH_TOKEN);

        verify(dashboardScenariosService).recordScenarios(
            eq(AUTH_TOKEN),
            eq(SCENARIO_AAA6_QUERY_RAISED_BY_OTHER_PARTY_DEFENDANT.getScenario()),
            eq(CASE_REFERENCE_STRING),
            any(ScenarioRequestParams.class)
        );
        verifyDeletionScenariosRecorded();
        verify(dashboardScenariosService, never()).recordScenarios(
            eq(AUTH_TOKEN),
            eq(SCENARIO_AAA6_VIEW_AVAILABLE_MESSAGES_TO_THE_COURT.getScenario()),
            anyString(),
            any(ScenarioRequestParams.class)
        );
    }

    @Test
    void shouldNotNotifyWhenCaseIsNotLip() {
        CaseData caseData = CaseDataBuilder.builder()
            .ccdCaseReference(CASE_REFERENCE)
            .businessProcess(BusinessProcess.builder().processInstanceId(PROCESS_INSTANCE_ID).build())
            .applicant1Represented(YesOrNo.YES)
            .respondent1Represented(YesOrNo.YES)
            .queries(queries(caseMessage(DEFENDANT_QUERY_ID, "any-user")))
            .build();

        service.notifyRaiseQuery(caseData, AUTH_TOKEN);

        verifyNoInteractions(dashboardScenariosService);
        verifyNoInteractions(coreCaseUserService);
        verifyNoInteractions(runtimeService);
    }

    private void verifyDeletionScenariosRecorded() {
        verify(dashboardScenariosService).recordScenarios(
            eq(AUTH_TOKEN),
            eq(SCENARIO_AAA6_QUERY_RAISED_BY_OTHER_PARTY_CLAIMANT_DELETE.getScenario()),
            eq(CASE_REFERENCE_STRING),
            any(ScenarioRequestParams.class)
        );
        verify(dashboardScenariosService).recordScenarios(
            eq(AUTH_TOKEN),
            eq(SCENARIO_AAA6_QUERY_RAISED_BY_OTHER_PARTY_DEFENDANT_DELETE.getScenario()),
            eq(CASE_REFERENCE_STRING),
            any(ScenarioRequestParams.class)
        );
    }

    private void mockProcessVariables(String queryId) {
        when(runtimeService.getProcessVariables(PROCESS_INSTANCE_ID))
            .thenReturn(QueryManagementVariables.builder().queryId(queryId).build());
    }

    private CaseData lipCaseData(CaseQueriesCollection queries) {
        return CaseDataBuilder.builder()
            .ccdCaseReference(CASE_REFERENCE)
            .businessProcess(BusinessProcess.builder().processInstanceId(PROCESS_INSTANCE_ID).build())
            .applicant1Represented(YesOrNo.NO)
            .respondent1Represented(YesOrNo.NO)
            .queries(queries)
            .build();
    }

    private CaseMessage caseMessage(String queryId, String createdBy) {
        return CaseMessage.builder()
            .id(queryId)
            .createdBy(createdBy)
            .build();
    }

    private CaseQueriesCollection queries(CaseMessage... caseMessages) {
        return CaseQueriesCollection.builder()
            .caseMessages(Stream.of(caseMessages).map(message -> element(message)).toList())
            .build();
    }
}
