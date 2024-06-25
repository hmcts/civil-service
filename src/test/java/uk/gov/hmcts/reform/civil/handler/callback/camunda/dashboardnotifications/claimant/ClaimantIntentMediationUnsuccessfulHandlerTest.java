package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.claimant;

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
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.mediation.MediationUnsuccessfulReason;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Mediation;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.service.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_CLAIMANT_DASHBOARD_NOTIFICATION_FOR_MEDIATION_UNSUCCESSFUL;
import static uk.gov.hmcts.reform.civil.enums.mediation.MediationUnsuccessfulReason.APPOINTMENT_NO_AGREEMENT;
import static uk.gov.hmcts.reform.civil.enums.mediation.MediationUnsuccessfulReason.NOT_CONTACTABLE_CLAIMANT_ONE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CLAIMANT_MEDIATION_UNSUCCESSFUL_CLAIMANT_NONATTENDANCE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CLAIMANT_MEDIATION_UNSUCCESSFUL_GENERIC;

@ExtendWith(MockitoExtension.class)
public class ClaimantIntentMediationUnsuccessfulHandlerTest extends BaseCallbackHandlerTest {

    @InjectMocks
    private ClaimantIntentMediationUnsuccessfulHandler handler;

    @Mock
    private DashboardApiClient dashboardApiClient;

    @Mock
    private FeatureToggleService toggleService;

    @Mock
    private DashboardNotificationsParamsMapper mapper;

    public static final String TASK_ID = "GenerateDashboardNotificationMediationUnsuccessfulForApplicant1";
    HashMap<String, Object> scenarioParams = new HashMap<>();

    @Test
    void handleEventsReturnsTheExpectedCallbackEvent() {
        assertThat(handler.handledEvents()).contains(
            CREATE_CLAIMANT_DASHBOARD_NOTIFICATION_FOR_MEDIATION_UNSUCCESSFUL);
    }

    @Test
    void shouldReturnCorrectCamundaActivityId_whenInvoked() {
        assertThat(handler.camundaActivityId(
            CallbackParamsBuilder.builder()
                .request(CallbackRequest.builder()
                             .eventId(CREATE_CLAIMANT_DASHBOARD_NOTIFICATION_FOR_MEDIATION_UNSUCCESSFUL.name())
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
        void createDashboardNotificationsWhenCarmIsDisabledMediationUnsuccessful() {
            scenarioParams.put("ccdCaseReference", "123455L");
            when(toggleService.isCarmEnabledForCase(any())).thenReturn(false);

            when(mapper.mapCaseDataToParams(any())).thenReturn(scenarioParams);

            CaseData caseData = CaseData.builder()
                .legacyCaseReference("reference")
                .applicant1Represented(YesOrNo.NO)
                .ccdCaseReference(123455L)
                .build();

            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(CREATE_CLAIMANT_DASHBOARD_NOTIFICATION_FOR_MEDIATION_UNSUCCESSFUL.name()).build()
            ).build();

            handler.handle(params);
            verify(dashboardApiClient).recordScenario(
                caseData.getCcdCaseReference().toString(),
                "Scenario.AAA6.ClaimantIntent.MediationUnsuccessful.Claimant",
                "BEARER_TOKEN",
                ScenarioRequestParams.builder().params(scenarioParams).build()
            );
        }

        @Test
        void createDashboardNotificationsWhenCarmIsEnabledAndMediationReasonIsGeneric() {
            when(toggleService.isCarmEnabledForCase(any())).thenReturn(true);

            scenarioParams.put("ccdCaseReference", "123");
            MediationUnsuccessfulReason reason = APPOINTMENT_NO_AGREEMENT;

            when(mapper.mapCaseDataToParams(any())).thenReturn(scenarioParams);

            LocalDateTime dateTime = LocalDate.of(2020, Month.JANUARY, 18).atStartOfDay();

            CaseData caseData = CaseData.builder()
                .legacyCaseReference("reference")
                .applicant1Represented(YesOrNo.NO)
                .ccdCaseReference(1234L)
                .respondent1ResponseDeadline(dateTime)
                .mediation(Mediation.builder()
                               .mediationUnsuccessfulReasonsMultiSelect(List.of(reason)).build())
                .build();

            CallbackParams callbackParams = CallbackParamsBuilder.builder()
                .of(ABOUT_TO_SUBMIT, caseData)
                .build();

            handler.handle(callbackParams);
            verify(dashboardApiClient).recordScenario(
                caseData.getCcdCaseReference().toString(),
                SCENARIO_AAA6_CLAIMANT_MEDIATION_UNSUCCESSFUL_GENERIC.getScenario(),
                "BEARER_TOKEN",
                ScenarioRequestParams.builder().params(scenarioParams).build()
            );
        }

        @Test
        void createDashboardNotificationsWhenCarmIsEnabledAndMediationReasonClaimantNonAttendance() {
            when(toggleService.isCarmEnabledForCase(any())).thenReturn(true);

            scenarioParams.put("ccdCaseReference", "123");
            MediationUnsuccessfulReason reason = NOT_CONTACTABLE_CLAIMANT_ONE;

            when(mapper.mapCaseDataToParams(any())).thenReturn(scenarioParams);

            LocalDateTime dateTime = LocalDate.of(2020, Month.JANUARY, 18).atStartOfDay();

            CaseData caseData = CaseData.builder()
                .legacyCaseReference("reference")
                .applicant1Represented(YesOrNo.NO)
                .ccdCaseReference(1234L)
                .respondent1ResponseDeadline(dateTime)
                .mediation(Mediation.builder()
                               .mediationUnsuccessfulReasonsMultiSelect(List.of(reason)).build())
                .build();

            CallbackParams callbackParams = CallbackParamsBuilder.builder()
                .of(ABOUT_TO_SUBMIT, caseData)
                .build();

            handler.handle(callbackParams);
            verify(dashboardApiClient).recordScenario(
                caseData.getCcdCaseReference().toString(),
                SCENARIO_AAA6_CLAIMANT_MEDIATION_UNSUCCESSFUL_CLAIMANT_NONATTENDANCE.getScenario(),
                "BEARER_TOKEN",
                ScenarioRequestParams.builder().params(scenarioParams).build()
            );
        }
    }
}
