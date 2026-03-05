package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.querymanagement.CaseMessage;
import uk.gov.hmcts.reform.civil.model.querymanagement.CaseQueriesCollection;
import uk.gov.hmcts.reform.civil.model.querymanagement.LatestQuery;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.service.CoreCaseUserService;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.service.querymanagement.QueryManagementCamundaService;
import uk.gov.hmcts.reform.civil.service.querymanagement.QueryManagementVariables;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;

import java.util.HashMap;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_DASHBOARD_NOTIFICATION_FOR_SETTLEMENT_DEFENDANT_RESPONSE;
import static uk.gov.hmcts.reform.civil.enums.CaseRole.CLAIMANT;
import static uk.gov.hmcts.reform.civil.enums.CaseRole.DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_QUERY_RAISED_BY_OTHER_PARTY_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_QUERY_RAISED_BY_OTHER_PARTY_CLAIMANT_DELETE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_QUERY_RAISED_BY_OTHER_PARTY_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_QUERY_RAISED_BY_OTHER_PARTY_DEFENDANT_DELETE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_VIEW_AVAILABLE_MESSAGES_TO_THE_COURT;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.wrapElements;

@ExtendWith(MockitoExtension.class)
public class UpdateDashboardNotificationsForRaisedQueryTest extends BaseCallbackHandlerTest {

    @InjectMocks
    private UpdateDashboardNotificationsForRaisedQuery handler;

    @Mock
    private DashboardScenariosService dashboardScenariosService;

    @Mock
    private CoreCaseUserService coreCaseUserService;

    @Mock
    private QueryManagementCamundaService runtimeService;

    @Mock
    private DashboardNotificationsParamsMapper dashboardNotificationsParamsMapper;

    public static final String TASK_ID = "UpdateDashboardNotificationsRaisedQm";

    @Test
    void shouldReturnCorrectCamundaActivityId_whenInvoked() {
        assertThat(handler.camundaActivityId(
            CallbackParamsBuilder.builder()
                .request(CallbackRequest.builder()
                    .eventId(CREATE_DASHBOARD_NOTIFICATION_FOR_SETTLEMENT_DEFENDANT_RESPONSE.name())
                    .build())
                .build()))
            .isEqualTo(TASK_ID);
    }

    @Test
    void configureViewAllMessagesTaskItemHasActive() {

        HashMap<String, Object> params = new HashMap<>();

        when(dashboardNotificationsParamsMapper.mapCaseDataToParams(any())).thenReturn(params);
        when(coreCaseUserService.getUserCaseRoles(any(), any())).thenReturn(List.of(CLAIMANT.getFormattedName()));
        when(runtimeService.getProcessVariables(any())).thenReturn(new QueryManagementVariables().setQueryId("123456"));
        CaseMessage applicantMessage = new CaseMessage();
        applicantMessage.setId("123456");
        applicantMessage.setCreatedBy("applicant");
        CaseQueriesCollection applicantCitizenQuery = new CaseQueriesCollection();
        applicantCitizenQuery.setCaseMessages(wrapElements(applicantMessage));
        CaseData caseData = CaseData.builder()
            .caseDataLiP(
                new CaseDataLiP().setRespondentSignSettlementAgreement(YesOrNo.NO
                )
            )
            .applicant1Represented(YesOrNo.NO)
            .qmLatestQuery(createLatestQuery("123456"))
            .queries(applicantCitizenQuery)
            .legacyCaseReference("reference")
            .businessProcess(new BusinessProcess().setProcessInstanceId("1234"))
            .ccdCaseReference(1234L)
            .build();

        CallbackParams callbackParams = CallbackParamsBuilder.builder()
            .of(ABOUT_TO_SUBMIT, caseData)
            .build();

        handler.handle(callbackParams);

        verify(dashboardScenariosService).recordScenarios(
            "BEARER_TOKEN",
            SCENARIO_AAA6_QUERY_RAISED_BY_OTHER_PARTY_DEFENDANT_DELETE.getScenario(),
            caseData.getCcdCaseReference().toString(),
            ScenarioRequestParams.builder().params(params).build()
        );
        verify(dashboardScenariosService).recordScenarios(
            "BEARER_TOKEN",
            SCENARIO_AAA6_QUERY_RAISED_BY_OTHER_PARTY_CLAIMANT_DELETE.getScenario(),
            caseData.getCcdCaseReference().toString(),
            ScenarioRequestParams.builder().params(params).build()
        );
        verify(dashboardScenariosService).recordScenarios(
            "BEARER_TOKEN",
            SCENARIO_AAA6_QUERY_RAISED_BY_OTHER_PARTY_DEFENDANT.getScenario(),
            caseData.getCcdCaseReference().toString(),
            ScenarioRequestParams.builder().params(params).build()
        );
        verify(dashboardScenariosService).recordScenarios(
            "BEARER_TOKEN",
            SCENARIO_AAA6_VIEW_AVAILABLE_MESSAGES_TO_THE_COURT.getScenario(),
            caseData.getCcdCaseReference().toString(),
            ScenarioRequestParams.builder().params(params).build()
        );
    }

