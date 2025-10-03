package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications;

import org.junit.jupiter.api.Nested;
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
import uk.gov.hmcts.reform.civil.model.genapplication.GAInformOtherParty;
import uk.gov.hmcts.reform.civil.model.genapplication.GAUrgencyRequirement;
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
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_APPLICATION_SUBMITTED_DASHBOARD_NOTIFICATION_FOR_RESPONDENT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_GENERAL_APPLICATION_SUBMITTED_NONURGENT_RESPONDENT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_GENERAL_APPLICATION_SUBMITTED_URGENT_RESPONDENT;

@ExtendWith(MockitoExtension.class)
public class CreateRespondentDashboardNotificationForApplicationSubmittedHandlerTest extends BaseCallbackHandlerTest {

    @Mock
    private DashboardScenariosService dashboardScenariosService;
    @Mock
    private DashboardNotificationsParamsMapper mapper;
    @Mock
    private FeatureToggleService featureToggleService;
    @InjectMocks
    private CreateRespondentDashboardNotificationForApplicationSubmittedHandler handler;

    @Test
    void handleEventsReturnsTheExpectedCallbackEvent() {
        assertThat(handler.handledEvents()).contains(CREATE_APPLICATION_SUBMITTED_DASHBOARD_NOTIFICATION_FOR_RESPONDENT);
    }

    @Test
    void shouldReturnCorrectCamundaActivityId_whenInvoked() {
        assertThat(handler.camundaActivityId(CallbackParams.builder().build()))
            .isEqualTo("default");
    }

    @Nested
    class AboutToSubmitCallback {

        @Test
        void shouldRecordApplicationSubmittedRespondentScenarioForWithNoticeNonUrgentApplicationWhenInvoked() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft().withNoticeCaseData();
            caseData = caseData.toBuilder()
                .parentCaseReference(caseData.getCcdCaseReference().toString())
                .isGaApplicantLip(YesOrNo.YES)
                .parentClaimantIsApplicant(YesOrNo.YES)
                .generalAppInformOtherParty(GAInformOtherParty.builder().isWithNotice(YesOrNo.YES).build())
                .generalAppUrgencyRequirement(GAUrgencyRequirement.builder().generalAppUrgency(YesOrNo.NO).build())
                .build();

            HashMap<String, Object> scenarioParams = new HashMap<>();
            when(featureToggleService.isLipVLipEnabled()).thenReturn(true);
            when(mapper.mapCaseDataToParams(any())).thenReturn(scenarioParams);

            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(CREATE_APPLICATION_SUBMITTED_DASHBOARD_NOTIFICATION_FOR_RESPONDENT.name())
                    .build()
            ).build();

