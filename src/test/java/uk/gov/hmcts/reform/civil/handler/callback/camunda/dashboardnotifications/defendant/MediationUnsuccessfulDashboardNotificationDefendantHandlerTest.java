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
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_DEFENDANT_DASHBOARD_NOTIFICATION_FOR_MEDIATION_UNSUCCESSFUL;
import static uk.gov.hmcts.reform.civil.enums.mediation.MediationUnsuccessfulReason.APPOINTMENT_NO_AGREEMENT;
import static uk.gov.hmcts.reform.civil.enums.mediation.MediationUnsuccessfulReason.NOT_CONTACTABLE_DEFENDANT_ONE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CLAIMANT_INTENT_MEDIATION_UNSUCCESSFUL_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_DEFENDANT_MEDIATION_UNSUCCESSFUL_DEFENDANT_NONATTENDANCE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_DEFENDANT_MEDIATION_UNSUCCESSFUL_GENERIC;

@ExtendWith(MockitoExtension.class)
public class MediationUnsuccessfulDashboardNotificationDefendantHandlerTest extends BaseCallbackHandlerTest {

    @InjectMocks
    private MediationUnsuccessfulDashboardNotificationDefendantHandler handler;

    @Mock
    private DashboardApiClient dashboardApiClient;

    @Mock
    private DashboardNotificationsParamsMapper dashboardNotificationsParamsMapper;

    @Mock
    private FeatureToggleService featureToggleService;

    public static final String TASK_ID = "GenerateDashboardNotificationDefendantMediationUnsuccessful";
    HashMap<String, Object> params = new HashMap<>();

    @Nested
    class AboutToSubmitCallback {

        @Test
        void handleEventsReturnsTheExpectedCallbackEvent() {
            assertThat(handler.handledEvents()).contains(
                CREATE_DEFENDANT_DASHBOARD_NOTIFICATION_FOR_MEDIATION_UNSUCCESSFUL);
        }

        @Test
        void shouldReturnCorrectCamundaActivityId_whenInvoked() {
            assertThat(handler.camundaActivityId(
                CallbackParamsBuilder.builder()
                    .request(CallbackRequest.builder()
                                 .eventId(CREATE_DEFENDANT_DASHBOARD_NOTIFICATION_FOR_MEDIATION_UNSUCCESSFUL.name())
                                 .build())
                    .build()))
                .isEqualTo(TASK_ID);
        }

        @Test
        void createDashboardNotifications() {
            params.put("ccdCaseReference", "123");

            when(dashboardNotificationsParamsMapper.mapCaseDataToParams(any())).thenReturn(params);
            when(featureToggleService.isLipVLipEnabled()).thenReturn(true);
            when(featureToggleService.isCarmEnabledForCase(any())).thenReturn(false);

            CaseData caseData = CaseData.builder()
                .respondent1Represented(YesOrNo.NO)
                .legacyCaseReference("reference")
                .ccdCaseReference(1234L)
                .build();

            CallbackParams callbackParams = CallbackParamsBuilder.builder()
                .of(ABOUT_TO_SUBMIT, caseData)
                .build();

            handler.handle(callbackParams);
            verify(dashboardApiClient).recordScenario(
                caseData.getCcdCaseReference().toString(),
                SCENARIO_AAA6_CLAIMANT_INTENT_MEDIATION_UNSUCCESSFUL_DEFENDANT.getScenario(),
                "BEARER_TOKEN",
                ScenarioRequestParams.builder().params(params).build()
            );
        }

        @Test
        void createDashboardNotificationsWhenCarmIsEnabledAndMediationReasonIsGeneric() {
            when(featureToggleService.isCarmEnabledForCase(any())).thenReturn(true);
            when(featureToggleService.isLipVLipEnabled()).thenReturn(true);

            params.put("ccdCaseReference", "123");
            MediationUnsuccessfulReason reason = APPOINTMENT_NO_AGREEMENT;

            when(dashboardNotificationsParamsMapper.mapCaseDataToParams(any())).thenReturn(params);

            LocalDateTime dateTime = LocalDate.of(2020, Month.JANUARY, 18).atStartOfDay();

            CaseData caseData = CaseData.builder()
                .legacyCaseReference("reference")
                .respondent1Represented(YesOrNo.NO)
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
                SCENARIO_AAA6_DEFENDANT_MEDIATION_UNSUCCESSFUL_GENERIC.getScenario(),
                "BEARER_TOKEN",
                ScenarioRequestParams.builder().params(params).build()
            );
        }

        @Test
        void createDashboardNotificationsWhenCarmIsEnabledAndMediationReasonIsDefendantNonAttendance() {
            when(featureToggleService.isCarmEnabledForCase(any())).thenReturn(true);
            when(featureToggleService.isLipVLipEnabled()).thenReturn(true);

            params.put("ccdCaseReference", "123");
            MediationUnsuccessfulReason reason = NOT_CONTACTABLE_DEFENDANT_ONE;

            when(dashboardNotificationsParamsMapper.mapCaseDataToParams(any())).thenReturn(params);

            LocalDateTime dateTime = LocalDate.of(2020, Month.JANUARY, 18).atStartOfDay();

            CaseData caseData = CaseData.builder()
                .legacyCaseReference("reference")
                .respondent1Represented(YesOrNo.NO)
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
                SCENARIO_AAA6_DEFENDANT_MEDIATION_UNSUCCESSFUL_DEFENDANT_NONATTENDANCE.getScenario(),
                "BEARER_TOKEN",
                ScenarioRequestParams.builder().params(params).build()
            );
        }
    }
}
