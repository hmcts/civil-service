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
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_DASHBOARD_NOTIFICATION_FOR_SETTLEMENT_DEFENDANT_RESPONSE;
import static uk.gov.hmcts.reform.civil.enums.CaseRole.CLAIMANT;
import static uk.gov.hmcts.reform.civil.enums.CaseRole.DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_SETTLEMENT_AGREEMENT_DEFENDANT_REJECTED_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_VIEW_MESSAGES_AVAILABLE_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_VIEW_MESSAGES_AVAILABLE_DEFENDANT;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.wrapElements;

@ExtendWith(MockitoExtension.class)
public class UpdateDashboardNotificationsForRaisedQueryTest extends BaseCallbackHandlerTest {

    @InjectMocks
    private UpdateDashboardNotificationsForRaisedQuery handler;

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
        when(runtimeService.getProcessVariables(any())).thenReturn(QueryManagementVariables.builder().queryId(null)
                                                                       .build());
        CaseQueriesCollection applicantCitizenQuery = CaseQueriesCollection.builder()
            .caseMessages(wrapElements(CaseMessage.builder()
                                           .id("123456")
                                           .build()))
            .build();
        CaseData caseData = CaseData.builder()
            .caseDataLiP(
                CaseDataLiP.builder().respondentSignSettlementAgreement(YesOrNo.NO
                ).build()
            )
            .qmLatestQuery(LatestQuery.builder().queryId("123456").build())
            .qmApplicantCitizenQueries(applicantCitizenQuery)
            .legacyCaseReference("reference")
            .businessProcess(BusinessProcess.builder().processInstanceId("1234").build())
            .ccdCaseReference(1234L)
            .build();

        CallbackParams callbackParams = CallbackParamsBuilder.builder()
            .of(ABOUT_TO_SUBMIT, caseData)
            .build();

        handler.handle(callbackParams);

