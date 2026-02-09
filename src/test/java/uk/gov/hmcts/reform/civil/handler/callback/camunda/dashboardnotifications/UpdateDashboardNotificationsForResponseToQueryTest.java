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
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;

import java.util.HashMap;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.UPDATE_DASHBOARD_NOTIFICATIONS_RESPONSE_TO_QUERY;
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
    private DashboardNotificationsParamsMapper dashboardNotificationsParamsMapper;

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
        CaseMessage claimantQuery = new CaseMessage();
        claimantQuery.setId("123457");
        claimantQuery.setCreatedBy("claimant");
        CaseMessage claimantResponse = new CaseMessage();
        claimantResponse.setId("queryId");
        claimantResponse.setCreatedBy("claimant");
        claimantResponse.setParentId("123457");
        CaseQueriesCollection claimantQueries = new CaseQueriesCollection();
        claimantQueries.setCaseMessages(wrapElements(List.of(claimantQuery, claimantResponse)));
        CaseData caseData = CaseData.builder()
            .caseDataLiP(
                new CaseDataLiP().setRespondentSignSettlementAgreement(YesOrNo.NO
                )
            )
            .applicant1Represented(YesOrNo.NO)
            .respondent1Represented(YesOrNo.YES)
            .qmLatestQuery(createLatestQuery("queryId"))
            .queries(claimantQueries)
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
        verifyNoMoreInteractions(dashboardScenariosService);
    }

    @Test
    void shouldCreateNotificationForDefendantLip() {

        HashMap<String, Object> params = new HashMap<>();

        when(dashboardNotificationsParamsMapper.mapCaseDataToParams(any())).thenReturn(params);
        CaseMessage defendantQuery = new CaseMessage();
        defendantQuery.setId("123457");
        defendantQuery.setCreatedBy("defendant");
        CaseMessage defendantResponse = new CaseMessage();
        defendantResponse.setId("queryId");
        defendantResponse.setCreatedBy("defendant");
        defendantResponse.setParentId("123457");
        CaseQueriesCollection defendantQueries = new CaseQueriesCollection();
        defendantQueries.setCaseMessages(wrapElements(List.of(defendantQuery, defendantResponse)));
        CaseData caseData = CaseData.builder()
            .qmLatestQuery(createLatestQuery("queryId"))
            .queries(defendantQueries)
            .legacyCaseReference("reference")
            .respondent1Represented(YesOrNo.NO)
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
        verifyNoMoreInteractions(dashboardScenariosService);
    }

    private LatestQuery createLatestQuery(String queryId) {
        LatestQuery latestQuery = new LatestQuery();
        latestQuery.setQueryId(queryId);
        return latestQuery;
    }
}
