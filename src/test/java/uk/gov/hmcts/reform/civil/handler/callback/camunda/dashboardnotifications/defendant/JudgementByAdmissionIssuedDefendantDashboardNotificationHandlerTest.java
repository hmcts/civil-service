package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.defendant;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentDetails;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentInstalmentDetails;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentPaymentPlan;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentState;
import uk.gov.hmcts.reform.civil.model.judgmentonline.PaymentFrequency;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.PartyBuilder;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;

import java.time.LocalDate;
import java.time.Month;
import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_DASHBOARD_NOTIFICATION_JUDGEMENT_BY_ADMISSION_DEFENDANT;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec.SUGGESTION_OF_REPAYMENT_PLAN;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CLAIMANT_INTENT_REQUESTED_CCJ_CLAIMANT_ACCEPTED_DEFENDANT_PLAN_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_JUDGEMENTS_ONLINE_ISSUED_CCJ_DEFENDANT;
import static uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentState.ISSUED;
import static uk.gov.hmcts.reform.civil.model.judgmentonline.PaymentPlanSelection.PAY_IN_INSTALMENTS;

@ExtendWith(MockitoExtension.class)
class JudgementByAdmissionIssuedDefendantDashboardNotificationHandlerTest extends BaseCallbackHandlerTest {

    @InjectMocks
    private JudgementByAdmissionIssuedDefendantDashboardNotificationHandler handler;

    @Mock
    private DashboardScenariosService dashboardScenariosService;

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
    void shouldCreateDashboardNotifications_whenDashboardIsEnabled() {
        params.put("ccdCaseReference", "123");

        when(featureToggleService.isJudgmentOnlineLive()).thenReturn(true);
        when(dashboardNotificationsParamsMapper.mapCaseDataToParams(any())).thenReturn(params);

        JudgmentInstalmentDetails instalmentDetails = new JudgmentInstalmentDetails();
        instalmentDetails.setAmount("20001");
        instalmentDetails.setPaymentFrequency(PaymentFrequency.MONTHLY);
        instalmentDetails.setStartDate(LocalDate.now());

        JudgmentPaymentPlan paymentPlan = new JudgmentPaymentPlan();
        paymentPlan.setType(PAY_IN_INSTALMENTS);

        JudgmentDetails activeJudgment = new JudgmentDetails();
        activeJudgment.setState(ISSUED);
        activeJudgment.setPaymentPlan(paymentPlan);
        activeJudgment.setOrderedAmount("150001");
        activeJudgment.setInstalmentDetails(instalmentDetails);

        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setLegacyCaseReference("reference");
        caseData.setCcdCaseReference(1234L);
        caseData.setRespondent1ResponseDeadline(LocalDate.of(2020, Month.JANUARY, 18).atStartOfDay());
        caseData.setRespondent1Represented(YesOrNo.NO);
        caseData.setDefenceAdmitPartPaymentTimeRouteRequired(SUGGESTION_OF_REPAYMENT_PLAN);
        caseData.setActiveJudgment(activeJudgment);

        CallbackParams callbackParams = CallbackParamsBuilder.builder()
            .of(ABOUT_TO_SUBMIT, caseData)
            .build();

        handler.handle(callbackParams);
        verify(dashboardScenariosService).recordScenarios(
            "BEARER_TOKEN",
            SCENARIO_AAA6_JUDGEMENTS_ONLINE_ISSUED_CCJ_DEFENDANT.getScenario(),
            caseData.getCcdCaseReference().toString(),
            ScenarioRequestParams.builder().params(params).build()
        );
    }

    @Test
    void shouldCreateDashboardNotificationsWhenPaymentPlanAcceptedAndCCJ() {
        params.put("ccdCaseReference", "123");

        when(featureToggleService.isJudgmentOnlineLive()).thenReturn(true);
        when(dashboardNotificationsParamsMapper.mapCaseDataToParams(any())).thenReturn(params);

        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setLegacyCaseReference("reference");
        caseData.setCcdCaseReference(1234L);
        caseData.setRespondent1ResponseDeadline(LocalDate.of(2020, Month.JANUARY, 18).atStartOfDay());
        caseData.setRespondent1Represented(YesOrNo.NO);
        caseData.setDefenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.BY_SET_DATE);
        caseData.setApplicant1AcceptPartAdmitPaymentPlanSpec(YesOrNo.YES);

        CallbackParams callbackParams = CallbackParamsBuilder.builder()
            .of(ABOUT_TO_SUBMIT, caseData)
            .build();

