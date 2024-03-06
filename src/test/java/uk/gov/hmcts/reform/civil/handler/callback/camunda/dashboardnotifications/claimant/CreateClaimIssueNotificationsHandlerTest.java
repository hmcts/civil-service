package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.claimant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.client.DashboardApiClient;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_DASHBOARD_NOTIFICATION_FOR_CLAIM_ISSUE_FOR_APPLICANT1;

@SpringBootTest(classes = {
    CreateClaimIssueNotificationsHandler.class,
    JacksonAutoConfiguration.class
})
public class CreateClaimIssueNotificationsHandlerTest extends BaseCallbackHandlerTest {

    @MockBean
    private DashboardApiClient dashboardApiClient;
    @MockBean
    private DashboardNotificationsParamsMapper mapper;
    @Autowired
    private CreateClaimIssueNotificationsHandler handler;
    @MockBean
    private FeatureToggleService toggleService;

    @Nested
    class AboutToSubmitCallback {
        @BeforeEach
        void setup() {
            when(dashboardApiClient.recordScenario(any(), any(), anyString(), any())).thenReturn(ResponseEntity.of(
                Optional.empty()));
        }

        @Test
        void shouldRecordScenario_whenInvoked() {
            when(toggleService.isDashboardServiceEnabled())
                .thenReturn(true);
            CaseData caseData = CaseDataBuilder.builder().atStateTrialReadyCheck().build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(CREATE_DASHBOARD_NOTIFICATION_FOR_CLAIM_ISSUE_FOR_APPLICANT1.name()).build()
            ).build();

            Map<String, Object> scenarioParams = new HashMap<>();
            scenarioParams.put("ccdCaseReference", caseData.getCcdCaseReference());

            when(mapper.mapCaseDataToParams(any())).thenReturn(scenarioParams);

            handler.handle(params);
            verify(dashboardApiClient).recordScenario(
                caseData.getCcdCaseReference().toString(),
                "Scenario.AAA7.ClaimIssue.Response.Await",
                "BEARER_TOKEN",
                ScenarioRequestParams.builder().params(scenarioParams).build()
            );
        }
    }
}
