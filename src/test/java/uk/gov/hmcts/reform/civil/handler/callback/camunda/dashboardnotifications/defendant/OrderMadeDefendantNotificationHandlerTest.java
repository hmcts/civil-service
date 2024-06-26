package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.defendant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.civil.bankholidays.WorkingDayIndicator;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.client.DashboardApiClient;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Mediation;
import uk.gov.hmcts.reform.civil.model.mediation.MediationDocumentsType;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.utils.ElementUtils;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_DASHBOARD_NOTIFICATION_DJ_SDO_DEFENDANT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_DASHBOARD_NOTIFICATION_FINAL_ORDER_DEFENDANT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_DASHBOARD_NOTIFICATION_SDO_DEFENDANT;
import static uk.gov.hmcts.reform.civil.enums.AllocatedTrack.FAST_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.mediation.MediationUnsuccessfulReason.NOT_CONTACTABLE_CLAIMANT_ONE;
import static uk.gov.hmcts.reform.civil.enums.mediation.MediationUnsuccessfulReason.NOT_CONTACTABLE_DEFENDANT_ONE;
import static uk.gov.hmcts.reform.civil.model.mediation.MediationDocumentsType.NON_ATTENDANCE_STATEMENT;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {
    OrderMadeDefendantNotificationHandler.class,
    DashboardApiClient.class,
    JacksonAutoConfiguration.class,
    ValidationAutoConfiguration.class
})
public class OrderMadeDefendantNotificationHandlerTest extends BaseCallbackHandlerTest {

    @Autowired
    private OrderMadeDefendantNotificationHandler handler;
    @MockBean
    private DashboardApiClient dashboardApiClient;
    @MockBean
    private DashboardNotificationsParamsMapper mapper;
    @MockBean
    private FeatureToggleService toggleService;
    @MockBean
    private WorkingDayIndicator workingDayIndicator;

    public static final String TASK_ID = "GenerateDashboardNotificationFinalOrderDefendant";

    private static final List<MediationDocumentsType> MEDIATION_NON_ATTENDANCE_OPTION = List.of(NON_ATTENDANCE_STATEMENT);

    @Test
    void handleEventsReturnsTheExpectedCallbackEvent() {
        assertThat(handler.handledEvents()).contains(CREATE_DASHBOARD_NOTIFICATION_FINAL_ORDER_DEFENDANT,
                                                     CREATE_DASHBOARD_NOTIFICATION_DJ_SDO_DEFENDANT,
                                                     CREATE_DASHBOARD_NOTIFICATION_SDO_DEFENDANT);
    }

    @Test
    void shouldReturnCorrectCamundaActivityId_whenInvoked() {
        assertThat(handler.camundaActivityId(
            CallbackParamsBuilder.builder()
                .request(CallbackRequest.builder()
                             .eventId(CREATE_DASHBOARD_NOTIFICATION_FINAL_ORDER_DEFENDANT.name())
                             .build())
                .build()))
            .isEqualTo(TASK_ID);
    }

    @Nested
    class AboutToSubmitCallback {
        @BeforeEach
        void setup() {
            when(dashboardApiClient.recordScenario(any(), any(), anyString(), any())).thenReturn(ResponseEntity.of(
                Optional.empty()));
            when(toggleService.isLipVLipEnabled()).thenReturn(true);
        }

        @Test
        void shouldRecordScenario_whenInvoked() {
            CaseData caseData = CaseDataBuilder.builder().atStateTrialReadyCheck().build().toBuilder()
                .finalOrderDocumentCollection(List.of(ElementUtils.element(CaseDocument.builder().documentLink(Document.builder().documentBinaryUrl("url").build()).build())))
                .respondent1Represented(YesOrNo.NO).build();

            HashMap<String, Object> scenarioParams = new HashMap<>();
            scenarioParams.put("orderDocument", "url");

            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(CREATE_DASHBOARD_NOTIFICATION_FINAL_ORDER_DEFENDANT.name()).build()
            ).build();

            when(mapper.mapCaseDataToParams(any(), any())).thenReturn(scenarioParams);
            when(toggleService.isCaseProgressionEnabled()).thenReturn(true);

            handler.handle(params);

            verify(dashboardApiClient).recordScenario(
                caseData.getCcdCaseReference().toString(),
                "Scenario.AAA6.CP.OrderMade.Defendant",
                "BEARER_TOKEN",
                ScenarioRequestParams.builder().params(scenarioParams).build()
            );
        }

