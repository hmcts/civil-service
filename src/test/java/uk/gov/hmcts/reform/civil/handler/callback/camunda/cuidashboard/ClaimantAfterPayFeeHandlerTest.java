package uk.gov.hmcts.reform.civil.handler.callback.camunda.cuidashboard;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
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
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.GENERATE_DASHBOARD_NOTIFICATION_CUI;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.cuidashboard.DashboardScenarios.SCENARIO_AAA7_CLAIM_ISSUE_RESPONSE_AWAIT;

@SpringBootTest(classes = {
    ClaimantAfterPayFeeHandler.class,
    JacksonAutoConfiguration.class
})
public class ClaimantAfterPayFeeHandlerTest extends BaseCallbackHandlerTest {

    @MockBean
    private DashboardApiClient dashboardApiClient;
    @Autowired
    private ClaimantAfterPayFeeHandler handler;

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
                CallbackRequest.builder().eventId(SCENARIO_AAA7_CLAIM_ISSUE_RESPONSE_AWAIT.name()).build()
            ).build();

            handler.handle(params);

            Map<String, Object> scenarioParams = new HashMap<>();
            scenarioParams.put("claimantName", "claimant_Name_Test");
            verify(dashboardApiClient).recordScenario(
                caseData.getCcdCaseReference().toString(),
                "SCENARIO_AAA7_CLAIM_ISSUE_RESPONSE_AWAIT",
                "BEARER_TOKEN",
                ScenarioRequestParams.builder().params(scenarioParams).build()
            );
        }
    }
}