        handler.handle(callbackParams);
        verify(dashboardScenariosService).recordScenarios(
            "BEARER_TOKEN",
            SCENARIO_AAA6_JUDGEMENTS_ONLINE_ISSUED_CCJ_DEFENDANT.getScenario(),
            caseData.getCcdCaseReference().toString(),
            ScenarioRequestParams.builder().params(params).build()
        );
    }

    @Test
    void shouldNotCreateDashboardNotificationsWhenRespondentIsNotLip() {
        params.put("ccdCaseReference", "123");

        when(featureToggleService.isJudgmentOnlineLive()).thenReturn(true);
        when(dashboardNotificationsParamsMapper.mapCaseDataToParams(any())).thenReturn(params);

        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setLegacyCaseReference("reference");
        caseData.setCcdCaseReference(1234L);
        caseData.setRespondent1Represented(YesOrNo.YES);
        caseData.setDefenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.BY_SET_DATE);
        caseData.setApplicant1AcceptPartAdmitPaymentPlanSpec(YesOrNo.YES);

        CallbackParams callbackParams = CallbackParamsBuilder.builder()
            .of(ABOUT_TO_SUBMIT, caseData)
            .build();

        handler.handle(callbackParams);
        verify(dashboardScenariosService, never()).recordScenarios(
            "BEARER_TOKEN",
            SCENARIO_AAA6_CLAIMANT_INTENT_REQUESTED_CCJ_CLAIMANT_ACCEPTED_DEFENDANT_PLAN_DEFENDANT.getScenario(),
            caseData.getCcdCaseReference().toString(),
            ScenarioRequestParams.builder().params(params).build()
        );
    }

    @Test
    void shouldCreateDashboardNotifications_WhenLipvsLipIndividualOrSoleTraderWithJoIssued() {
        params.put("ccdCaseReference", "123");

        when(featureToggleService.isJudgmentOnlineLive()).thenReturn(true);
        when(dashboardNotificationsParamsMapper.mapCaseDataToParams(any())).thenReturn(params);

        DynamicListElement dynamicListElement = new DynamicListElement();
        dynamicListElement.setLabel("John Doe");

        DynamicList defendantDetailsSpec = new DynamicList();
        defendantDetailsSpec.setValue(dynamicListElement);

        JudgmentDetails activeJudgment = new JudgmentDetails();
        activeJudgment.setIssueDate(LocalDate.now());
        activeJudgment.setState(JudgmentState.ISSUED);

        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setLegacyCaseReference("reference");
        caseData.setCcdCaseReference(1234L);
        caseData.setRespondent1Represented(YesOrNo.NO);
        caseData.setApplicant1Represented(YesOrNo.NO);
        caseData.setDefenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.BY_SET_DATE);
        caseData.setApplicant1AcceptPartAdmitPaymentPlanSpec(YesOrNo.YES);
        caseData.setDefendantDetailsSpec(defendantDetailsSpec);
        caseData.setRespondent1(PartyBuilder.builder().individual().build());
        caseData.setActiveJudgment(activeJudgment);

        CallbackParams callbackParams = CallbackParamsBuilder.builder()
            .of(ABOUT_TO_SUBMIT, caseData)
            .build();

        handler.handle(callbackParams);
        verify(dashboardScenariosService).recordScenarios(
            "BEARER_TOKEN",
            SCENARIO_AAA6_JUDGEMENTS_ONLINE_ISSUED_CCJ_DEFENDANT.getScenario(),
            caseData.getCcdCaseReference().toString(),
            ScenarioRequestParams.builder().params(params).build()
        );
    }

    @Test
    void shouldCreateDashboardNotifications_WhenLipvsLipCompanyOrOrganisationWithRepaymentPlanAccepted() {
        params.put("ccdCaseReference", "123");

        when(featureToggleService.isJudgmentOnlineLive()).thenReturn(true);
        when(dashboardNotificationsParamsMapper.mapCaseDataToParams(any())).thenReturn(params);

        DynamicListElement dynamicListElement = new DynamicListElement();
        dynamicListElement.setLabel("John Doe");

        DynamicList defendantDetailsSpec = new DynamicList();
        defendantDetailsSpec.setValue(dynamicListElement);

        JudgmentDetails activeJudgment = new JudgmentDetails();
        activeJudgment.setIssueDate(LocalDate.now());
        activeJudgment.setState(JudgmentState.ISSUED);

        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setLegacyCaseReference("reference");
        caseData.setCcdCaseReference(1234L);
        caseData.setRespondent1Represented(YesOrNo.NO);
        caseData.setApplicant1Represented(YesOrNo.NO);
        caseData.setDefenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.BY_SET_DATE);
        caseData.setApplicant1AcceptPartAdmitPaymentPlanSpec(YesOrNo.YES);
        caseData.setDefendantDetailsSpec(defendantDetailsSpec);
        caseData.setRespondent1(PartyBuilder.builder().organisation().build());
        caseData.setActiveJudgment(activeJudgment);

        CallbackParams callbackParams = CallbackParamsBuilder.builder()
            .of(ABOUT_TO_SUBMIT, caseData)
            .build();

        handler.handle(callbackParams);
        verify(dashboardScenariosService).recordScenarios(
            "BEARER_TOKEN",
            SCENARIO_AAA6_JUDGEMENTS_ONLINE_ISSUED_CCJ_DEFENDANT.getScenario(),
            caseData.getCcdCaseReference().toString(),
            ScenarioRequestParams.builder().params(params).build()
        );
    }
}