    @Test
    void configureViewAllMessagesTaskItemHasActiveForDefendant() {

        HashMap<String, Object> params = new HashMap<>();

        when(dashboardNotificationsParamsMapper.mapCaseDataToParams(any())).thenReturn(params);
        when(coreCaseUserService.getUserCaseRoles(any(), any())).thenReturn(List.of(DEFENDANT.getFormattedName()));
        when(runtimeService.getProcessVariables(any())).thenReturn(new QueryManagementVariables().setQueryId("123457"));
        CaseMessage defendantMessage = new CaseMessage();
        defendantMessage.setId("123457");
        defendantMessage.setCreatedBy("defendant");
        CaseQueriesCollection defendantCitizenQuery = new CaseQueriesCollection();
        defendantCitizenQuery.setCaseMessages(wrapElements(defendantMessage));
        CaseData caseData = CaseData.builder()
            .caseDataLiP(
                new CaseDataLiP().setRespondentSignSettlementAgreement(YesOrNo.NO
                )
            )
            .applicant1Represented(YesOrNo.NO)
            .qmLatestQuery(createLatestQuery("123457"))
            .queries(defendantCitizenQuery)
            .legacyCaseReference("reference")
            .businessProcess(new BusinessProcess().setProcessInstanceId("1234"))
            .ccdCaseReference(1234L)
            .build();

        CallbackParams callbackParams = CallbackParamsBuilder.builder()
            .of(ABOUT_TO_SUBMIT, caseData)
            .build();

        handler.handle(callbackParams);

        verify(dashboardScenariosService).recordScenarios(
            "BEARER_TOKEN",
            SCENARIO_AAA6_QUERY_RAISED_BY_OTHER_PARTY_DEFENDANT_DELETE.getScenario(),
            caseData.getCcdCaseReference().toString(),
            ScenarioRequestParams.builder().params(params).build()
        );
        verify(dashboardScenariosService).recordScenarios(
            "BEARER_TOKEN",
            SCENARIO_AAA6_QUERY_RAISED_BY_OTHER_PARTY_CLAIMANT_DELETE.getScenario(),
            caseData.getCcdCaseReference().toString(),
            ScenarioRequestParams.builder().params(params).build()
        );
        verify(dashboardScenariosService).recordScenarios(
            "BEARER_TOKEN",
            SCENARIO_AAA6_QUERY_RAISED_BY_OTHER_PARTY_CLAIMANT.getScenario(),
            caseData.getCcdCaseReference().toString(),
            ScenarioRequestParams.builder().params(params).build()
        );
        verify(dashboardScenariosService).recordScenarios(
            "BEARER_TOKEN",
            SCENARIO_AAA6_VIEW_AVAILABLE_MESSAGES_TO_THE_COURT.getScenario(),
            caseData.getCcdCaseReference().toString(),
            ScenarioRequestParams.builder().params(params).build()
        );
    }

