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
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;

import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_NOTIFICATION_REQUEST_FOR_RECONSIDERATION_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CP_REQUEST_FOR_RECONSIDERATION_REQUESTED_BY_OTHER_PARTY_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CP_REQUEST_FOR_RECONSIDERATION_REQUESTED_BY_OTHER_PARTY_DEFENDANT_RECIPIENT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CP_REQUEST_FOR_RECONSIDERATION_REQUESTED_BY_OTHER_PARTY_DELETE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CP_REQUEST_FOR_RECONSIDERATION_REQUESTED_BY_OTHER_PARTY_LR_RECIPIENT_DELETE;

@ExtendWith(MockitoExtension.class)
class RequestForReconsiderationRequestedByOtherPartyDefendantNotificationHandlerTest extends BaseCallbackHandlerTest {

    @InjectMocks
    private RequestForReconsiderationRequestedByOtherPartyDefendantNotificationHandler handler;
    @Mock
    private DashboardScenariosService dashboardScenariosService;
    @Mock
    private FeatureToggleService featureToggleService;
    @Mock
    private DashboardNotificationsParamsMapper dashboardNotificationsParamsMapper;

    public static final String TASK_ID = "CreateNotificationRequestForReconsiderationDefendant";

    @Test
    void handleEventsReturnsTheExpectedCallbackEvent() {
        assertThat(handler.handledEvents()).contains(CREATE_NOTIFICATION_REQUEST_FOR_RECONSIDERATION_DEFENDANT);
    }

    @Test
    void shouldReturnCorrectCamundaActivityId_whenInvoked() {
        assertThat(handler.camundaActivityId(
            CallbackParamsBuilder.builder()
                .request(CallbackRequest.builder()
                             .eventId(CREATE_NOTIFICATION_REQUEST_FOR_RECONSIDERATION_DEFENDANT.name())
                             .build())
                .build()))
            .isEqualTo(TASK_ID);
    }

    @Test
    void configureDashboardNotificationsForDefendantRequest() {

        HashMap<String, Object> params = new HashMap<>();

        when(dashboardNotificationsParamsMapper.mapCaseDataToParams(any())).thenReturn(params);
        when(featureToggleService.isCaseProgressionEnabled()).thenReturn(true);

        CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued().build()
            .toBuilder().respondent1Represented(YesOrNo.NO)
            .build();

        CallbackParams callbackParams = CallbackParamsBuilder.builder()
            .of(ABOUT_TO_SUBMIT, caseData)
            .build();

        handler.handle(callbackParams);

        verify(dashboardScenariosService).recordScenarios(
            "BEARER_TOKEN",
            SCENARIO_AAA6_CP_REQUEST_FOR_RECONSIDERATION_REQUESTED_BY_OTHER_PARTY_DEFENDANT.getScenario(),
            caseData.getCcdCaseReference().toString(),
            ScenarioRequestParams.builder().params(params).build()
        );
    }

    @Test
    void configureDashboardNotificationsForDefendantRequestOtherPartyLR() {

        HashMap<String, Object> params = new HashMap<>();

        when(dashboardNotificationsParamsMapper.mapCaseDataToParams(any())).thenReturn(params);
        when(featureToggleService.isCaseProgressionEnabled()).thenReturn(true);

        CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued().build()
            .toBuilder().respondent1Represented(YesOrNo.YES)
            .build();

        CallbackParams callbackParams = CallbackParamsBuilder.builder()
            .of(ABOUT_TO_SUBMIT, caseData)
            .build();

        handler.handle(callbackParams);

        verify(dashboardScenariosService).recordScenarios(
            "BEARER_TOKEN",
            SCENARIO_AAA6_CP_REQUEST_FOR_RECONSIDERATION_REQUESTED_BY_OTHER_PARTY_DELETE.getScenario(),
            caseData.getCcdCaseReference().toString(),
            ScenarioRequestParams.builder().params(params).build()
        );
    }

    @Test
    void configureDashboardNotificationsForDefendantRequestOtherPartyLRSecondRequest() {

        HashMap<String, Object> params = new HashMap<>();

        when(dashboardNotificationsParamsMapper.mapCaseDataToParams(any())).thenReturn(params);
        when(featureToggleService.isCaseProgressionEnabled()).thenReturn(true);

        CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued().build()
            .toBuilder().respondent1Represented(YesOrNo.YES)
            .orderRequestedForReviewDefendant(YesOrNo.YES)
            .orderRequestedForReviewClaimant(YesOrNo.YES)
            .build();

        CallbackParams callbackParams = CallbackParamsBuilder.builder()
            .of(ABOUT_TO_SUBMIT, caseData)
            .build();

        handler.handle(callbackParams);

        verify(dashboardScenariosService).recordScenarios(
            "BEARER_TOKEN",
            SCENARIO_AAA6_CP_REQUEST_FOR_RECONSIDERATION_REQUESTED_BY_OTHER_PARTY_LR_RECIPIENT_DELETE.getScenario(),
            caseData.getCcdCaseReference().toString(),
            ScenarioRequestParams.builder().params(params).build()
        );
    }

    @Test
    void configureDashboardNotificationsForDefendantRequestSecondRequest() {

        HashMap<String, Object> params = new HashMap<>();

        when(dashboardNotificationsParamsMapper.mapCaseDataToParams(any())).thenReturn(params);
        when(featureToggleService.isCaseProgressionEnabled()).thenReturn(true);

        CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued().build()
            .toBuilder().respondent1Represented(YesOrNo.NO)
            .orderRequestedForReviewDefendant(YesOrNo.YES)
            .orderRequestedForReviewClaimant(YesOrNo.YES)
            .build();

        CallbackParams callbackParams = CallbackParamsBuilder.builder()
            .of(ABOUT_TO_SUBMIT, caseData)
            .build();

        handler.handle(callbackParams);

        verify(dashboardScenariosService).recordScenarios(
            "BEARER_TOKEN",
            SCENARIO_AAA6_CP_REQUEST_FOR_RECONSIDERATION_REQUESTED_BY_OTHER_PARTY_DEFENDANT_RECIPIENT.getScenario(),
            caseData.getCcdCaseReference().toString(),
            ScenarioRequestParams.builder().params(params).build()
        );
    }
}
