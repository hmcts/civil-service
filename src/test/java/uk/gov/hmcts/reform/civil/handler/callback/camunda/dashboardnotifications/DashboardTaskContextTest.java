package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;

class DashboardTaskContextTest {

    @Test
    void shouldExposeCaseDataAndAuthToken() {
        CaseData caseData = CaseData.builder().ccdCaseReference(123L).build();
        CallbackParams params = CallbackParams.builder()
            .caseData(caseData)
            .params(Map.of(BEARER_TOKEN, "token"))
            .build();

        DashboardTaskContext context = DashboardTaskContext.from(params);

        assertThat(context.caseData()).isSameAs(caseData);
        assertThat(context.authToken()).isEqualTo("token");
        assertThat(context.callbackParams()).isSameAs(params);
    }

    @Test
    void shouldReturnNullTokenWhenMissing() {
        CaseData caseData = CaseData.builder().build();
        CallbackParams params = CallbackParams.builder()
            .caseData(caseData)
            .build();

        DashboardTaskContext context = DashboardTaskContext.from(params);

        assertThat(context.authToken()).isNull();
    }
}