        @Test
        void shouldRecordScenarioInSdoDj_whenInvoked() {
            CaseData caseData = CaseDataBuilder.builder().atStateTrialReadyCheck().build().toBuilder()
                .orderSDODocumentDJCollection(List.of(
                    ElementUtils.element(CaseDocument.builder().documentLink(
                        Document.builder().documentBinaryUrl("urlDirectionsOrder").build()).build())))
                .respondent1Represented(YesOrNo.NO).build();

            HashMap<String, Object> scenarioParams = new HashMap<>();
            scenarioParams.put("orderDocument", "urlDirectionsOrder");

            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(CREATE_DASHBOARD_NOTIFICATION_DJ_SDO_DEFENDANT.name()).build()
            ).build();

            when(mapper.mapCaseDataToParams(any(), any())).thenReturn(scenarioParams);
            when(toggleService.isCaseProgressionEnabled()).thenReturn(true);

            handler.handle(params);

            verify(dashboardApiClient).recordScenario(
                caseData.getCcdCaseReference().toString(),
                "Scenario.AAA6.CP.OrderMade.Defendant",
                "BEARER_TOKEN",
                ScenarioRequestParams.builder().params(scenarioParams).build()
            );
        }

        @Test
        void shouldRecordScenarioInSDO_whenInvoked() {
            CaseData caseData = CaseDataBuilder.builder().atStateTrialReadyCheck().build().toBuilder()
                .orderSDODocumentDJCollection(List.of(
                    ElementUtils.element(CaseDocument.builder().documentLink(
                        Document.builder().documentBinaryUrl("urlDirectionsOrder").build()).build())))
                .respondent1Represented(YesOrNo.NO).build();

            HashMap<String, Object> scenarioParams = new HashMap<>();
            scenarioParams.put("orderDocument", "urlDirectionsOrder");

            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(CREATE_DASHBOARD_NOTIFICATION_SDO_DEFENDANT.name()).build()
            ).build();

            when(mapper.mapCaseDataToParams(any(), any())).thenReturn(scenarioParams);
            when(toggleService.isCaseProgressionEnabled()).thenReturn(true);

            handler.handle(params);

            verify(dashboardApiClient).recordScenario(
                caseData.getCcdCaseReference().toString(),
                "Scenario.AAA6.CP.OrderMade.Defendant",
                "BEARER_TOKEN",
                ScenarioRequestParams.builder().params(scenarioParams).build()
            );
        }

        @Test
        void shouldRecordScenarioInSDO_whenInvokedMediationUnsuccessfulCarmWithoutUploadDocuments() {
            CaseData caseData = CaseDataBuilder.builder().atStateTrialReadyCheck().build().toBuilder()
                .orderSDODocumentDJCollection(List.of(
                    ElementUtils.element(CaseDocument.builder().documentLink(
                        Document.builder().documentBinaryUrl("urlDirectionsOrder").build()).build())))
                .respondent1Represented(YesOrNo.NO)
                .responseClaimTrack(FAST_CLAIM.name())
                .totalClaimAmount(BigDecimal.valueOf(999))
                .mediation(Mediation.builder()
                               .mediationUnsuccessfulReasonsMultiSelect(List.of(NOT_CONTACTABLE_CLAIMANT_ONE, NOT_CONTACTABLE_DEFENDANT_ONE))
                               .build())
                .build();

            HashMap<String, Object> scenarioParams = new HashMap<>();
            scenarioParams.put("orderDocument", "urlDirectionsOrder");

            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(CREATE_DASHBOARD_NOTIFICATION_SDO_DEFENDANT.name()).build()
            ).build();

            when(mapper.mapCaseDataToParams(any(), any())).thenReturn(scenarioParams);
            when(toggleService.isCarmEnabledForCase(any())).thenReturn(true);

            handler.handle(params);

            verify(dashboardApiClient).recordScenario(
                caseData.getCcdCaseReference().toString(),
                "Scenario.AAA6.MediationUnsuccessfulWithoutUploadDocuments.TrackChange.CARM.Defendant",
                "BEARER_TOKEN",
                ScenarioRequestParams.builder().params(scenarioParams).build()
            );
        }

        @Test
        void shouldRecordScenarioInSDO_whenInvokedMediationUnsuccessfulCarmWithUploadDocuments() {
            CaseData caseData = CaseDataBuilder.builder().atStateTrialReadyCheck()
                .uploadMediationByDocumentTypes(MEDIATION_NON_ATTENDANCE_OPTION)
                .build().toBuilder()
                .orderSDODocumentDJCollection(List.of(
                    ElementUtils.element(CaseDocument.builder().documentLink(
                        Document.builder().documentBinaryUrl("urlDirectionsOrder").build()).build())))
                .respondent1Represented(YesOrNo.NO)
                .responseClaimTrack(FAST_CLAIM.name())
                .totalClaimAmount(BigDecimal.valueOf(999))
                .mediation(Mediation.builder()
                               .mediationUnsuccessfulReasonsMultiSelect(List.of(NOT_CONTACTABLE_CLAIMANT_ONE, NOT_CONTACTABLE_DEFENDANT_ONE))
                               .build())
                .build();

            HashMap<String, Object> scenarioParams = new HashMap<>();
            scenarioParams.put("orderDocument", "urlDirectionsOrder");

            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(CREATE_DASHBOARD_NOTIFICATION_SDO_DEFENDANT.name()).build()
            ).build();

            when(mapper.mapCaseDataToParams(any(), any())).thenReturn(scenarioParams);
            when(toggleService.isCarmEnabledForCase(any())).thenReturn(true);

            handler.handle(params);

            verify(dashboardApiClient).recordScenario(
                caseData.getCcdCaseReference().toString(),
                "Scenario.AAA6.MediationUnsuccessful.TrackChange.CARM.Defendant",
                "BEARER_TOKEN",
                ScenarioRequestParams.builder().params(scenarioParams).build()
            );
        }

        @Test
        void shouldNotRecordScenarioInDJSdo_whenInvokedMediationUnsuccessfulCarmWithoutUploadDocuments() {
            CaseData caseData = CaseDataBuilder.builder().atStateTrialReadyCheck().build().toBuilder()
                .orderSDODocumentDJCollection(List.of(
                    ElementUtils.element(CaseDocument.builder().documentLink(
                        Document.builder().documentBinaryUrl("urlDirectionsOrder").build()).build())))
                .respondent1Represented(YesOrNo.NO)
                .responseClaimTrack(FAST_CLAIM.name())
                .totalClaimAmount(BigDecimal.valueOf(999))
                .mediation(Mediation.builder()
                               .mediationUnsuccessfulReasonsMultiSelect(List.of(NOT_CONTACTABLE_CLAIMANT_ONE, NOT_CONTACTABLE_DEFENDANT_ONE))
                               .build())
                .build();

            HashMap<String, Object> scenarioParams = new HashMap<>();
            scenarioParams.put("orderDocument", "urlDirectionsOrder");

            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(CREATE_DASHBOARD_NOTIFICATION_DJ_SDO_DEFENDANT.name()).build()
            ).build();

            when(mapper.mapCaseDataToParams(any(), any())).thenReturn(scenarioParams);
            when(toggleService.isCarmEnabledForCase(any())).thenReturn(true);

            handler.handle(params);

            verify(dashboardApiClient, never()).recordScenario(
                caseData.getCcdCaseReference().toString(),
                "Scenario.AAA6.MediationUnsuccessfulWithoutUploadDocuments.TrackChange.CARM.Defendant",
                "BEARER_TOKEN",
                ScenarioRequestParams.builder().params(scenarioParams).build()
            );
        }

        @Test
        void shouldNotRecordScenarioInDJSdo_whenInvokedMediationUnsuccessfulCarmWithUploadDocuments() {
            CaseData caseData = CaseDataBuilder.builder().atStateTrialReadyCheck()
                .uploadMediationByDocumentTypes(MEDIATION_NON_ATTENDANCE_OPTION)
                .build().toBuilder()
                .orderSDODocumentDJCollection(List.of(
                    ElementUtils.element(CaseDocument.builder().documentLink(
                        Document.builder().documentBinaryUrl("urlDirectionsOrder").build()).build())))
                .respondent1Represented(YesOrNo.NO)
                .responseClaimTrack(FAST_CLAIM.name())
                .totalClaimAmount(BigDecimal.valueOf(999))
                .mediation(Mediation.builder()
                               .mediationUnsuccessfulReasonsMultiSelect(List.of(NOT_CONTACTABLE_CLAIMANT_ONE, NOT_CONTACTABLE_DEFENDANT_ONE))
                               .build())
                .build();

            HashMap<String, Object> scenarioParams = new HashMap<>();
            scenarioParams.put("orderDocument", "urlDirectionsOrder");

            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(CREATE_DASHBOARD_NOTIFICATION_DJ_SDO_DEFENDANT.name()).build()
            ).build();

            when(mapper.mapCaseDataToParams(any(), any())).thenReturn(scenarioParams);
            when(toggleService.isCarmEnabledForCase(any())).thenReturn(true);

            handler.handle(params);

            verify(dashboardApiClient, never()).recordScenario(
                caseData.getCcdCaseReference().toString(),
                "Scenario.AAA6.MediationUnsuccessful.TrackChange.CARM.Defendant",
                "BEARER_TOKEN",
                ScenarioRequestParams.builder().params(scenarioParams).build()
            );
        }

        @Test
        void shouldNotRecordScenarioInSDO_whenInvokedMediationUnsuccessfulCarmFastClaim() {
            CaseData caseData = CaseDataBuilder.builder().atStateTrialReadyCheck().build().toBuilder()
                .orderSDODocumentDJCollection(List.of(
                    ElementUtils.element(CaseDocument.builder().documentLink(
                        Document.builder().documentBinaryUrl("urlDirectionsOrder").build()).build())))
                .respondent1Represented(YesOrNo.NO)
                .responseClaimTrack(FAST_CLAIM.name())
                .totalClaimAmount(BigDecimal.valueOf(11111111))
                .mediation(Mediation.builder()
                               .mediationUnsuccessfulReasonsMultiSelect(List.of(NOT_CONTACTABLE_CLAIMANT_ONE, NOT_CONTACTABLE_DEFENDANT_ONE))
                               .build())
                .build();

            HashMap<String, Object> scenarioParams = new HashMap<>();
            scenarioParams.put("orderDocument", "urlDirectionsOrder");

            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(CREATE_DASHBOARD_NOTIFICATION_SDO_DEFENDANT.name()).build()
            ).build();

            when(mapper.mapCaseDataToParams(any(), any())).thenReturn(scenarioParams);
            when(toggleService.isCarmEnabledForCase(any())).thenReturn(true);

            handler.handle(params);

            verify(dashboardApiClient, never()).recordScenario(
                caseData.getCcdCaseReference().toString(),
                "Scenario.AAA6.MediationUnsuccessful.TrackChange.CARM.Defendant",
                "BEARER_TOKEN",
                ScenarioRequestParams.builder().params(scenarioParams).build()
            );
        }

        @Test
        void shouldRecordScenarioInSDOPreCPRelease_whenInvoked() {
            CaseData caseData = CaseDataBuilder.builder().atStateTrialReadyCheck().build().toBuilder()
                .orderSDODocumentDJCollection(List.of(
                    ElementUtils.element(CaseDocument.builder().documentLink(
                        Document.builder().documentBinaryUrl("urlDirectionsOrder").build()).build())))
                .respondent1Represented(YesOrNo.NO).build();

            HashMap<String, Object> scenarioParams = new HashMap<>();
            scenarioParams.put("orderDocument", "urlDirectionsOrder");
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(CREATE_DASHBOARD_NOTIFICATION_SDO_DEFENDANT.name()).build()
            ).build();

            when(mapper.mapCaseDataToParams(any(), any())).thenReturn(scenarioParams);
            when(toggleService.isCaseProgressionEnabled()).thenReturn(false);

            handler.handle(params);

            verify(dashboardApiClient).recordScenario(
                caseData.getCcdCaseReference().toString(),
                "Scenario.AAA6.ClaimantIntent.SDODrawn.PreCaseProgression.Defendant",
                "BEARER_TOKEN",
                ScenarioRequestParams.builder().params(scenarioParams).build()
            );
        }

        @Test
        void shouldRecordScenarioInSdoLegalAdviser_whenInvoked() {
            HashMap<String, Object> scenarioParams = new HashMap<>();
            scenarioParams.put("orderDocument", "urlDirectionsOrder");

            when(mapper.mapCaseDataToParams(any(), any())).thenReturn(scenarioParams);
            when(toggleService.isCaseProgressionEnabled()).thenReturn(true);
            when(workingDayIndicator.isPublicHoliday(LocalDate.now().plusDays(1))).thenReturn(true);

            CaseData caseData = CaseDataBuilder.builder().atStateTrialReadyCheck().build().toBuilder()
                .responseClaimTrack("SMALL_CLAIM")
                .totalClaimAmount(BigDecimal.valueOf(500))
                .respondent1Represented(YesOrNo.NO).build();

            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(CREATE_DASHBOARD_NOTIFICATION_SDO_DEFENDANT.name()).build()
            ).build();
            var response = (AboutToStartOrSubmitCallbackResponse)  handler.handle(params);

            verify(dashboardApiClient).recordScenario(
                caseData.getCcdCaseReference().toString(),
                "Scenario.AAA6.CP.SDOMadebyLA.Defendant",
                "BEARER_TOKEN",
                ScenarioRequestParams.builder().params(scenarioParams).build()
            );
            LocalDate now = LocalDate.now().plusDays(8);
            LocalDateTime dateTime = LocalDateTime.of(now, LocalTime.of(16, 0));
            assertThat(response.getData()).extracting("requestForReconsiderationDeadline")
                .isEqualTo(dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")));
        }
    }
}
