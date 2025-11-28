package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.claimant;

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
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentState;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentType;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.PartyBuilder;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;

import java.time.LocalDate;
import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_DASHBOARD_NOTIFICATION_JUDGEMENT_BY_ADMISSION_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_UPDATE_JUDGEMENTS_ONLINE_ISSUED_CCJ_CLAIMANT;

@ExtendWith(MockitoExtension.class)
class JudgementByAdmissionIssuedClaimantDashboardNotificationHandlerTest extends BaseCallbackHandlerTest {

    @InjectMocks
    private JudgementByAdmissionIssuedClaimantDashboardNotificationHandler handler;

    @Mock
    private DashboardScenariosService dashboardScenariosService;

    @Mock
    private DashboardNotificationsParamsMapper dashboardNotificationsParamsMapper;

    @Mock
    private FeatureToggleService featureToggleService;

    public static final String TASK_ID = "GenerateDashboardNotificationJudgementByAdmissionClaimant";

    HashMap<String, Object> params = new HashMap<>();

    @Test
    void handleEventsReturnsTheExpectedCallbackEvent() {
        assertThat(handler.handledEvents()).contains(
            CREATE_DASHBOARD_NOTIFICATION_JUDGEMENT_BY_ADMISSION_CLAIMANT);
    }

    @Test
    void shouldReturnCorrectCamundaActivityId_whenInvoked() {
        assertThat(handler.camundaActivityId(
            CallbackParamsBuilder.builder()
                .request(CallbackRequest.builder()
                             .eventId(CREATE_DASHBOARD_NOTIFICATION_JUDGEMENT_BY_ADMISSION_CLAIMANT.name())
                             .build())
                .build()))
            .isEqualTo(TASK_ID);
    }

    @Test
    void shouldCreateDashboardNotifications_WhenLipvsLipIndividualOrSoleTraderWithJoIssued() {
        params.put("ccdCaseReference", "123");

        when(featureToggleService.isJudgmentOnlineLive()).thenReturn(true);
        when(dashboardNotificationsParamsMapper.mapCaseDataToParams(any())).thenReturn(params);

        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setLegacyCaseReference("reference");
        caseData.setCcdCaseReference(1234L);
        caseData.setRespondent1Represented(YesOrNo.NO);
        caseData.setApplicant1Represented(YesOrNo.NO);
        caseData.setDefenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.BY_SET_DATE);
        caseData.setApplicant1AcceptPartAdmitPaymentPlanSpec(YesOrNo.YES);
        DynamicListElement dynamicListElement = new DynamicListElement();
        dynamicListElement.setLabel("John Doe");
        DynamicList dynamicList = new DynamicList();
        dynamicList.setValue(dynamicListElement);
        caseData.setDefendantDetailsSpec(dynamicList);
        caseData.setRespondent1(PartyBuilder.builder().individual().build());
        JudgmentDetails judgmentDetails = new JudgmentDetails();
        judgmentDetails.setIssueDate(LocalDate.now());
        judgmentDetails.setState(JudgmentState.ISSUED);
        judgmentDetails.setType(JudgmentType.JUDGMENT_BY_ADMISSION);
        caseData.setActiveJudgment(judgmentDetails);

        CallbackParams callbackParams = CallbackParamsBuilder.builder()
            .of(ABOUT_TO_SUBMIT, caseData)
            .build();

        handler.handle(callbackParams);
        verify(dashboardScenariosService).recordScenarios(
            "BEARER_TOKEN",
            SCENARIO_AAA6_UPDATE_JUDGEMENTS_ONLINE_ISSUED_CCJ_CLAIMANT.getScenario(),
            caseData.getCcdCaseReference().toString(),
            ScenarioRequestParams.builder().params(params).build()
        );
    }

    @Test
    void shouldCreateDashboardNotifications_WhenLipvsLipCompanyOrOrganisationWithRepaymentPlanAccepted() {
        params.put("ccdCaseReference", "123");

        when(featureToggleService.isJudgmentOnlineLive()).thenReturn(true);
        when(dashboardNotificationsParamsMapper.mapCaseDataToParams(any())).thenReturn(params);

        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setLegacyCaseReference("reference");
        caseData.setCcdCaseReference(1234L);
        caseData.setRespondent1Represented(YesOrNo.NO);
        caseData.setApplicant1Represented(YesOrNo.NO);
        caseData.setDefenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.BY_SET_DATE);
        caseData.setApplicant1AcceptPartAdmitPaymentPlanSpec(YesOrNo.YES);
        DynamicListElement dynamicListElement = new DynamicListElement();
        dynamicListElement.setLabel("John Doe");
        DynamicList dynamicList = new DynamicList();
        dynamicList.setValue(dynamicListElement);
        caseData.setDefendantDetailsSpec(dynamicList);
        caseData.setRespondent1(PartyBuilder.builder().organisation().build());
        JudgmentDetails judgmentDetails = new JudgmentDetails();
        judgmentDetails.setIssueDate(LocalDate.now());
        judgmentDetails.setState(JudgmentState.ISSUED);
        judgmentDetails.setType(JudgmentType.JUDGMENT_BY_ADMISSION);
        caseData.setActiveJudgment(judgmentDetails);

        CallbackParams callbackParams = CallbackParamsBuilder.builder()
            .of(ABOUT_TO_SUBMIT, caseData)
            .build();

        handler.handle(callbackParams);
        verify(dashboardScenariosService).recordScenarios(
            "BEARER_TOKEN",
            SCENARIO_AAA6_UPDATE_JUDGEMENTS_ONLINE_ISSUED_CCJ_CLAIMANT.getScenario(),
            caseData.getCcdCaseReference().toString(),
            ScenarioRequestParams.builder().params(params).build()
        );
    }

    @Test
    void shouldNotCreateDashboardNotifications_WhenLRClaimant() {
        params.put("ccdCaseReference", "123");

        when(featureToggleService.isJudgmentOnlineLive()).thenReturn(true);
        when(dashboardNotificationsParamsMapper.mapCaseDataToParams(any())).thenReturn(params);

        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setLegacyCaseReference("reference");
        caseData.setCcdCaseReference(1234L);
        caseData.setRespondent1Represented(YesOrNo.NO);
        caseData.setApplicant1Represented(YesOrNo.YES);
        DynamicListElement dynamicListElement = new DynamicListElement();
        dynamicListElement.setLabel("John Doe");
        DynamicList dynamicList = new DynamicList();
        dynamicList.setValue(dynamicListElement);
        caseData.setDefendantDetailsSpec(dynamicList);
        caseData.setRespondent1(PartyBuilder.builder().individual().build());

        CallbackParams callbackParams = CallbackParamsBuilder.builder()
            .of(ABOUT_TO_SUBMIT, caseData)
            .build();

        handler.handle(callbackParams);
        verifyNoInteractions(dashboardScenariosService);
    }
}
