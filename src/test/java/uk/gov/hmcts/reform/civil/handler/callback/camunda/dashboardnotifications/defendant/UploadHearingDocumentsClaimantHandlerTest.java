package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.defendant;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.sdo.ClaimsTrack;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.claimant.UploadHearingDocumentsClaimantHandler;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.CaseLocationCivil;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;

import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_DASHBOARD_NOTIFICATION_UPLOAD_HEARING_DOCUMENTS_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CP_HEARING_DOCUMENTS_UPLOAD_CLAIMANT;

@ExtendWith(MockitoExtension.class)
class UploadHearingDocumentsClaimantHandlerTest extends BaseCallbackHandlerTest {

    @InjectMocks
    private UploadHearingDocumentsClaimantHandler handler;

    @Mock
    private DashboardScenariosService dashboardScenariosService;

    @Mock
    private DashboardNotificationsParamsMapper dashboardNotificationsParamsMapper;

    @Mock
    private FeatureToggleService featureToggleService;

    public static final String TASK_ID = "CreateUploadHearingDocumentNotificationForClaimant";

    HashMap<String, Object> params = new HashMap<>();

    @Test
    void handleEventsReturnsTheExpectedCallbackEvent() {
        assertThat(handler.handledEvents()).contains(CREATE_DASHBOARD_NOTIFICATION_UPLOAD_HEARING_DOCUMENTS_CLAIMANT);
    }

    @Test
    void shouldReturnCorrectCamundaActivityId_whenInvoked() {
        assertThat(handler.camundaActivityId(
                CallbackParamsBuilder.builder()
                        .request(CallbackRequest.builder()
                                .eventId(CREATE_DASHBOARD_NOTIFICATION_UPLOAD_HEARING_DOCUMENTS_CLAIMANT.name())
                                .build())
                        .build()))
                .isEqualTo(TASK_ID);
    }

    @Test
    void createDashboardNotifications() {

        params.put("ccdCaseReference", "1239988");
        when(featureToggleService.isCaseProgressionEnabled()).thenReturn(true);
        // when(featureToggleService.isCaseProgressionEnabledAndLocationWhiteListed(any())).thenReturn(true);
        when(dashboardNotificationsParamsMapper.mapCaseDataToParams(any())).thenReturn(params);

        DynamicListElement selectedCourt = DynamicListElement.builder()
                .code("00002").label("court 2 - 2 address - Y02 7RB").build();

        CaseData caseData = CaseData.builder()
                .legacyCaseReference("reference")
                .applicant1Represented(YesOrNo.NO)
                .ccdCaseReference(12349988L)
                .drawDirectionsOrderRequired(YesOrNo.NO)
                .claimsTrack(ClaimsTrack.fastTrack)
                .ccdState(CaseState.CASE_PROGRESSION)
                .caseManagementLocation(CaseLocationCivil.builder().baseLocation(selectedCourt.getCode()).build())
                .build();

        CallbackParams callbackParams = CallbackParamsBuilder.builder()
                .of(ABOUT_TO_SUBMIT, caseData)
                .build();

        handler.handle(callbackParams);
        verify(dashboardScenariosService).recordScenarios(
                "BEARER_TOKEN",
                SCENARIO_AAA6_CP_HEARING_DOCUMENTS_UPLOAD_CLAIMANT.getScenario(),
                caseData.getCcdCaseReference().toString(),
                ScenarioRequestParams.builder().params(params).build());
    }

    @Test
    void createDashboardNotificationsAfterNroChangesAndWelshEnabledForMainCase() {

        params.put("ccdCaseReference", "1239988");
        when(featureToggleService.isCaseProgressionEnabled()).thenReturn(true);
        // when(featureToggleService.isCaseProgressionEnabledAndLocationWhiteListed(any())).thenReturn(false);
        when(dashboardNotificationsParamsMapper.mapCaseDataToParams(any())).thenReturn(params);

        DynamicListElement selectedCourt = DynamicListElement.builder()
                .code("00002").label("court 2 - 2 address - Y02 7RB").build();

        CaseData caseData = CaseData.builder()
                .legacyCaseReference("reference")
                .applicant1Represented(YesOrNo.NO)
                .ccdCaseReference(12349988L)
                .drawDirectionsOrderRequired(YesOrNo.NO)
                .claimsTrack(ClaimsTrack.fastTrack)
                .ccdState(CaseState.CASE_PROGRESSION)
                .caseManagementLocation(CaseLocationCivil.builder().baseLocation(selectedCourt.getCode()).build())
                .build();

        CallbackParams callbackParams = CallbackParamsBuilder.builder()
                .of(ABOUT_TO_SUBMIT, caseData)
                .build();

        handler.handle(callbackParams);
        verify(dashboardScenariosService).recordScenarios(
                "BEARER_TOKEN",
                SCENARIO_AAA6_CP_HEARING_DOCUMENTS_UPLOAD_CLAIMANT.getScenario(),
                caseData.getCcdCaseReference().toString(),
                ScenarioRequestParams.builder().params(params).build());
    }

    // @Test
    // void
    // createDashboardNotificationsAfterNroChangesAndWelshNotEnabledForMainCase() {
    //
    // params.put("ccdCaseReference", "1239988");
    // when(featureToggleService.isCaseProgressionEnabled()).thenReturn(true);
    // when(featureToggleService.isCaseProgressionEnabledAndLocationWhiteListed(any())).thenReturn(false);
    // when(dashboardNotificationsParamsMapper.mapCaseDataToParams(any())).thenReturn(params);
    //
    // DynamicListElement selectedCourt = DynamicListElement.builder()
    // .code("00002").label("court 2 - 2 address - Y02 7RB").build();
    //
    // CaseData caseData = CaseData.builder()
    // .legacyCaseReference("reference")
    // .applicant1Represented(YesOrNo.NO)
    // .ccdCaseReference(12349988L)
    // .drawDirectionsOrderRequired(YesOrNo.NO)
    // .claimsTrack(ClaimsTrack.fastTrack)
    // .ccdState(CaseState.CASE_PROGRESSION)
    // .caseManagementLocation(CaseLocationCivil.builder().baseLocation(selectedCourt.getCode()).build())
    // .build();
    //
    // CallbackParams callbackParams = CallbackParamsBuilder.builder()
    // .of(ABOUT_TO_SUBMIT, caseData)
    // .build();
    //
    // handler.handle(callbackParams);
    // verifyNoInteractions(dashboardScenariosService);
    // }
}
