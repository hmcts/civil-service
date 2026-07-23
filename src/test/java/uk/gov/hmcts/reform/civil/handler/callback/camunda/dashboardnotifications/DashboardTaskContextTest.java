package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;

class DashboardTaskContextTest {

    @Test
    void shouldExposeGaCaseDataAndCaseType() {
        GeneralApplicationCaseData gaCaseData = new GeneralApplicationCaseData()
            .ccdCaseReference(111L)
            .build();
        CallbackParams callbackParams = new CallbackParams()
            .caseData(gaCaseData)
            .isGeneralApplicationCaseType(true)
            .params(Map.of(BEARER_TOKEN, "ga-token"));

        DashboardTaskContext context = DashboardTaskContext.from(callbackParams);

        assertThat(context.caseType()).isEqualTo(DashboardCaseType.GENERAL_APPLICATION);
        assertThat(context.generalApplicationCaseData()).isSameAs(gaCaseData);
        assertThat(context.authToken()).isEqualTo("ga-token");
    }

    @Test
    void shouldExposeCivilCaseDataAndCaseType() {
        CaseData civilCaseData = CaseData.builder().build();
        CallbackParams callbackParams = new CallbackParams()
            .caseData(civilCaseData)
            .isGeneralApplicationCaseType(false)
            .params(Map.of(BEARER_TOKEN, "civil-token"));

        DashboardTaskContext context = DashboardTaskContext.from(callbackParams);

        assertThat(context.caseType()).isEqualTo(DashboardCaseType.CIVIL);
        assertThat(context.caseData()).isSameAs(civilCaseData);
        assertThat(context.authToken()).isEqualTo("civil-token");
    }

    @Test
    void shouldReturnNullTokenWhenMissing() {
        CaseData caseData = CaseData.builder().build();
        CallbackParams params = new CallbackParams()
            .caseData(caseData);

        DashboardTaskContext context = DashboardTaskContext.from(params);

        assertThat(context.authToken()).isNull();
    }

    @Test
    void shouldCreateCivilContextWithoutCallbackParams() {
        CaseData caseData = CaseData.builder().build();

        DashboardTaskContext context = DashboardTaskContext.civil(caseData, "direct-token");

        assertThat(context.caseType()).isEqualTo(DashboardCaseType.CIVIL);
        assertThat(context.caseData()).isSameAs(caseData);
        assertThat(context.authToken()).isEqualTo("direct-token");
        assertThat(context.callbackParams()).isNull();
    }

    @Test
    void shouldCreateGaContextWithoutCallbackParams() {
        GeneralApplicationCaseData caseData = new GeneralApplicationCaseData().build();

        DashboardTaskContext context = DashboardTaskContext.generalApplication(caseData, "direct-token");

        assertThat(context.caseType()).isEqualTo(DashboardCaseType.GENERAL_APPLICATION);
        assertThat(context.generalApplicationCaseData()).isSameAs(caseData);
        assertThat(context.authToken()).isEqualTo("direct-token");
        assertThat(context.callbackParams()).isNull();
    }
}
