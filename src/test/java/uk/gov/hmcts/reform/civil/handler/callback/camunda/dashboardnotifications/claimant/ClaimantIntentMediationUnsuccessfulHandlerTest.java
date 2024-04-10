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
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.service.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;

import java.util.HashMap;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_CLAIMANT_DASHBOARD_NOTIFICATION_FOR_MEDIATION_UNSUCCESSFUL;

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
            when(toggleService.isDashboardServiceEnabled()).thenReturn(true);
        }

        @Test
        public void createDashboardNotificationsMediationUnsuccess() {

            HashMap<String, Object> scenarioParams = new HashMap<>();
            scenarioParams.put("ccdCaseReference", "123455L");

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
    }
}
