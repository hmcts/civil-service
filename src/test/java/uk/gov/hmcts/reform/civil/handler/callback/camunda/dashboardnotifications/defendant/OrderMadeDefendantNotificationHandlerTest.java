package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.defendant;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.bankholidays.WorkingDayIndicator;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.enums.DecisionOnRequestReconsiderationOptions;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.sdo.ClaimsTrack;
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
import uk.gov.hmcts.reform.dashboard.services.DashboardNotificationService;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;
import uk.gov.hmcts.reform.dashboard.services.TaskListService;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_DASHBOARD_NOTIFICATION_DJ_SDO_DEFENDANT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_DASHBOARD_NOTIFICATION_FINAL_ORDER_DEFENDANT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_DASHBOARD_NOTIFICATION_SDO_DEFENDANT;
import static uk.gov.hmcts.reform.civil.enums.AllocatedTrack.FAST_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.CaseState.All_FINAL_ORDERS_ISSUED;
import static uk.gov.hmcts.reform.civil.enums.CaseState.PREPARE_FOR_HEARING_CONDUCT_HEARING;
import static uk.gov.hmcts.reform.civil.enums.mediation.MediationUnsuccessfulReason.NOT_CONTACTABLE_CLAIMANT_ONE;
import static uk.gov.hmcts.reform.civil.enums.mediation.MediationUnsuccessfulReason.NOT_CONTACTABLE_DEFENDANT_ONE;
import static uk.gov.hmcts.reform.civil.model.mediation.MediationDocumentsType.NON_ATTENDANCE_STATEMENT;

@ExtendWith(MockitoExtension.class)
class OrderMadeDefendantNotificationHandlerTest extends BaseCallbackHandlerTest {

    @InjectMocks
    private OrderMadeDefendantNotificationHandler handler;

    @Mock
    private DashboardScenariosService dashboardScenariosService;
    @Mock
    private DashboardNotificationService dashboardNotificationService;
    @Mock
    private TaskListService taskListService;

    @Mock
    private DashboardNotificationsParamsMapper mapper;

    @Mock
    private FeatureToggleService toggleService;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private WorkingDayIndicator workingDayIndicator;

    public static final String TASK_ID = "GenerateDashboardNotificationFinalOrderDefendant";
    private static final List<MediationDocumentsType> MEDIATION_NON_ATTENDANCE_OPTION = List.of(NON_ATTENDANCE_STATEMENT);

    @Test
    void handleEventsReturnsTheExpectedCallbackEvent() {
        assertThat(handler.handledEvents()).contains(
            CREATE_DASHBOARD_NOTIFICATION_FINAL_ORDER_DEFENDANT,
            CREATE_DASHBOARD_NOTIFICATION_DJ_SDO_DEFENDANT,
            CREATE_DASHBOARD_NOTIFICATION_SDO_DEFENDANT
        );
    }

    @Test
    void shouldReturnCorrectCamundaActivityId_whenInvoked() {
        CallbackParams params = CallbackParamsBuilder.builder()
            .request(CallbackRequest.builder().eventId(CREATE_DASHBOARD_NOTIFICATION_FINAL_ORDER_DEFENDANT.name()).build())
            .build();

        assertThat(handler.camundaActivityId(params)).isEqualTo(TASK_ID);
    }

    @Nested
    class AboutToSubmitCallback {

        @Test
        void shouldRecordScenario_whenInvoked() {
            CaseData caseData = CaseDataBuilder.builder().atStateTrialReadyCheck().build().toBuilder()
                .finalOrderDocumentCollection(List.of(ElementUtils.element(CaseDocument.builder().documentLink(Document.builder().documentBinaryUrl(
                    "url").build()).build())))
                .respondent1Represented(YesOrNo.NO)
                .decisionOnRequestReconsiderationOptions(DecisionOnRequestReconsiderationOptions.CREATE_SDO)
                .build();

            HashMap<String, Object> scenarioParams = new HashMap<>();
            scenarioParams.put("orderDocument", "url");

            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(CREATE_DASHBOARD_NOTIFICATION_FINAL_ORDER_DEFENDANT.name())
                    .caseDetails(CaseDetails.builder().state(PREPARE_FOR_HEARING_CONDUCT_HEARING.toString()).build()).build()).build();

