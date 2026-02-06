package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;

class GaDashboardServiceTaskTest {

    private static final String AUTH_TOKEN = "auth-token";

    @Test
    void shouldDelegateToServiceWithCaseDataAndAuthToken() {
        GeneralApplicationCaseData caseData = GeneralApplicationCaseData.builder()
            .ccdCaseReference(123456L)
            .build();
        CallbackParams callbackParams = CallbackParams.builder()
            .caseData(caseData)
            .params(Map.of(BEARER_TOKEN, AUTH_TOKEN))
            .build();

        DashboardTaskContext context = DashboardTaskContext.from(callbackParams);
        CapturingTask task = new CapturingTask();

        task.execute(context);

        assertThat(task.capturedCaseData).isSameAs(caseData);
        assertThat(task.capturedAuthToken).isEqualTo(AUTH_TOKEN);
    }

    @Test
    void shouldThrowWhenAuthTokenMissing() {
        GeneralApplicationCaseData caseData = GeneralApplicationCaseData.builder()
            .ccdCaseReference(123456L)
            .build();
        CallbackParams callbackParams = CallbackParams.builder()
            .caseData(caseData)
            .build();

        DashboardTaskContext context = DashboardTaskContext.from(callbackParams);
        CapturingTask task = new CapturingTask();

        assertThatThrownBy(() -> task.execute(context))
            .isInstanceOf(NullPointerException.class)
            .hasMessageContaining("Missing auth token");
    }

    private static class CapturingTask extends GaDashboardServiceTask {

        private GeneralApplicationCaseData capturedCaseData;
        private String capturedAuthToken;

        @Override
        protected void notifyDashboard(GeneralApplicationCaseData caseData, String authToken) {
            capturedCaseData = caseData;
            capturedAuthToken = authToken;
        }
    }
}
