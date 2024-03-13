package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.defendant;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.client.DashboardApiClient;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.service.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_DEFENDANT_DASHBOARD_NOTIFICATION_FOR_CLAIMANT_RESPONSE;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec.PART_ADMISSION;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA7_CLAIMANT_INTENT_CLAIM_SETTLED_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA7_CLAIMANT_INTENT_PART_ADMIT_DEFENDANT;

@ExtendWith(MockitoExtension.class)
public class ClaimantResponseDefendantNotificationHandlerTest extends BaseCallbackHandlerTest {

    @InjectMocks
    private ClaimantResponseDefendantNotificationHandler handler;

    @Mock
    private DashboardApiClient dashboardApiClient;

    @Mock
    private DashboardNotificationsParamsMapper dashboardNotificationsParamsMapper;

    public static final String TASK_ID = "GenerateDefendantDashboardNotificationClaimantResponse";

    @Test
    void handleEventsReturnsTheExpectedCallbackEvent() {
        assertThat(handler.handledEvents()).contains(CREATE_DEFENDANT_DASHBOARD_NOTIFICATION_FOR_CLAIMANT_RESPONSE);
    }

    @Test
    void shouldReturnCorrectCamundaActivityId_whenInvoked() {
        assertThat(handler.camundaActivityId(
            CallbackParamsBuilder.builder()
                .request(CallbackRequest.builder()
                             .eventId(CREATE_DEFENDANT_DASHBOARD_NOTIFICATION_FOR_CLAIMANT_RESPONSE.name())
                             .build())
                .build()))
            .isEqualTo(TASK_ID);
    }

    @Test
    public void configureDashboardNotificationsForClaimantResponse() {

        Map<String, Object> params = new HashMap<>();

        when(dashboardNotificationsParamsMapper.mapCaseDataToParams(any())).thenReturn(params);

        CaseData caseData = CaseData.builder()
            .legacyCaseReference("reference")
            .ccdCaseReference(1234L)
            .ccdState(CaseState.CASE_SETTLED)
            .applicant1PartAdmitIntentionToSettleClaimSpec(YesOrNo.YES)
            .specRespondent1Represented(YesOrNo.NO)
            .respondent1Represented(YesOrNo.NO)
            .build();

        CallbackParams callbackParams = CallbackParamsBuilder.builder()
            .of(ABOUT_TO_SUBMIT, caseData)
            .build();

        handler.handle(callbackParams);

        verify(dashboardApiClient, times(1)).recordScenario(
            caseData.getCcdCaseReference().toString(),
            SCENARIO_AAA7_CLAIMANT_INTENT_CLAIM_SETTLED_DEFENDANT.getScenario(),
            "BEARER_TOKEN",
            ScenarioRequestParams.builder().params(params).build()
        );
    }

    @Test
    public void configureDashboardNotificationsForClaimantResponseForPartAdmitImmediately() {

        Map<String, Object> params = new HashMap<>();

        when(dashboardNotificationsParamsMapper.mapCaseDataToParams(any())).thenReturn(params);

        CaseData caseData = CaseData.builder()
            .legacyCaseReference("reference")
            .ccdCaseReference(1234L)
            .ccdState(CaseState.CASE_SETTLED)
            .respondent1ClaimResponseTypeForSpec(PART_ADMISSION)
            .applicant1AcceptAdmitAmountPaidSpec(YesOrNo.YES)
            .applicant1AcceptPartAdmitPaymentPlanSpec(null)
            .respondent1Represented(YesOrNo.NO)
            .build();

        CallbackParams callbackParams = CallbackParamsBuilder.builder()
            .of(ABOUT_TO_SUBMIT, caseData)
            .build();

        handler.handle(callbackParams);

        verify(dashboardApiClient, times(1)).recordScenario(
            caseData.getCcdCaseReference().toString(),
            SCENARIO_AAA7_CLAIMANT_INTENT_PART_ADMIT_DEFENDANT.getScenario(),
            "BEARER_TOKEN",
            ScenarioRequestParams.builder().params(params).build()
        );
    }
}