    @Test
    void shouldNotActiveAgianIfAlreadyTaskItemIsActivatedForDefendant() {

        HashMap<String, Object> params = new HashMap<>();

        when(dashboardNotificationsParamsMapper.mapCaseDataToParams(any())).thenReturn(params);
        when(coreCaseUserService.getUserCaseRoles(any(), any())).thenReturn(List.of(DEFENDANT.getFormattedName()));
        when(runtimeService.getProcessVariables(any())).thenReturn(new QueryManagementVariables().setQueryId("123457"));
        CaseMessage defendantMessage = new CaseMessage();
        defendantMessage.setId("123457");
        defendantMessage.setCreatedBy("defendant");
        CaseMessage defendantMessage2 = new CaseMessage();
        defendantMessage2.setId("123458");
        defendantMessage2.setCreatedBy("defendant");
        CaseQueriesCollection defendantCitizenQuery = new CaseQueriesCollection();
        defendantCitizenQuery.setCaseMessages(wrapElements(List.of(defendantMessage, defendantMessage2)));
        CaseData caseData = CaseData.builder()
            .caseDataLiP(
                new CaseDataLiP().setRespondentSignSettlementAgreement(YesOrNo.NO
                )
            )
            .applicant1Represented(YesOrNo.NO)
            .qmLatestQuery(createLatestQuery("123457"))
            .queries(defendantCitizenQuery)
            .legacyCaseReference("reference")
            .businessProcess(new BusinessProcess().setProcessInstanceId("1234"))
            .ccdCaseReference(1234L)
            .build();

        CallbackParams callbackParams = CallbackParamsBuilder.builder()
            .of(ABOUT_TO_SUBMIT, caseData)
            .build();

        handler.handle(callbackParams);

        verify(dashboardScenariosService).recordScenarios(
            "BEARER_TOKEN",
            SCENARIO_AAA6_QUERY_RAISED_BY_OTHER_PARTY_DEFENDANT_DELETE.getScenario(),
            caseData.getCcdCaseReference().toString(),
            ScenarioRequestParams.builder().params(params).build()
        );
        verify(dashboardScenariosService).recordScenarios(
            "BEARER_TOKEN",
            SCENARIO_AAA6_QUERY_RAISED_BY_OTHER_PARTY_CLAIMANT_DELETE.getScenario(),
            caseData.getCcdCaseReference().toString(),
            ScenarioRequestParams.builder().params(params).build()
        );
        verify(dashboardScenariosService).recordScenarios(
            "BEARER_TOKEN",
            SCENARIO_AAA6_QUERY_RAISED_BY_OTHER_PARTY_CLAIMANT.getScenario(),
            caseData.getCcdCaseReference().toString(),
            ScenarioRequestParams.builder().params(params).build()
        );
        verifyNoMoreInteractions(dashboardScenariosService);
    }

    @Test
    void shouldNotActiveAgianIfAlreadyTaskItemIsActivatedForClaimant() {

        HashMap<String, Object> params = new HashMap<>();

        when(dashboardNotificationsParamsMapper.mapCaseDataToParams(any())).thenReturn(params);
        when(coreCaseUserService.getUserCaseRoles(any(), any())).thenReturn(List.of(CLAIMANT.getFormattedName()));
        when(runtimeService.getProcessVariables(any())).thenReturn(new QueryManagementVariables().setQueryId("123457"));
        CaseMessage claimantMessage = new CaseMessage();
        claimantMessage.setId("123457");
        claimantMessage.setCreatedBy("claimant");
        CaseMessage claimantMessage2 = new CaseMessage();
        claimantMessage2.setId("123458");
        claimantMessage2.setCreatedBy("claimant");
        CaseQueriesCollection applicantCitizenQuery = new CaseQueriesCollection();
        applicantCitizenQuery.setCaseMessages(wrapElements(List.of(claimantMessage, claimantMessage2)));
        CaseData caseData = CaseData.builder()
            .caseDataLiP(
                new CaseDataLiP().setRespondentSignSettlementAgreement(YesOrNo.NO
                )
            )
            .applicant1Represented(YesOrNo.NO)
            .qmLatestQuery(createLatestQuery("123457"))
            .queries(applicantCitizenQuery)
            .legacyCaseReference("reference")
            .businessProcess(new BusinessProcess().setProcessInstanceId("1234"))
            .ccdCaseReference(1234L)
            .build();

        CallbackParams callbackParams = CallbackParamsBuilder.builder()
            .of(ABOUT_TO_SUBMIT, caseData)
            .build();

        handler.handle(callbackParams);

        verify(dashboardScenariosService).recordScenarios(
            "BEARER_TOKEN",
            SCENARIO_AAA6_QUERY_RAISED_BY_OTHER_PARTY_DEFENDANT_DELETE.getScenario(),
            caseData.getCcdCaseReference().toString(),
            ScenarioRequestParams.builder().params(params).build()
        );
        verify(dashboardScenariosService).recordScenarios(
            "BEARER_TOKEN",
            SCENARIO_AAA6_QUERY_RAISED_BY_OTHER_PARTY_CLAIMANT_DELETE.getScenario(),
            caseData.getCcdCaseReference().toString(),
            ScenarioRequestParams.builder().params(params).build()
        );
        verify(dashboardScenariosService).recordScenarios(
            "BEARER_TOKEN",
            SCENARIO_AAA6_QUERY_RAISED_BY_OTHER_PARTY_DEFENDANT.getScenario(),
            caseData.getCcdCaseReference().toString(),
            ScenarioRequestParams.builder().params(params).build()
        );
        verifyNoMoreInteractions(dashboardScenariosService);
    }

    private CaseMessage createCaseMessage(String id) {
        CaseMessage caseMessage = new CaseMessage();
        caseMessage.setId(id);
        return caseMessage;
    }

    private LatestQuery createLatestQuery(String queryId) {
        LatestQuery latestQuery = new LatestQuery();
        latestQuery.setQueryId(queryId);
        return latestQuery;
    }
}
