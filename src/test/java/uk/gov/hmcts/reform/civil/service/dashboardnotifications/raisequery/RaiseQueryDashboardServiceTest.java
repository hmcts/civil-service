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
import uk.gov.hmcts.reform.civil.model.common.Element;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
    private static final String PROCESS_INSTANCE_ID = "process-id";
    private static final String DEFENDANT_QUERY_ID = "def-query";
    private static final String CLAIMANT_QUERY_ID = "clm-query";

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
        when(mapper.mapCaseDataToParams(any())).thenReturn(new HashMap<>());
    }

    @Test
    void shouldNotifyClaimantWhenDefendantRaisesQueryAndFirstQuery() {
        CaseData caseData = lipCaseData(singleMessageQuery(DEFENDANT_QUERY_ID, "defendant"));
        when(runtimeService.getProcessVariables(PROCESS_INSTANCE_ID))
            .thenReturn(QueryManagementVariables.builder().queryId(DEFENDANT_QUERY_ID).build());
        when(coreCaseUserService.getUserCaseRoles(String.valueOf(CASE_REFERENCE), "defendant"))
            .thenReturn(List.of(CaseRole.DEFENDANT.getFormattedName()));

        service.notifyRaiseQuery(caseData, AUTH_TOKEN);

        verifyDeletionScenarios();
        verify(dashboardScenariosService).recordScenarios(
            eq(AUTH_TOKEN),
            eq(SCENARIO_AAA6_QUERY_RAISED_BY_OTHER_PARTY_CLAIMANT.getScenario()),
            eq(String.valueOf(CASE_REFERENCE)),
            any(ScenarioRequestParams.class)
        );
        verify(dashboardScenariosService).recordScenarios(
            eq(AUTH_TOKEN),
            eq(SCENARIO_AAA6_VIEW_AVAILABLE_MESSAGES_TO_THE_COURT.getScenario()),
            eq(String.valueOf(CASE_REFERENCE)),
            any(ScenarioRequestParams.class)
        );
        verify(dashboardScenariosService, never()).recordScenarios(
            eq(AUTH_TOKEN),
            eq(SCENARIO_AAA6_QUERY_RAISED_BY_OTHER_PARTY_DEFENDANT.getScenario()),
            any(),
            any(ScenarioRequestParams.class)
        );
    }

    @Test
    void shouldNotifyDefendantWhenClaimantRaisesQueryWithoutFirstQueryScenario() {
        CaseData caseData = lipCaseData(multiMessageQuery());
        when(runtimeService.getProcessVariables(PROCESS_INSTANCE_ID))
            .thenReturn(QueryManagementVariables.builder().queryId(CLAIMANT_QUERY_ID).build());
        when(coreCaseUserService.getUserCaseRoles(String.valueOf(CASE_REFERENCE), "claimant"))
            .thenReturn(List.of(CaseRole.CLAIMANT.getFormattedName()));

        service.notifyRaiseQuery(caseData, AUTH_TOKEN);

        verifyDeletionScenarios();
        verify(dashboardScenariosService).recordScenarios(
            eq(AUTH_TOKEN),
            eq(SCENARIO_AAA6_QUERY_RAISED_BY_OTHER_PARTY_DEFENDANT.getScenario()),
            eq(String.valueOf(CASE_REFERENCE)),
            any(ScenarioRequestParams.class)
        );
        verify(dashboardScenariosService, never()).recordScenarios(
            eq(AUTH_TOKEN),
            eq(SCENARIO_AAA6_VIEW_AVAILABLE_MESSAGES_TO_THE_COURT.getScenario()),
            any(),
            any(ScenarioRequestParams.class)
        );
    }

    @Test
    void shouldNotRecordWhenCaseIsNotLip() {
        CaseData caseData = CaseDataBuilder.builder()
            .ccdCaseReference(CASE_REFERENCE)
            .businessProcess(BusinessProcess.builder().processInstanceId(PROCESS_INSTANCE_ID).build())
            .applicant1Represented(YesOrNo.YES)
            .respondent1Represented(YesOrNo.YES)
            .queries(singleMessageQuery(DEFENDANT_QUERY_ID, "defendant"))
            .build();

        when(runtimeService.getProcessVariables(PROCESS_INSTANCE_ID))
            .thenReturn(QueryManagementVariables.builder().queryId(DEFENDANT_QUERY_ID).build());
        when(coreCaseUserService.getUserCaseRoles(String.valueOf(CASE_REFERENCE), "defendant"))
            .thenReturn(List.of(CaseRole.DEFENDANT.getFormattedName()));

        service.notifyRaiseQuery(caseData, AUTH_TOKEN);

        verifyNoInteractions(dashboardScenariosService);
    }

    @Test
    void shouldDeleteDuplicatesWhenNoPartyScenarioIsRecorded() {
        CaseData caseData = lipCaseData(singleMessageQuery(DEFENDANT_QUERY_ID, "defendant"));
        when(runtimeService.getProcessVariables(PROCESS_INSTANCE_ID))
            .thenReturn(QueryManagementVariables.builder().queryId(DEFENDANT_QUERY_ID).build());
        when(coreCaseUserService.getUserCaseRoles(String.valueOf(CASE_REFERENCE), "defendant"))
            .thenReturn(List.of());

        service.notifyRaiseQuery(caseData, AUTH_TOKEN);

        verifyDeletionScenarios();
    }

    @Test
    void shouldNotifyBothPartiesWhenQueryCreatorHasNoLipRole() {
        CaseData caseData = lipCaseData(singleMessageQuery(DEFENDANT_QUERY_ID, "defendant"));
        when(runtimeService.getProcessVariables(PROCESS_INSTANCE_ID))
            .thenReturn(QueryManagementVariables.builder().queryId(DEFENDANT_QUERY_ID).build());
        when(coreCaseUserService.getUserCaseRoles(String.valueOf(CASE_REFERENCE), "defendant"))
            .thenReturn(List.of());

        service.notifyRaiseQuery(caseData, AUTH_TOKEN);

        verifyDeletionScenarios();
        verify(dashboardScenariosService).recordScenarios(
            eq(AUTH_TOKEN),
            eq(SCENARIO_AAA6_QUERY_RAISED_BY_OTHER_PARTY_CLAIMANT.getScenario()),
            eq(String.valueOf(CASE_REFERENCE)),
            any(ScenarioRequestParams.class)
        );
        verify(dashboardScenariosService).recordScenarios(
            eq(AUTH_TOKEN),
            eq(SCENARIO_AAA6_QUERY_RAISED_BY_OTHER_PARTY_DEFENDANT.getScenario()),
            eq(String.valueOf(CASE_REFERENCE)),
            any(ScenarioRequestParams.class)
        );
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

    private CaseQueriesCollection singleMessageQuery(String queryId, String createdBy) {
        CaseMessage caseMessage = new CaseMessage();
        caseMessage.setId(queryId);
        caseMessage.setCreatedBy(createdBy);

        CaseQueriesCollection caseQueriesCollection = new CaseQueriesCollection();
        caseQueriesCollection.setCaseMessages(List.of(element(caseMessage)));
        return caseQueriesCollection;
    }

    private CaseQueriesCollection multiMessageQuery() {
        CaseMessage firstMessage = new CaseMessage();
        firstMessage.setId(CLAIMANT_QUERY_ID);
        firstMessage.setCreatedBy("claimant");
        Element<CaseMessage> first = element(firstMessage);

        CaseMessage secondMessage = new CaseMessage();
        secondMessage.setId("follow-up");
        secondMessage.setCreatedBy("claimant");
        Element<CaseMessage> second = element(secondMessage);

        CaseQueriesCollection caseQueriesCollection = new CaseQueriesCollection();
        caseQueriesCollection.setCaseMessages(List.of(first, second));
        return caseQueriesCollection;
    }

    private void verifyDeletionScenarios() {
        verify(dashboardScenariosService).recordScenarios(
            eq(AUTH_TOKEN),
            eq(SCENARIO_AAA6_QUERY_RAISED_BY_OTHER_PARTY_CLAIMANT_DELETE.getScenario()),
            eq(String.valueOf(CASE_REFERENCE)),
            any(ScenarioRequestParams.class)
        );
        verify(dashboardScenariosService).recordScenarios(
            eq(AUTH_TOKEN),
            eq(SCENARIO_AAA6_QUERY_RAISED_BY_OTHER_PARTY_DEFENDANT_DELETE.getScenario()),
            eq(String.valueOf(CASE_REFERENCE)),
            any(ScenarioRequestParams.class)
        );
    }
}
