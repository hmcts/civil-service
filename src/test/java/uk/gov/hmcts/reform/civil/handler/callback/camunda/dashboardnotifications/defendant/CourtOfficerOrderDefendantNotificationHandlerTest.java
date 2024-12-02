package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.defendant;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.client.DashboardApiClient;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;

import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_RESPONDENT_SOLICITOR1_FOR_COURT_OFFICER_ORDER;
import static uk.gov.hmcts.reform.civil.enums.CaseState.CASE_PROGRESSION;
import static uk.gov.hmcts.reform.civil.enums.CaseState.DECISION_OUTCOME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CASE_PROCEED_COURT_OFFICER_ORDER_DEFENDANT;

@ExtendWith(MockitoExtension.class)
public class CourtOfficerOrderDefendantNotificationHandlerTest extends BaseCallbackHandlerTest {

    @InjectMocks
    private CourtOfficerOrderDefendantNotificationHandler handler;
    @Mock
    private DashboardApiClient dashboardApiClient;
    @Mock
    private DashboardNotificationsParamsMapper mapper;
    @Mock
    private FeatureToggleService toggleService;
    public static final String TASK_ID = "GenerateDefendantDashboardNotificationCourtOfficerOrder";

    @Test
    void handleEventsReturnsTheExpectedCallbackEvent() {
        assertThat(handler.handledEvents()).contains(NOTIFY_RESPONDENT_SOLICITOR1_FOR_COURT_OFFICER_ORDER);
    }

    @Test
    void shouldReturnCorrectCamundaActivityId_whenInvoked() {
        assertThat(handler.camundaActivityId(
            CallbackParamsBuilder.builder()
                .request(CallbackRequest.builder()
                             .eventId(NOTIFY_RESPONDENT_SOLICITOR1_FOR_COURT_OFFICER_ORDER.name())
                             .build())
                .build()))
            .isEqualTo(TASK_ID);
    }

    @Nested
    class AboutToSubmitCallback {
        @Test
        void shouldRecordScenario_whenInvokedWithoutCaseEventsEnabled() {
            // Given
            CaseData caseData = CaseDataBuilder.builder().atStateRespondentFullAdmissionSpec().build().toBuilder()
                .respondent1Represented(YesOrNo.NO)
                .applicant1Represented(YesOrNo.NO)
                .ccdCaseReference(1234L)
                .previousCCDState(CASE_PROGRESSION).build();

            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(NOTIFY_RESPONDENT_SOLICITOR1_FOR_COURT_OFFICER_ORDER.name()).build()
            ).build();

            when(toggleService.isCaseEventsEnabled()).thenReturn(true);
            HashMap<String, Object> scenarioParams = new HashMap<>();
            when(mapper.mapCaseDataToParams(any())).thenReturn(scenarioParams);

            // When
            handler.handle(params);

            //then
            verify(dashboardApiClient).recordScenario(
                caseData.getCcdCaseReference().toString(),
                SCENARIO_AAA6_CASE_PROCEED_COURT_OFFICER_ORDER_DEFENDANT.getScenario(),
                "BEARER_TOKEN",
                ScenarioRequestParams.builder().params(scenarioParams).build()
            );
        }

        @Test
        void shouldRecordScenario_whenInvokedForCaseEventFeatureToggle() {
            // Given
            CaseData caseData = CaseDataBuilder.builder().atStateRespondentFullAdmissionSpec().build().toBuilder()
                .respondent1Represented(YesOrNo.NO)
                .applicant1Represented(YesOrNo.NO)
                .ccdCaseReference(1234L)
                .previousCCDState(DECISION_OUTCOME).build();

            when(toggleService.isCaseEventsEnabled()).thenReturn(true);
            HashMap<String, Object> scenarioParams = new HashMap<>();
            when(mapper.mapCaseDataToParams(any())).thenReturn(scenarioParams);

            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(NOTIFY_RESPONDENT_SOLICITOR1_FOR_COURT_OFFICER_ORDER.name()).build()
            ).build();
            // When
            handler.handle(params);

            verify(dashboardApiClient).recordScenario(
                caseData.getCcdCaseReference().toString(),
                SCENARIO_AAA6_CASE_PROCEED_COURT_OFFICER_ORDER_DEFENDANT.getScenario(),
                "BEARER_TOKEN",
                ScenarioRequestParams.builder().params(scenarioParams).build()
            );
        }

        @Test
        void shouldNotRecordScenario_whenNotInvoked() {
            // Given
            CaseData caseData = CaseDataBuilder.builder().atStateRespondentFullAdmissionSpec().build().toBuilder()
                .respondent1Represented(YesOrNo.NO)
                .applicant1Represented(YesOrNo.NO)
                .ccdCaseReference(1234L)
                .build();

            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(NOTIFY_RESPONDENT_SOLICITOR1_FOR_COURT_OFFICER_ORDER.name()).build()
            ).build();
            HashMap<String, Object> scenarioParams = new HashMap<>();

            // When
            handler.handle(params);

            // Then
            verify(dashboardApiClient, never()).recordScenario(
                caseData.getCcdCaseReference().toString(),
                SCENARIO_AAA6_CASE_PROCEED_COURT_OFFICER_ORDER_DEFENDANT.getScenario(),
                "BEARER_TOKEN",
                ScenarioRequestParams.builder().params(scenarioParams).build()
            );
        }

        @Test
        void shouldNotRecordScenario_whenCaseEventsIsNotEnabledAndIsOnCaseManState() {
            // Given
            CaseData caseData = CaseDataBuilder.builder().atStateRespondentFullAdmissionSpec().build().toBuilder()
                .respondent1Represented(YesOrNo.NO)
                .applicant1Represented(YesOrNo.NO)
                .ccdCaseReference(1234L)
                .previousCCDState(DECISION_OUTCOME).build();

            when(toggleService.isCaseEventsEnabled()).thenReturn(false);
            HashMap<String, Object> scenarioParams = new HashMap<>();

            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(NOTIFY_RESPONDENT_SOLICITOR1_FOR_COURT_OFFICER_ORDER.name()).build()
            ).build();

            // When
            handler.handle(params);

            // Then
            verify(dashboardApiClient, never()).recordScenario(
                caseData.getCcdCaseReference().toString(),
                SCENARIO_AAA6_CASE_PROCEED_COURT_OFFICER_ORDER_DEFENDANT.getScenario(),
                "BEARER_TOKEN",
                ScenarioRequestParams.builder().params(scenarioParams).build()
            );
        }
    }

}