            when(mapper.mapCaseDataToParams(any(), any())).thenReturn(scenarioParams);
            when(toggleService.isCaseProgressionEnabledAndLocationWhiteListed(any())).thenReturn(true);

            handler.handle(params);

            verify(dashboardScenariosService).recordScenarios(
                "BEARER_TOKEN",
                "Scenario.AAA6.CP.OrderMade.Defendant",
                caseData.getCcdCaseReference().toString(),
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
                CallbackRequest.builder().eventId(CREATE_DASHBOARD_NOTIFICATION_DJ_SDO_DEFENDANT.name())
                    .caseDetails(CaseDetails.builder().state(PREPARE_FOR_HEARING_CONDUCT_HEARING.toString()).build()).build()).build();

            when(mapper.mapCaseDataToParams(any(), any())).thenReturn(scenarioParams);
            when(toggleService.isCaseProgressionEnabledAndLocationWhiteListed(any())).thenReturn(true);

            handler.handle(params);
            verify(dashboardScenariosService).recordScenarios(
                "BEARER_TOKEN",
                "Scenario.AAA6.CP.OrderMade.Defendant",
                caseData.getCcdCaseReference().toString(),
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
                CallbackRequest.builder().eventId(CREATE_DASHBOARD_NOTIFICATION_SDO_DEFENDANT.name())
                    .caseDetails(CaseDetails.builder().state(PREPARE_FOR_HEARING_CONDUCT_HEARING.toString()).build()).build()).build();

            when(mapper.mapCaseDataToParams(any(), any())).thenReturn(scenarioParams);
            when(toggleService.isCaseProgressionEnabledAndLocationWhiteListed(any())).thenReturn(true);