            handler.handle(params);
            verify(dashboardScenariosService).recordScenarios(
                    "BEARER_TOKEN",
                SCENARIO_AAA6_GENERAL_APPLICATION_SUBMITTED_NONURGENT_RESPONDENT.getScenario(),
                    caseData.getCcdCaseReference().toString(),
                ScenarioRequestParams.builder().params(scenarioParams).build()
            );
        }

        @Test
        void shouldRecordApplicationSubmittedRespondentScenarioForWithNoticeUrgentApplicationWhenInvoked() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft().withNoticeCaseData();
            caseData = caseData.toBuilder()
                .parentCaseReference(caseData.getCcdCaseReference().toString())
                .isGaApplicantLip(YesOrNo.YES)
                .parentClaimantIsApplicant(YesOrNo.YES)
                .generalAppInformOtherParty(GAInformOtherParty.builder().isWithNotice(YesOrNo.YES).build())
                .generalAppUrgencyRequirement(GAUrgencyRequirement.builder().generalAppUrgency(YesOrNo.YES).build())
                .build();

            HashMap<String, Object> scenarioParams = new HashMap<>();
            when(featureToggleService.isLipVLipEnabled()).thenReturn(true);
            when(mapper.mapCaseDataToParams(any())).thenReturn(scenarioParams);

            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(SCENARIO_AAA6_GENERAL_APPLICATION_SUBMITTED_URGENT_RESPONDENT.name())
                    .build()
            ).build();

            handler.handle(params);
            verify(dashboardScenariosService).recordScenarios(
                    "BEARER_TOKEN",
                SCENARIO_AAA6_GENERAL_APPLICATION_SUBMITTED_URGENT_RESPONDENT.getScenario(),
                    caseData.getCcdCaseReference().toString(),
                ScenarioRequestParams.builder().params(scenarioParams).build()
            );
        }

        @Test
        void shouldRecordApplicationSubmittedRespondentScenarioForWithConsentNonUrgentApplicationWhenInvoked() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft().withNoticeCaseData();
            caseData = caseData.toBuilder()
                .parentCaseReference(caseData.getCcdCaseReference().toString())
                .isGaApplicantLip(YesOrNo.YES)
                .parentClaimantIsApplicant(YesOrNo.YES)
                .generalAppConsentOrder(YesOrNo.YES)
                .generalAppUrgencyRequirement(GAUrgencyRequirement.builder().generalAppUrgency(YesOrNo.NO).build())
                .build();

            HashMap<String, Object> scenarioParams = new HashMap<>();
            when(featureToggleService.isLipVLipEnabled()).thenReturn(true);
            when(mapper.mapCaseDataToParams(any())).thenReturn(scenarioParams);

            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(CREATE_APPLICATION_SUBMITTED_DASHBOARD_NOTIFICATION_FOR_RESPONDENT.name())
                    .build()
            ).build();

            handler.handle(params);
            verify(dashboardScenariosService).recordScenarios(
                    "BEARER_TOKEN",
                SCENARIO_AAA6_GENERAL_APPLICATION_SUBMITTED_NONURGENT_RESPONDENT.getScenario(),
                    caseData.getCcdCaseReference().toString(),
                ScenarioRequestParams.builder().params(scenarioParams).build()
            );
        }

        @Test
        void shouldRecordApplicationSubmittedRespondentScenarioForWithConsentUrgentApplicationWhenInvoked() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft().withNoticeCaseData();
            caseData = caseData.toBuilder()
                .parentCaseReference(caseData.getCcdCaseReference().toString())
                .isGaApplicantLip(YesOrNo.YES)
                .parentClaimantIsApplicant(YesOrNo.YES)
                .generalAppConsentOrder(YesOrNo.YES)
                .generalAppUrgencyRequirement(GAUrgencyRequirement.builder().generalAppUrgency(YesOrNo.YES).build())
                .build();

            HashMap<String, Object> scenarioParams = new HashMap<>();
            when(featureToggleService.isLipVLipEnabled()).thenReturn(true);
            when(mapper.mapCaseDataToParams(any())).thenReturn(scenarioParams);

            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(SCENARIO_AAA6_GENERAL_APPLICATION_SUBMITTED_URGENT_RESPONDENT.name())
                    .build()
            ).build();

            handler.handle(params);
            verify(dashboardScenariosService).recordScenarios(
                    "BEARER_TOKEN",
                SCENARIO_AAA6_GENERAL_APPLICATION_SUBMITTED_URGENT_RESPONDENT.getScenario(),
                    caseData.getCcdCaseReference().toString(),
                ScenarioRequestParams.builder().params(scenarioParams).build()
            );
        }

        @Test
        void shouldNotRecordScenarioWhenGALipsToggleIsDisableInvoked() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft().withNoticeCaseData();
            caseData = caseData.toBuilder()
                .parentCaseReference(caseData.getCcdCaseReference().toString())
                .isGaApplicantLip(YesOrNo.YES)
                .parentClaimantIsApplicant(YesOrNo.YES)
                .generalAppUrgencyRequirement(GAUrgencyRequirement.builder().generalAppUrgency(YesOrNo.NO).build())
                .generalAppInformOtherParty(GAInformOtherParty.builder().isWithNotice(YesOrNo.YES).build())
                .build();
            when(featureToggleService.isLipVLipEnabled()).thenReturn(false);

            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(CREATE_APPLICATION_SUBMITTED_DASHBOARD_NOTIFICATION_FOR_RESPONDENT.name())
                    .build()
            ).build();

            handler.handle(params);
            verifyNoInteractions(dashboardScenariosService);
        }
    }
}