        verify(dashboardScenariosService).recordScenarios(
            "BEARER_TOKEN",
            SCENARIO_AAA6_VIEW_MESSAGES_AVAILABLE_CLAIMANT.getScenario(),
            caseData.getCcdCaseReference().toString(),
            ScenarioRequestParams.builder().params(params).build()
        );
    }

    @Test
    void configureViewAllMessagesTaskItemHasActiveForDefendant() {

        HashMap<String, Object> params = new HashMap<>();

        when(dashboardNotificationsParamsMapper.mapCaseDataToParams(any())).thenReturn(params);
        when(coreCaseUserService.getUserCaseRoles(any(), any())).thenReturn(List.of(DEFENDANT.getFormattedName()));
        when(runtimeService.getProcessVariables(any())).thenReturn(QueryManagementVariables.builder().queryId(null)
                                                                       .build());
        CaseQueriesCollection defendantCitizenQuery = CaseQueriesCollection.builder()
            .caseMessages(wrapElements(CaseMessage.builder()
                                           .id("123457")
                                           .build()))
            .build();
        CaseData caseData = CaseData.builder()
            .caseDataLiP(
                CaseDataLiP.builder().respondentSignSettlementAgreement(YesOrNo.NO
                ).build()
            )
            .qmLatestQuery(LatestQuery.builder().queryId("123457").build())
            .qmRespondentCitizenQueries(defendantCitizenQuery)
            .legacyCaseReference("reference")
            .businessProcess(BusinessProcess.builder().processInstanceId("1234").build())
            .ccdCaseReference(1234L)
            .build();

        CallbackParams callbackParams = CallbackParamsBuilder.builder()
            .of(ABOUT_TO_SUBMIT, caseData)
            .build();

        handler.handle(callbackParams);

        verify(dashboardScenariosService).recordScenarios(
            "BEARER_TOKEN",
            SCENARIO_AAA6_VIEW_MESSAGES_AVAILABLE_DEFENDANT.getScenario(),
            caseData.getCcdCaseReference().toString(),
            ScenarioRequestParams.builder().params(params).build()
        );
    }

    @Test
    void shouldNotActiveAgianIfAlreadyTaskItemIsActivatedForDefendant() {

        HashMap<String, Object> params = new HashMap<>();

        when(dashboardNotificationsParamsMapper.mapCaseDataToParams(any())).thenReturn(params);
        when(coreCaseUserService.getUserCaseRoles(any(), any())).thenReturn(List.of(DEFENDANT.getFormattedName()));
        when(runtimeService.getProcessVariables(any())).thenReturn(QueryManagementVariables.builder().queryId(null)
                                                                       .build());
        CaseQueriesCollection defendantCitizenQuery = CaseQueriesCollection.builder()
            .caseMessages(wrapElements(List.of(CaseMessage.builder()
                                                   .id("123457")
                                                   .build(), CaseMessage.builder()
                                                   .id("123458")
                                                   .build())))
            .build();
        CaseData caseData = CaseData.builder()
            .caseDataLiP(
                CaseDataLiP.builder().respondentSignSettlementAgreement(YesOrNo.NO
                ).build()
            )
            .qmLatestQuery(LatestQuery.builder().queryId("123457").build())
            .qmRespondentCitizenQueries(defendantCitizenQuery)
            .legacyCaseReference("reference")
            .businessProcess(BusinessProcess.builder().processInstanceId("1234").build())
            .ccdCaseReference(1234L)
            .build();

        CallbackParams callbackParams = CallbackParamsBuilder.builder()
            .of(ABOUT_TO_SUBMIT, caseData)
            .build();

        handler.handle(callbackParams);

        verify(dashboardScenariosService, times(0)).recordScenarios(
            "BEARER_TOKEN",
            SCENARIO_AAA6_VIEW_MESSAGES_AVAILABLE_DEFENDANT.getScenario(),
            caseData.getCcdCaseReference().toString(),
            ScenarioRequestParams.builder().params(params).build()
        );
    }

    @Test
    void shouldNotActiveAgianIfAlreadyTaskItemIsActivatedForClaimant() {

        HashMap<String, Object> params = new HashMap<>();

        when(dashboardNotificationsParamsMapper.mapCaseDataToParams(any())).thenReturn(params);
        when(coreCaseUserService.getUserCaseRoles(any(), any())).thenReturn(List.of(CLAIMANT.getFormattedName()));
        when(runtimeService.getProcessVariables(any())).thenReturn(QueryManagementVariables.builder().queryId(null)
                                                                       .build());
        CaseQueriesCollection applicantCitizenQuery = CaseQueriesCollection.builder()
            .caseMessages(wrapElements(List.of(CaseMessage.builder()
                                                   .id("123457")
                                                   .build(), CaseMessage.builder()
                                                   .id("123458")
                                                   .build())))
            .build();
        CaseData caseData = CaseData.builder()
            .caseDataLiP(
                CaseDataLiP.builder().respondentSignSettlementAgreement(YesOrNo.NO
                ).build()
            )
            .qmLatestQuery(LatestQuery.builder().queryId("123457").build())
            .qmApplicantCitizenQueries(applicantCitizenQuery)
            .legacyCaseReference("reference")
            .businessProcess(BusinessProcess.builder().processInstanceId("1234").build())
            .ccdCaseReference(1234L)
            .build();

        CallbackParams callbackParams = CallbackParamsBuilder.builder()
            .of(ABOUT_TO_SUBMIT, caseData)
            .build();

        handler.handle(callbackParams);

        verify(dashboardScenariosService, times(0)).recordScenarios(
            "BEARER_TOKEN",
            SCENARIO_AAA6_VIEW_MESSAGES_AVAILABLE_CLAIMANT.getScenario(),
            caseData.getCcdCaseReference().toString(),
            ScenarioRequestParams.builder().params(params).build()
        );
    }
}
