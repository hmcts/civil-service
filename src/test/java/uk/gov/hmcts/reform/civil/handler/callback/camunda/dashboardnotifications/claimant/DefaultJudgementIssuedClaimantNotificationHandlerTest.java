package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.claimant;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentDetails;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentState;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentType;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;

import java.time.LocalDate;
import java.time.Month;
import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_DASHBOARD_NOTIFICATION_DJ_NON_DIVERGENT_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_JUDGEMENTS_ONLINE_DEFAULT_JUDGEMENT_GRANTED_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_JUDGEMENTS_ONLINE_DEFAULT_JUDGEMENT_ISSUED_CLAIMANT;

@ExtendWith(MockitoExtension.class)
public class DefaultJudgementIssuedClaimantNotificationHandlerTest extends BaseCallbackHandlerTest {

    @InjectMocks
    private DefaultJudgementIssuedClaimantNotificationHandler handler;

    @Mock
    private DashboardScenariosService dashboardScenariosService;

    @Mock
    private DashboardNotificationsParamsMapper dashboardNotificationsParamsMapper;

    @Mock
    private FeatureToggleService featureToggleService;

    public static final String TASK_ID = "GenerateDashboardNotificationDJNonDivergentClaimant";

    HashMap<String, Object> params = new HashMap<>();

    @Test
    void handleEventsReturnsTheExpectedCallbackEvent() {
        assertThat(handler.handledEvents()).contains(
            CREATE_DASHBOARD_NOTIFICATION_DJ_NON_DIVERGENT_CLAIMANT);
    }

    @Test
    void shouldReturnCorrectCamundaActivityId_whenInvoked() {
        assertThat(handler.camundaActivityId(
            CallbackParamsBuilder.builder()
                .request(CallbackRequest.builder()
                             .eventId(CREATE_DASHBOARD_NOTIFICATION_DJ_NON_DIVERGENT_CLAIMANT.name())
                             .build())
                .build()))
            .isEqualTo(TASK_ID);
    }

    @Test
    void shouldCreateDefaultJudgmentIssuedDashboardNotifications_whenDashboardIsEnabledAndJudgmentBufferDisabled() {
        params.put("ccdCaseReference", "123");
        when(featureToggleService.isJudgmentOnlineLive()).thenReturn(true);
        when(featureToggleService.isJudgmentBufferEnabled()).thenReturn(false);
        when(dashboardNotificationsParamsMapper.mapCaseDataToParams(any())).thenReturn(params);

        CaseData caseData = defaultJudgmentIssuedCaseData();
        CallbackParams callbackParams = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();

        handler.handle(callbackParams);
        verify(dashboardScenariosService).recordScenarios(
            "BEARER_TOKEN",
            SCENARIO_AAA6_JUDGEMENTS_ONLINE_DEFAULT_JUDGEMENT_ISSUED_CLAIMANT.getScenario(),
            caseData.getCcdCaseReference().toString(),
            new ScenarioRequestParams(params)
        );
    }

    @Test
    void shouldCreateDefaultJudgmentGrantedDashboardNotifications_whenDashboardAndJudgmentBufferAreEnabled() {
        params.put("ccdCaseReference", "123");
        when(featureToggleService.isJudgmentOnlineLive()).thenReturn(true);
        when(featureToggleService.isJudgmentBufferEnabled()).thenReturn(true);
        when(dashboardNotificationsParamsMapper.mapCaseDataToParams(any())).thenReturn(params);

        CaseData caseData = defaultJudgmentIssuedCaseData();
        CallbackParams callbackParams = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();

        handler.handle(callbackParams);
        verify(dashboardScenariosService).recordScenarios(
            "BEARER_TOKEN",
            SCENARIO_AAA6_JUDGEMENTS_ONLINE_DEFAULT_JUDGEMENT_GRANTED_CLAIMANT.getScenario(),
            caseData.getCcdCaseReference().toString(),
            new ScenarioRequestParams(params)
        );
    }

    @Test
    void shouldNotCreateDefaultJudgmentGrantedDashboardNotifications_whenJudgmentBufferEnabledAndJudgmentNotIssued() {
        params.put("ccdCaseReference", "123");
        when(featureToggleService.isJudgmentOnlineLive()).thenReturn(true);
        when(featureToggleService.isJudgmentBufferEnabled()).thenReturn(true);
        when(dashboardNotificationsParamsMapper.mapCaseDataToParams(any())).thenReturn(params);

        CaseData caseData = defaultJudgmentRequestedCaseData();
        CallbackParams callbackParams = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();

        handler.handle(callbackParams);

        verifyNoInteractions(dashboardScenariosService);
    }

    private CaseData defaultJudgmentIssuedCaseData() {
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setLegacyCaseReference("reference");
        caseData.setCcdCaseReference(1234L);
        caseData.setCcdState(CaseState.All_FINAL_ORDERS_ISSUED);
        caseData.setRespondent1ResponseDeadline(LocalDate.of(2020, Month.JANUARY, 18).atStartOfDay());
        caseData.setApplicant1Represented(YesOrNo.NO);
        caseData.setActiveJudgment(defaultJudgmentIssued());
        return caseData;
    }

    private CaseData defaultJudgmentRequestedCaseData() {
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setLegacyCaseReference("reference");
        caseData.setCcdCaseReference(1234L);
        caseData.setCcdState(CaseState.JUDGMENT_REQUESTED);
        caseData.setRespondent1ResponseDeadline(LocalDate.of(2020, Month.JANUARY, 18).atStartOfDay());
        caseData.setApplicant1Represented(YesOrNo.NO);
        return caseData;
    }

    private JudgmentDetails defaultJudgmentIssued() {
        return new JudgmentDetails()
            .setType(JudgmentType.DEFAULT_JUDGMENT)
            .setState(JudgmentState.ISSUED);
    }
}