            handler.handle(params);
            verify(dashboardScenariosService).recordScenarios(
                "BEARER_TOKEN",
                "Scenario.AAA6.CP.OrderMade.Defendant",
                caseData.getCcdCaseReference().toString(),
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
                               .mediationUnsuccessfulReasonsMultiSelect(List.of(
                                   NOT_CONTACTABLE_CLAIMANT_ONE,
                                   NOT_CONTACTABLE_DEFENDANT_ONE
                               ))
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
            verify(dashboardScenariosService).recordScenarios(
                "BEARER_TOKEN",
                "Scenario.AAA6.MediationUnsuccessfulWithoutUploadDocuments.TrackChange.CARM.Defendant",
                caseData.getCcdCaseReference().toString(),
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
                               .mediationUnsuccessfulReasonsMultiSelect(List.of(
                                   NOT_CONTACTABLE_CLAIMANT_ONE,
                                   NOT_CONTACTABLE_DEFENDANT_ONE
                               ))
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
            verify(dashboardScenariosService).recordScenarios(
                "BEARER_TOKEN",
                "Scenario.AAA6.MediationUnsuccessful.TrackChange.CARM.Defendant",
                caseData.getCcdCaseReference().toString(),
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
                               .mediationUnsuccessfulReasonsMultiSelect(List.of(
                                   NOT_CONTACTABLE_CLAIMANT_ONE,
                                   NOT_CONTACTABLE_DEFENDANT_ONE
                               ))
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
            verify(dashboardScenariosService, never()).recordScenarios(
                "BEARER_TOKEN",
                "Scenario.AAA6.MediationUnsuccessfulWithoutUploadDocuments.TrackChange.CARM.Defendant",
                caseData.getCcdCaseReference().toString(),
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
                               .mediationUnsuccessfulReasonsMultiSelect(List.of(
                                   NOT_CONTACTABLE_CLAIMANT_ONE,
                                   NOT_CONTACTABLE_DEFENDANT_ONE
                               ))
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

            verify(dashboardScenariosService, never()).recordScenarios(
                "BEARER_TOKEN",
                "Scenario.AAA6.MediationUnsuccessful.TrackChange.CARM.Defendant",
                caseData.getCcdCaseReference().toString(),
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
                               .mediationUnsuccessfulReasonsMultiSelect(List.of(
                                   NOT_CONTACTABLE_CLAIMANT_ONE,
                                   NOT_CONTACTABLE_DEFENDANT_ONE
                               ))
                               .build())
                .build();

            HashMap<String, Object> scenarioParams = new HashMap<>();
            scenarioParams.put("orderDocument", "urlDirectionsOrder");

            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(CREATE_DASHBOARD_NOTIFICATION_SDO_DEFENDANT.name())
                    .caseDetails(CaseDetails.builder().state(PREPARE_FOR_HEARING_CONDUCT_HEARING.toString()).build()).build()).build();

            when(mapper.mapCaseDataToParams(any(), any())).thenReturn(scenarioParams);
            when(toggleService.isCarmEnabledForCase(any())).thenReturn(true);

            handler.handle(params);
            verify(dashboardScenariosService, never()).recordScenarios(
                "BEARER_TOKEN",
                "Scenario.AAA6.MediationUnsuccessful.TrackChange.CARM.Defendant",
                caseData.getCcdCaseReference().toString(),
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
            when(toggleService.isCaseProgressionEnabledAndLocationWhiteListed(any())).thenReturn(false);

            handler.handle(params);
            verify(dashboardScenariosService).recordScenarios(
                "BEARER_TOKEN",
                "Scenario.AAA6.ClaimantIntent.SDODrawn.PreCaseProgression.Defendant",
                caseData.getCcdCaseReference().toString(),
                ScenarioRequestParams.builder().params(scenarioParams).build()
            );
        }

        @Test
        void shouldRecordScenarioInSdoLegalAdviser_whenInvoked() {
            when(toggleService.isCaseProgressionEnabledAndLocationWhiteListed(any())).thenReturn(true);
            CaseData caseData = CaseDataBuilder.builder().atStateTrialReadyCheck().build().toBuilder()
                .orderSDODocumentDJCollection(List.of(
                    ElementUtils.element(CaseDocument.builder().documentLink(
                        Document.builder().documentBinaryUrl("urlDirectionsOrder").build()).build())))
                .respondent1Represented(YesOrNo.NO)
                .build();

            HashMap<String, Object> scenarioParams = new HashMap<>();
            scenarioParams.put("orderDocument", "urlDirectionsOrder");

            CallbackParams params = CallbackParamsBuilder.builder()
                .of(ABOUT_TO_SUBMIT, caseData)
                .request(CallbackRequest.builder()
                             .eventId(CREATE_DASHBOARD_NOTIFICATION_SDO_DEFENDANT.name())
                             .caseDetails(CaseDetails.builder().state(PREPARE_FOR_HEARING_CONDUCT_HEARING.toString()).build())
                             .build())
                .build();

            when(mapper.mapCaseDataToParams(any(), any())).thenReturn(scenarioParams);
            handler.handle(params);
            verify(dashboardScenariosService).recordScenarios(
                "BEARER_TOKEN",
                "Scenario.AAA6.CP.OrderMade.Defendant",
                caseData.getCcdCaseReference().toString(),
                ScenarioRequestParams.builder().params(scenarioParams).build()
            );
        }

        @Test
        void shouldNotRecordScenarioInSdoLegalAdviser_whenInvokedWithFeatureToggleDisabled() {
            HashMap<String, Object> scenarioParams = new HashMap<>();
            scenarioParams.put("orderDocument", "urlDirectionsOrder");

            when(mapper.mapCaseDataToParams(any(), any())).thenReturn(scenarioParams);
            when(toggleService.isCaseProgressionEnabledAndLocationWhiteListed(any())).thenReturn(false);

            CaseData caseData = CaseDataBuilder.builder().atStateTrialReadyCheck().build().toBuilder()
                .responseClaimTrack("SMALL_CLAIM")
                .totalClaimAmount(BigDecimal.valueOf(500))
                .respondent1Represented(YesOrNo.NO)
                .build();

            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(CREATE_DASHBOARD_NOTIFICATION_SDO_DEFENDANT.name()).build()
            ).build();
            handler.handle(params);
            ArgumentCaptor<String> secondParamCaptor = ArgumentCaptor.forClass(String.class);
            verify(dashboardScenariosService).recordScenarios(
                eq("BEARER_TOKEN"),
                secondParamCaptor.capture(),
                eq(caseData.getCcdCaseReference().toString()),
                eq(ScenarioRequestParams.builder().params(scenarioParams).build())
            );
            String capturedSecondParam = secondParamCaptor.getValue();
            Assertions.assertNotEquals("Scenario.AAA6.CP.SDOMadebyLA.Defendant", capturedSecondParam);
        }

        @ParameterizedTest
        @MethodSource("provideCsvSource")
        void shouldRecordScenarioInSdoLegalAdviser(BigDecimal totalClaimAmount,
                                                   DecisionOnRequestReconsiderationOptions decisionOnRequestReconsiderationOptions,
                                                   String expectedScenario) {
            HashMap<String, Object> scenarioParams = new HashMap<>();
            scenarioParams.put("orderDocument", "urlDirectionsOrder");

            when(mapper.mapCaseDataToParams(any(), any())).thenReturn(scenarioParams);
            when(toggleService.isCaseProgressionEnabledAndLocationWhiteListed(any())).thenReturn(true);

            CaseData caseData = CaseDataBuilder.builder().atAllFinalOrdersIssuedCheck().build().toBuilder()
                .responseClaimTrack("SMALL_CLAIM")
                .totalClaimAmount(totalClaimAmount)
                .respondent1Represented(YesOrNo.NO)
                .decisionOnRequestReconsiderationOptions(decisionOnRequestReconsiderationOptions)
                .build();

            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(CREATE_DASHBOARD_NOTIFICATION_SDO_DEFENDANT.name())
                    .caseDetails(CaseDetails.builder().state(All_FINAL_ORDERS_ISSUED.toString()).build()).build()).build();
            handler.handle(params);
            ArgumentCaptor<String> secondParamCaptor = ArgumentCaptor.forClass(String.class);
            verify(dashboardScenariosService).recordScenarios(
                eq("BEARER_TOKEN"),
                secondParamCaptor.capture(),
                eq(caseData.getCcdCaseReference().toString()),
                eq(ScenarioRequestParams.builder().params(scenarioParams).build())
            );
            String capturedSecondParam = secondParamCaptor.getValue();
            if (expectedScenario.startsWith("not ")) {
                Assertions.assertNotEquals(expectedScenario.substring(4), capturedSecondParam);
            } else {
                Assertions.assertEquals(expectedScenario, capturedSecondParam);
            }
        }

        private static Stream<Arguments> provideCsvSource() {
            return Stream.of(
                Arguments.of(BigDecimal.valueOf(500), null, "Scenario.AAA6.CP.SDOMadebyLA.Defendant"),
                Arguments.of(BigDecimal.valueOf(1000), null, "Scenario.AAA6.CP.SDOMadebyLA.Defendant"),
                Arguments.of(BigDecimal.valueOf(1000), DecisionOnRequestReconsiderationOptions.CREATE_SDO, "not Scenario.AAA6.CP.SDOMadebyLA.Defendant"),
                Arguments.of(BigDecimal.valueOf(10000), DecisionOnRequestReconsiderationOptions.CREATE_SDO, "not Scenario.AAA6.CP.SDOMadebyLA.Defendant"),
                Arguments.of(BigDecimal.valueOf(1000), DecisionOnRequestReconsiderationOptions.YES, "Scenario.AAA6.CP.SDOMadebyLA.Defendant")
            );
        }

        @Test
        void shouldRecordScenarioDefendantFinalOrder_whenInvoked() {
            CaseData caseData = CaseDataBuilder.builder().atAllFinalOrdersIssuedCheck().build().toBuilder()
                .respondent1Represented(YesOrNo.NO)
                .build();

            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(CREATE_DASHBOARD_NOTIFICATION_FINAL_ORDER_DEFENDANT.name())
                    .caseDetails(CaseDetails.builder().state(All_FINAL_ORDERS_ISSUED.toString()).build()).build()).build();

            when(toggleService.isCaseProgressionEnabledAndLocationWhiteListed(any())).thenReturn(true);
            when(toggleService.isCarmEnabledForCase(any())).thenReturn(false);

            handler.handle(params);
            HashMap<String, Object> scenarioParams = new HashMap<>();

            // Then
            verifyDeleteNotificationsAndTaskListUpdates(caseData);
            verify(dashboardScenariosService).recordScenarios(
                "BEARER_TOKEN",
                "Scenario.AAA6.Update.Defendant.TaskList.UploadDocuments.FinalOrders",
                caseData.getCcdCaseReference().toString(),
                ScenarioRequestParams.builder().params(scenarioParams).build()
            );
        }

