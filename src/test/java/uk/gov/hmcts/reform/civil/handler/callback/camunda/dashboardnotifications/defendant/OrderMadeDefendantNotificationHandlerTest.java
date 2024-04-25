package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.defendant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.client.DashboardApiClient;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.utils.ElementUtils;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.*;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_DASHBOARD_NOTIFICATION_HEARING_SCHEDULED_CLAIMANT;

@ExtendWith(MockitoExtension.class)
public class OrderMadeDefendantNotificationHandlerTest extends BaseCallbackHandlerTest {

    @InjectMocks
    private OrderMadeDefendantNotificationHandler handler;
    @Mock
    private DashboardApiClient dashboardApiClient;
    @Mock
    private DashboardNotificationsParamsMapper mapper;
    @Mock
    private FeatureToggleService toggleService;

    public static final String TASK_ID = "GenerateDashboardNotificationFinalOrderDefendant";

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
            when(toggleService.isDashboardServiceEnabled()).thenReturn(true);
        }

        @Test
        void shouldRecordScenario_whenInvoked() {
            CaseData caseData = CaseDataBuilder.builder().atStateTrialReadyCheck().build().toBuilder()
                .finalOrderDocumentCollection(List.of(ElementUtils.element(CaseDocument.builder().documentLink(Document.builder().documentBinaryUrl("url").build()).build())))
                .respondent1Represented(YesOrNo.NO).build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(CREATE_DASHBOARD_NOTIFICATION_FINAL_ORDER_DEFENDANT.name()).build()
            ).build();

            HashMap<String, Object> scenarioParams = new HashMap<>();
            scenarioParams.put("orderDocument", "url");

            when(mapper.getMapWithDocumentInfo(any(), any())).thenReturn(scenarioParams);

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
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(CREATE_DASHBOARD_NOTIFICATION_DJ_SDO_DEFENDANT.name()).build()
            ).build();

            HashMap<String, Object> scenarioParams = new HashMap<>();
            scenarioParams.put("orderDocument", "urlDirectionsOrder");

            when(mapper.getMapWithDocumentInfo(any(), any())).thenReturn(scenarioParams);

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
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(CREATE_DASHBOARD_NOTIFICATION_SDO_DEFENDANT.name()).build()
            ).build();

            HashMap<String, Object> scenarioParams = new HashMap<>();
            scenarioParams.put("orderDocument", "urlDirectionsOrder");

            when(mapper.getMapWithDocumentInfo(any(), any())).thenReturn(scenarioParams);

            handler.handle(params);

            verify(dashboardApiClient).recordScenario(
                caseData.getCcdCaseReference().toString(),
                "Scenario.AAA6.CP.OrderMade.Defendant",
                "BEARER_TOKEN",
                ScenarioRequestParams.builder().params(scenarioParams).build()
            );
        }
    }
}
