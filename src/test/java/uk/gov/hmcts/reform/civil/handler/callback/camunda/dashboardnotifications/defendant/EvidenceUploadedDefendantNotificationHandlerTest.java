package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.defendant;

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
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
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
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_DASHBOARD_NOTIFICATION_EVIDENCE_UPLOADED_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CP_HEARING_DOCUMENTS_NOT_UPLOADED_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CP_HEARING_DOCUMENTS_UPLOADED_DEFENDANT;

@ExtendWith(MockitoExtension.class)
class EvidenceUploadedDefendantNotificationHandlerTest extends BaseCallbackHandlerTest {

    @Mock
    private DashboardScenariosService dashboardScenariosService;
    @Mock
    private DashboardNotificationsParamsMapper mapper;
    @Mock
    private FeatureToggleService toggleService;
    @InjectMocks
    private EvidenceUploadedDefendantNotificationHandler handler;

    public static final String TASK_ID = "GenerateDashboardNotificationEvidenceUploadedDefendant";

    @Test
    void handleEventsReturnsTheExpectedCallbackEvent() {
        assertThat(handler.handledEvents()).contains(CREATE_DASHBOARD_NOTIFICATION_EVIDENCE_UPLOADED_DEFENDANT);
    }

    @Test
    void shouldReturnCorrectCamundaActivityId_whenInvoked() {
        assertThat(handler.camundaActivityId(
            CallbackParamsBuilder.builder()
                .request(CallbackRequest.builder()
                             .eventId(CREATE_DASHBOARD_NOTIFICATION_EVIDENCE_UPLOADED_DEFENDANT.name())
                             .build())
                .build()))
            .isEqualTo(TASK_ID);
    }

    @Nested
    class AboutToSubmitCallback {

        @Test
        void shouldRecordScenario_whenInvoked_whenClaimantUploadsDocument() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimSubmittedSmallClaim()
                .caseDataLip(CaseDataLiP.builder().applicant1SettleClaim(YesOrNo.YES)
                                 .applicant1ClaimSettledDate(
                                     LocalDate.now()).build())
                .respondent1Represented(YesOrNo.NO).build();

            HashMap<String, Object> scenarioParams = new HashMap<>();
            when(mapper.mapCaseDataToParams(any())).thenReturn(scenarioParams);
            when(toggleService.isLipVLipEnabled()).thenReturn(true);

            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(CREATE_DASHBOARD_NOTIFICATION_EVIDENCE_UPLOADED_DEFENDANT.name()).build()
            ).build();

            handler.handle(params);
            verify(dashboardScenariosService).recordScenarios(
                "BEARER_TOKEN",
                SCENARIO_AAA6_CP_HEARING_DOCUMENTS_NOT_UPLOADED_DEFENDANT.getScenario(),
                caseData.getCcdCaseReference().toString(),
                ScenarioRequestParams.builder().params(scenarioParams).build()
            );
        }

        @Test
        void shouldRecordScenario_whenInvoked_whenDefendantUploadsDocument() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimSubmittedSmallClaim()
                .caseDataLip(CaseDataLiP.builder().applicant1SettleClaim(YesOrNo.YES)
                                 .applicant1ClaimSettledDate(
                                     LocalDate.now()).build())
                .respondent1Represented(YesOrNo.NO).build().toBuilder().caseDocumentUploadDateRes(LocalDateTime.now()).build();

            HashMap<String, Object> scenarioParams = new HashMap<>();
            when(mapper.mapCaseDataToParams(any())).thenReturn(scenarioParams);
            when(toggleService.isLipVLipEnabled()).thenReturn(true);

            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(CREATE_DASHBOARD_NOTIFICATION_EVIDENCE_UPLOADED_DEFENDANT.name()).build()
            ).build();

            handler.handle(params);
            verify(dashboardScenariosService).recordScenarios(
                "BEARER_TOKEN",
                SCENARIO_AAA6_CP_HEARING_DOCUMENTS_UPLOADED_DEFENDANT.getScenario(),
                caseData.getCcdCaseReference().toString(),
                ScenarioRequestParams.builder().params(scenarioParams).build()
            );
        }
    }

}
