package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications;

import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.util.Optional;

import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;

public class DashboardTaskContext {

    private final CallbackParams callbackParams;

    private DashboardTaskContext(CallbackParams callbackParams) {
        this.callbackParams = callbackParams;
    }

    public static DashboardTaskContext from(CallbackParams callbackParams) {
        return new DashboardTaskContext(callbackParams);
    }

    public CallbackParams callbackParams() {
        return callbackParams;
    }

    public CaseData caseData() {
        return callbackParams.getCaseData();
    }

    public GeneralApplicationCaseData generalApplicationCaseData() {
        return callbackParams.getGeneralApplicationCaseData();
    }

    public DashboardCaseType caseType() {
        return callbackParams.isGeneralApplicationCaseType()
            ? DashboardCaseType.GENERAL_APPLICATION
            : DashboardCaseType.CIVIL;
    }

    public String authToken() {
        return Optional.ofNullable(callbackParams.getParams())
            .map(params -> params.get(BEARER_TOKEN))
            .map(Object::toString)
            .orElse(null);
    }
}