        @Test
        void shouldRecordScenarioDefendantFinalOrderFastTrackNotReadyTrial_whenInvoked() {
            CaseData caseData = CaseDataBuilder.builder().atAllFinalOrdersIssuedCheck().build().toBuilder()
                .respondent1Represented(YesOrNo.NO)
                .claimsTrack(ClaimsTrack.fastTrack)
                .drawDirectionsOrderRequired(YesOrNo.NO)
                .build();

            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(CREATE_DASHBOARD_NOTIFICATION_FINAL_ORDER_DEFENDANT.name())
                    .caseDetails(CaseDetails.builder().state(All_FINAL_ORDERS_ISSUED.toString()).build()).build()).build();

            when(toggleService.isCaseProgressionEnabledAndLocationWhiteListed(any())).thenReturn(true);
            when(toggleService.isCarmEnabledForCase(any())).thenReturn(false);

            handler.handle(params);
            HashMap<String, Object> scenarioParams = new HashMap<>();

            // Then
            verifyDeleteNotificationsAndTaskListUpdates(caseData);
            verify(dashboardScenariosService).recordScenarios(
                "BEARER_TOKEN",
                "Scenario.AAA6.Update.TaskList.TrialReady.FinalOrders.Defendant",
                caseData.getCcdCaseReference().toString(),
                ScenarioRequestParams.builder().params(scenarioParams).build()
            );
        }

