package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.defendant;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.RespondToClaimAdmitPartLRspec;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.ClaimantLiPResponse;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.service.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_DASHBOARD_NOTIFICATION_FOR_CCJ_REQUEST_FOR_RESPONDENT1;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CCJ_CLAIMANT_ACCEPT_OR_REJECT_PLAN_SETTLEMENT_REQUESTED_DEF_PAYMENT_MISSED_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CCJ_CLAIMANT_ACCEPT_OR_REJECT_PLAN_SETTLEMENT_REQUESTED_NO_DEF_RESPONSE_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CLAIMANT_INTENT_CCJ_REQUESTED_DEFENDANT;

@ExtendWith(MockitoExtension.class)
class CCJRequestedDashboardNotificationHandlerTest extends BaseCallbackHandlerTest {

    @InjectMocks
    private CCJRequestedDashboardNotificationDefendantHandler handler;
    @Mock
    private FeatureToggleService featureToggleService;
    @Mock
    private DashboardScenariosService dashboardScenariosService;

    @Mock
    private DashboardNotificationsParamsMapper dashboardNotificationsParamsMapper;

    public static final String TASK_ID = "GenerateDashboardNotificationClaimantIntentCCJRequestedForRespondent1";

    HashMap<String, Object> params = new HashMap<>();

    @Test
    void handleEventsReturnsTheExpectedCallbackEvent() {
        assertThat(handler.handledEvents()).contains(CREATE_DASHBOARD_NOTIFICATION_FOR_CCJ_REQUEST_FOR_RESPONDENT1);
    }

    @Test
    void shouldReturnCorrectCamundaActivityId_whenInvoked() {
        assertThat(handler.camundaActivityId(
            CallbackParamsBuilder.builder()
                .request(CallbackRequest.builder()
                             .eventId(CREATE_DASHBOARD_NOTIFICATION_FOR_CCJ_REQUEST_FOR_RESPONDENT1.name())
                             .build())
                .build()))
            .isEqualTo(TASK_ID);
    }

    @Test
    void createDashboardNotificationsCcjRequested() {

        params.put("ccdCaseReference", "123");

        when(dashboardNotificationsParamsMapper.mapCaseDataToParams(any())).thenReturn(params);
        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);
        CaseData caseData = CaseData.builder()
            .legacyCaseReference("reference")
            .ccdCaseReference(1234L)
            .build();

        CallbackParams callbackParams = CallbackParamsBuilder.builder()
            .of(ABOUT_TO_SUBMIT, caseData)
            .build();

        handler.handle(callbackParams);
        verify(dashboardScenariosService).recordScenarios(
            "BEARER_TOKEN",
            SCENARIO_AAA6_CLAIMANT_INTENT_CCJ_REQUESTED_DEFENDANT.getScenario(),
            caseData.getCcdCaseReference().toString(),
            ScenarioRequestParams.builder().params(params).build()
        );
    }

    @Test
    void createDashboardNotificationsForBrokenSettlementAgreement() {

        params.put("ccdCaseReference", "12376868");

        when(dashboardNotificationsParamsMapper.mapCaseDataToParams(any())).thenReturn(params);
        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);
        CaseData caseData = CaseData.builder()
            .legacyCaseReference("reference")
            .ccdCaseReference(12376868L)
            .respondToClaimAdmitPartLRspec(RespondToClaimAdmitPartLRspec.builder()
                                               .whenWillThisAmountBePaid(LocalDate.now().minusDays(1)).build())
            .caseDataLiP(CaseDataLiP.builder().respondentSignSettlementAgreement(YesOrNo.YES)
                             .applicant1LiPResponse(ClaimantLiPResponse.builder()
                                                                         .applicant1SignedSettlementAgreement(
                                                                             YesOrNo.YES).build()).build())
            .respondent1RespondToSettlementAgreementDeadline(LocalDateTime.now().minusDays(1))
            .build();

        CallbackParams callbackParams = CallbackParamsBuilder.builder()
            .of(ABOUT_TO_SUBMIT, caseData)
            .build();

        handler.handle(callbackParams);
        verify(dashboardScenariosService).recordScenarios(
            "BEARER_TOKEN",
            SCENARIO_AAA6_CCJ_CLAIMANT_ACCEPT_OR_REJECT_PLAN_SETTLEMENT_REQUESTED_DEF_PAYMENT_MISSED_DEFENDANT.getScenario(),
            caseData.getCcdCaseReference().toString(),
            ScenarioRequestParams.builder().params(params).build()
        );
    }

    @Test
    void createDashboardNotificationsCcjRequestedDefendantRejectedSettlementAgreement() {

        params.put("ccdCaseReference", "123");

        when(dashboardNotificationsParamsMapper.mapCaseDataToParams(any())).thenReturn(params);
        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);
        CaseData caseData = CaseData.builder()
            .legacyCaseReference("reference")
            .ccdCaseReference(1234L)
            .caseDataLiP(CaseDataLiP.builder()
                             .applicant1LiPResponse(ClaimantLiPResponse.builder().applicant1SignedSettlementAgreement(YesOrNo.YES).build())
                             .respondentSignSettlementAgreement(YesOrNo.NO)
                             .build())
            .build();

        CallbackParams callbackParams = CallbackParamsBuilder.builder()
            .of(ABOUT_TO_SUBMIT, caseData)
            .build();

        handler.handle(callbackParams);
        verify(dashboardScenariosService).recordScenarios(
            "BEARER_TOKEN",
            SCENARIO_AAA6_CCJ_CLAIMANT_ACCEPT_OR_REJECT_PLAN_SETTLEMENT_REQUESTED_NO_DEF_RESPONSE_DEFENDANT.getScenario(),
            caseData.getCcdCaseReference().toString(),
            ScenarioRequestParams.builder().params(params).build()
        );
    }

    @Test
    void createDashboardNotificationsCcjRequestedDefendantNotRespondedToSettlementAgreement() {

        params.put("ccdCaseReference", "123");

        when(dashboardNotificationsParamsMapper.mapCaseDataToParams(any())).thenReturn(params);
        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);
        CaseData caseData = CaseData.builder()
            .legacyCaseReference("reference")
            .ccdCaseReference(1234L)
            .caseDataLiP(CaseDataLiP.builder()
                             .applicant1LiPResponse(ClaimantLiPResponse.builder().applicant1SignedSettlementAgreement(YesOrNo.YES).build())
                             .build())
            .respondent1RespondToSettlementAgreementDeadline(LocalDateTime.now().minusDays(1))
            .build();

        CallbackParams callbackParams = CallbackParamsBuilder.builder()
            .of(ABOUT_TO_SUBMIT, caseData)
            .build();

        handler.handle(callbackParams);
        verify(dashboardScenariosService).recordScenarios(
            "BEARER_TOKEN",
            SCENARIO_AAA6_CCJ_CLAIMANT_ACCEPT_OR_REJECT_PLAN_SETTLEMENT_REQUESTED_NO_DEF_RESPONSE_DEFENDANT.getScenario(),
            caseData.getCcdCaseReference().toString(),
            ScenarioRequestParams.builder().params(params).build()
        );
    }
}
