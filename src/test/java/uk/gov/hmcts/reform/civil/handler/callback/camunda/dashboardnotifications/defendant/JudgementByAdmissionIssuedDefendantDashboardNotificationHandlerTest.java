package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.defendant;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.client.DashboardApiClient;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentDetails;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentInstalmentDetails;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentPaymentPlan;
import uk.gov.hmcts.reform.civil.model.judgmentonline.PaymentFrequency;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.service.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;

import java.time.LocalDate;
import java.time.Month;
import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_DASHBOARD_NOTIFICATION_JUDGEMENT_BY_ADMISSION_DEFENDANT;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec.SUGGESTION_OF_REPAYMENT_PLAN;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_JUDGEMENTS_ONLINE_ISSUED_CCJ_DEFENDANT;
import static uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentState.ISSUED;
import static uk.gov.hmcts.reform.civil.model.judgmentonline.PaymentPlanSelection.PAY_IN_INSTALMENTS;

@ExtendWith(MockitoExtension.class)
class JudgementByAdmissionIssuedDefendantDashboardNotificationHandlerTest extends BaseCallbackHandlerTest {

    @InjectMocks
    private JudgementByAdmissionIssuedDefendantDashboardNotificationHandler handler;

    @Mock
    private DashboardApiClient dashboardApiClient;

    @Mock
    private DashboardNotificationsParamsMapper dashboardNotificationsParamsMapper;

    @Mock
    private FeatureToggleService featureToggleService;

    public static final String TASK_ID = "GenerateDashboardNotificationJudgementByAdmissionDefendant";

    HashMap<String, Object> params = new HashMap<>();

    @Test
    void handleEventsReturnsTheExpectedCallbackEvent() {
        assertThat(handler.handledEvents()).contains(
            CREATE_DASHBOARD_NOTIFICATION_JUDGEMENT_BY_ADMISSION_DEFENDANT);
    }

    @Test
    void shouldReturnCorrectCamundaActivityId_whenInvoked() {
        assertThat(handler.camundaActivityId(
            CallbackParamsBuilder.builder()
                .request(CallbackRequest.builder()
                             .eventId(CREATE_DASHBOARD_NOTIFICATION_JUDGEMENT_BY_ADMISSION_DEFENDANT.name())
                             .build())
                .build()))
            .isEqualTo(TASK_ID);
    }

    @Test
    public void shouldCreateDashboardNotifications_whenDashboardIsEnabled() {
        params.put("ccdCaseReference", "123");

        when(featureToggleService.isJudgmentOnlineLive()).thenReturn(true);
        when(dashboardNotificationsParamsMapper.mapCaseDataToParams(any())).thenReturn(params);

        CaseData caseData = CaseData.builder()
            .legacyCaseReference("reference")
            .ccdCaseReference(1234L)
            .respondent1ResponseDeadline(LocalDate.of(2020, Month.JANUARY, 18).atStartOfDay())
            .respondent1Represented(YesOrNo.NO)
            .defenceAdmitPartPaymentTimeRouteRequired(SUGGESTION_OF_REPAYMENT_PLAN)
            .activeJudgment(JudgmentDetails.builder()
                                .state(ISSUED)
                                .paymentPlan(JudgmentPaymentPlan.builder().type(PAY_IN_INSTALMENTS).build())
                                .orderedAmount("150001")
                                .instalmentDetails(JudgmentInstalmentDetails.builder()
                                                       .amount("20001")
                                                       .paymentFrequency(PaymentFrequency.MONTHLY)
                                                       .startDate(LocalDate.now())
                                                       .build())
                                .build())
            .build();

        CallbackParams callbackParams = CallbackParamsBuilder.builder()
            .of(ABOUT_TO_SUBMIT, caseData)
            .build();

        handler.handle(callbackParams);
        verify(dashboardApiClient).recordScenario(
            caseData.getCcdCaseReference().toString(),
            SCENARIO_AAA6_JUDGEMENTS_ONLINE_ISSUED_CCJ_DEFENDANT.getScenario(),
            "BEARER_TOKEN",
            ScenarioRequestParams.builder().params(params).build()
        );
    }
}
