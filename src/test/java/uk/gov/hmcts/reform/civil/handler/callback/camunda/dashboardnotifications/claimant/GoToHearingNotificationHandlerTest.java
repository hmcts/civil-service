package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.claimant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_DASHBOARD_NOTIFICATION_GO_TO_HEARING_FOR_APPLICANT1;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
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
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;

@ExtendWith(MockitoExtension.class)
public class GoToHearingNotificationHandlerTest extends BaseCallbackHandlerTest {

    @Mock
    private DashboardApiClient dashboardApiClient;
    @Mock
    private DashboardNotificationsParamsMapper mapper;
    @InjectMocks
    private GoToHearingNotificationHandler handler;

    @Nested
    class AboutToSubmitCallback {
        @BeforeEach
        void setup() {
            when(dashboardApiClient.recordScenario(any(), any(), anyString(), any())).thenReturn(ResponseEntity.of(
                Optional.empty()));
        }

        @Test
        void shouldRecordScenario_whenInvoked() {
            CaseData caseData = CaseDataBuilder.builder().atStateTrialReadyCheck().build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(CREATE_DASHBOARD_NOTIFICATION_GO_TO_HEARING_FOR_APPLICANT1.name()).build()
            ).build();

            Map<String, Object> scenarioParams = new HashMap<>();
            scenarioParams.put("defendant", "Defendant Name");

            when(mapper.mapCaseDataToParams(any())).thenReturn(scenarioParams);

            handler.handle(params);

            verify(dashboardApiClient).recordScenario(
                caseData.getCcdCaseReference().toString(),
                "Scenario.AAA7.ClaimantIntent.GoToHearing.Claimant",
                "BEARER_TOKEN",
                ScenarioRequestParams.builder().params(scenarioParams).build()
            );
        }
    }
}
