package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;

import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_RESPONDENT_DASHBOARD_NOTIFICATION_ORDER_MADE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_GENERAL_APPLICATION_ORDER_MADE_RESPONDENT;

@ExtendWith(MockitoExtension.class)
class CreateDashboardNotificationWhenFinalOrderMadeRespondentHandlerTest extends BaseCallbackHandlerTest {

    @Mock
    private DashboardScenariosService dashboardScenariosService;
    @Mock
    private DashboardNotificationsParamsMapper mapper;
    @Mock
    private FeatureToggleService featureToggleService;
    private CreateDashboardNotificationWhenFinalOrderMadeRespondentHandler handler;

    @BeforeEach
    void setUp() {
        handler = new CreateDashboardNotificationWhenFinalOrderMadeRespondentHandler(
            dashboardScenariosService,
            mapper,
            featureToggleService,
            objectMapper
        );
    }

    @Test
    void handleEventsReturnsTheExpectedCallbackEvent() {
        assertThat(handler.handledEvents()).contains(CREATE_RESPONDENT_DASHBOARD_NOTIFICATION_ORDER_MADE);
    }

    @Test
    void shouldReturnCorrectCamundaActivityId_whenInvoked() {
        assertThat(handler.camundaActivityId(CallbackParams.builder().build()))
            .isEqualTo("default");
    }

    @Nested
    class AboutToSubmitCallback {

        @Test
        void shouldRecordApplicationSubmittedScenario_whenInvoked() {
            when(featureToggleService.isLipVLipEnabled()).thenReturn(true);
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft().withNoticeCaseData();
            caseData = caseData.toBuilder().parentCaseReference(caseData.getCcdCaseReference().toString())
                .isGaApplicantLip(YesOrNo.YES)
                .parentClaimantIsApplicant(YesOrNo.YES)
                .build();
            HashMap<String, Object> scenarioParams = new HashMap<>();
            when(mapper.mapCaseDataToParams(any())).thenReturn(scenarioParams);
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(CREATE_RESPONDENT_DASHBOARD_NOTIFICATION_ORDER_MADE.name())
                    .build()
            ).build();
            handler.handle(params);
            verify(dashboardScenariosService).recordScenarios(
                    "BEARER_TOKEN",
                SCENARIO_AAA6_GENERAL_APPLICATION_ORDER_MADE_RESPONDENT.getScenario(),
                    caseData.getCcdCaseReference().toString(),
                ScenarioRequestParams.builder().params(scenarioParams).build()
            );
        }

        @Test
        void shouldNotRecordApplicationSubmittedScenario_whenInvoked() {
            when(featureToggleService.isLipVLipEnabled()).thenReturn(true);
            CaseData caseData = CaseDataBuilder.builder().withoutNoticeCaseData();
            caseData = caseData.toBuilder().parentCaseReference(caseData.getCcdCaseReference().toString())
                .isGaApplicantLip(YesOrNo.YES)
                .parentClaimantIsApplicant(YesOrNo.YES)
                .build();
            HashMap<String, Object> scenarioParams = new HashMap<>();
            when(mapper.mapCaseDataToParams(any())).thenReturn(scenarioParams);
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(CREATE_RESPONDENT_DASHBOARD_NOTIFICATION_ORDER_MADE.name())
                    .build()
            ).build();
            handler.handle(params);
            verifyNoInteractions(dashboardScenariosService);
        }

    }

    @Nested
    class GaCaseDataOnly {

        @Test
        void shouldRecordScenarioWhenGaCaseDataProvided() {
            when(featureToggleService.isLipVLipEnabled()).thenReturn(true);
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft().withNoticeCaseData();
            caseData = caseData.toBuilder().parentCaseReference(caseData.getCcdCaseReference().toString())
                .isGaApplicantLip(YesOrNo.YES)
                .parentClaimantIsApplicant(YesOrNo.YES)
                .build();
            GeneralApplicationCaseData gaCaseData = toGaCaseData(caseData);
            HashMap<String, Object> scenarioParams = new HashMap<>();
            when(mapper.mapCaseDataToParams(any())).thenReturn(scenarioParams);

            CallbackParams params = gaCallbackParamsOf(gaCaseData, ABOUT_TO_SUBMIT);
            handler.handle(params);

            verify(dashboardScenariosService).recordScenarios(
                "BEARER_TOKEN",
                SCENARIO_AAA6_GENERAL_APPLICATION_ORDER_MADE_RESPONDENT.getScenario(),
                gaCaseData.getCcdCaseReference().toString(),
                ScenarioRequestParams.builder().params(scenarioParams).build()
            );
        }
    }
}