        @Test
        void shouldRecordScenarioDefendantFinalOrderFastTrackTrialReady_whenInvoked() {
            CaseData caseData = CaseDataBuilder.builder().atAllFinalOrdersIssuedCheck().build().toBuilder()
                .respondent1Represented(YesOrNo.NO)
                .claimsTrack(ClaimsTrack.fastTrack)
                .drawDirectionsOrderRequired(YesOrNo.NO)
                .trialReadyRespondent1(YesOrNo.YES)
                .build();

            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(CREATE_DASHBOARD_NOTIFICATION_FINAL_ORDER_DEFENDANT.name())
                    .caseDetails(CaseDetails.builder().state(All_FINAL_ORDERS_ISSUED.toString()).build()).build()).build();

            when(toggleService.isCaseProgressionEnabledAndLocationWhiteListed(any())).thenReturn(true);
            when(toggleService.isCarmEnabledForCase(any())).thenReturn(false);

            handler.handle(params);
            HashMap<String, Object> scenarioParams = new HashMap<>();
            verify(dashboardScenariosService).recordScenarios(
                "BEARER_TOKEN",
                "Scenario.AAA6.Update.Defendant.TaskList.UploadDocuments.FinalOrders",
                caseData.getCcdCaseReference().toString(),
                ScenarioRequestParams.builder().params(scenarioParams).build()
            );
        }
    }

    private void verifyDeleteNotificationsAndTaskListUpdates(CaseData caseData) {
        verify(dashboardNotificationService).deleteByReferenceAndCitizenRole(
            caseData.getCcdCaseReference().toString(),
            "DEFENDANT"
        );
        verify(taskListService).makeProgressAbleTasksInactiveForCaseIdentifierAndRole(
            caseData.getCcdCaseReference().toString(),
            "DEFENDANT",
            null
        );
    }
}
