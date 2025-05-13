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
import uk.gov.hmcts.reform.civil.service.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.querymanagement.QueryManagementCamundaService;
import uk.gov.hmcts.reform.civil.service.querymanagement.QueryManagementVariables;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;

import java.util.HashMap;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.UPDATE_DASHBOARD_NOTIFICATIONS_RESPONSE_TO_QUERY;
import static uk.gov.hmcts.reform.civil.enums.CaseRole.CLAIMANT;
import static uk.gov.hmcts.reform.civil.enums.CaseRole.DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_QUERY_RESPONDED_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_QUERY_RESPONDED_CLAIMANT_DELETE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_QUERY_RESPONDED_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_QUERY_RESPONDED_DEFENDANT_DELETE;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.wrapElements;

@ExtendWith(MockitoExtension.class)
public class UpdateDashboardNotificationsForResponseToQueryTest extends BaseCallbackHandlerTest {

    @InjectMocks
    private UpdateDashboardNotificationsForResponseToQuery handler;

    @Mock
    private DashboardScenariosService dashboardScenariosService;

    @Mock
    private FeatureToggleService featureToggleService;

    @Mock
    private DashboardNotificationsParamsMapper dashboardNotificationsParamsMapper;
    @Mock
    private CoreCaseUserService coreCaseUserService;
    @Mock
    private QueryManagementCamundaService runtimeService;
    public static final String TASK_ID = "UpdateDashboardNotificationsResponseToQuery";

    @Test
    void shouldReturnCorrectCamundaActivityId_whenInvoked() {
        assertThat(handler.camundaActivityId(
            CallbackParamsBuilder.builder()
                .request(CallbackRequest.builder()
                             .eventId(UPDATE_DASHBOARD_NOTIFICATIONS_RESPONSE_TO_QUERY.name())
                             .build())
                .build()))
            .isEqualTo(TASK_ID);
    }

    @Test
    void shouldCreateNotificationForClaimantLip() {

        HashMap<String, Object> params = new HashMap<>();

        when(dashboardNotificationsParamsMapper.mapCaseDataToParams(any())).thenReturn(params);
        when(coreCaseUserService.getUserCaseRoles(any(), any())).thenReturn(List.of(CLAIMANT.getFormattedName()));
        when(runtimeService.getProcessVariables(any())).thenReturn(QueryManagementVariables.builder().queryId("queryId")
                                                                       .build());
        CaseQueriesCollection claimantQueries = CaseQueriesCollection.builder()
            .caseMessages(wrapElements(List.of(CaseMessage.builder()
                                                   .id("123457")
                                                   .build(),
                    CaseMessage.builder()
                            .id("queryId")
                            .parentId("123457")
                            .build())))
            .build();
        CaseData caseData = CaseData.builder()
            .caseDataLiP(
                CaseDataLiP.builder().respondentSignSettlementAgreement(YesOrNo.NO
                ).build()
            )
            .qmLatestQuery(LatestQuery.builder().queryId("queryId").build())
            .qmApplicantCitizenQueries(claimantQueries)
            .legacyCaseReference("reference")
            .businessProcess(BusinessProcess.builder().processInstanceId("1234").build())
            .ccdCaseReference(1234L)
            .build();

        CallbackParams callbackParams = CallbackParamsBuilder.builder()
            .of(ABOUT_TO_SUBMIT, caseData)
            .build();

        handler.handle(callbackParams);

        verify(dashboardScenariosService, times(1)).recordScenarios(
            "BEARER_TOKEN",
                SCENARIO_AAA6_QUERY_RESPONDED_CLAIMANT_DELETE.getScenario(),
                caseData.getCcdCaseReference().toString(),
                ScenarioRequestParams.builder().params(params).build()
        );
        verify(dashboardScenariosService, times(1)).recordScenarios(
                "BEARER_TOKEN",
                SCENARIO_AAA6_QUERY_RESPONDED_CLAIMANT.getScenario(),
                caseData.getCcdCaseReference().toString(),
                ScenarioRequestParams.builder().params(params).build()
        );
    }

    @Test
    void shouldCreateNotificationForDefendantLip() {

        HashMap<String, Object> params = new HashMap<>();

        when(dashboardNotificationsParamsMapper.mapCaseDataToParams(any())).thenReturn(params);
        when(coreCaseUserService.getUserCaseRoles(any(), any())).thenReturn(List.of(DEFENDANT.getFormattedName()));
        when(runtimeService.getProcessVariables(any())).thenReturn(QueryManagementVariables.builder().queryId("queryId")
                .build());
        CaseQueriesCollection defendantQueries = CaseQueriesCollection.builder()
                .caseMessages(wrapElements(List.of(CaseMessage.builder()
                                .id("123457")
                                .build(),
                        CaseMessage.builder()
                                .id("queryId")
                                .parentId("123457")
                                .build())))
                .build();
        CaseData caseData = CaseData.builder()
                .qmLatestQuery(LatestQuery.builder().queryId("queryId").build())
                .qmRespondentCitizenQueries(defendantQueries)
                .legacyCaseReference("reference")
                .businessProcess(BusinessProcess.builder().processInstanceId("1234").build())
                .ccdCaseReference(1234L)
                .build();

        CallbackParams callbackParams = CallbackParamsBuilder.builder()
                .of(ABOUT_TO_SUBMIT, caseData)
                .build();

        handler.handle(callbackParams);

        verify(dashboardScenariosService, times(1)).recordScenarios(
                "BEARER_TOKEN",
                SCENARIO_AAA6_QUERY_RESPONDED_DEFENDANT_DELETE.getScenario(),
                caseData.getCcdCaseReference().toString(),
                ScenarioRequestParams.builder().params(params).build()
        );
        verify(dashboardScenariosService, times(1)).recordScenarios(
                "BEARER_TOKEN",
                SCENARIO_AAA6_QUERY_RESPONDED_DEFENDANT.getScenario(),
                caseData.getCcdCaseReference().toString(),
                ScenarioRequestParams.builder().params(params).build()
        );
    }
}
