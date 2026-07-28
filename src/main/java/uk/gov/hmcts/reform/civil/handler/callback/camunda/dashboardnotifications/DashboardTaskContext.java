package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications;

import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.util.Optional;

import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;

public class DashboardTaskContext {

    private final CallbackParams callbackParams;
    private final CaseData caseData;
    private final GeneralApplicationCaseData generalApplicationCaseData;
    private final DashboardCaseType caseType;
    private final String authToken;

    private DashboardTaskContext(CallbackParams callbackParams,
                                 CaseData caseData,
                                 GeneralApplicationCaseData generalApplicationCaseData,
                                 DashboardCaseType caseType,
                                 String authToken) {
        this.callbackParams = callbackParams;
        this.caseData = caseData;
        this.generalApplicationCaseData = generalApplicationCaseData;
        this.caseType = caseType;
        this.authToken = authToken;
    }

    public static DashboardTaskContext from(CallbackParams callbackParams) {
        String authToken = Optional.ofNullable(callbackParams.getParams())
            .map(params -> params.get(BEARER_TOKEN))
            .map(Object::toString)
            .orElse(null);

        if (callbackParams.isGeneralApplicationCaseType()) {
            return generalApplication(callbackParams.getGeneralApplicationCaseData(), authToken, callbackParams);
        }

        if (!callbackParams.isCivilCaseType()) {
            try {
                return generalApplication(callbackParams.getGeneralApplicationCaseData(), authToken, callbackParams);
            } catch (IllegalStateException ignored) {
                return civil(callbackParams.getCaseData(), authToken, callbackParams);
            }
        }

        return civil(callbackParams.getCaseData(), authToken, callbackParams);
    }

    public static DashboardTaskContext civil(CaseData caseData, String authToken) {
        return civil(caseData, authToken, null);
    }

    private static DashboardTaskContext civil(CaseData caseData, String authToken, CallbackParams callbackParams) {
        return new DashboardTaskContext(callbackParams, caseData, null, DashboardCaseType.CIVIL, authToken);
    }

    public static DashboardTaskContext generalApplication(GeneralApplicationCaseData caseData, String authToken) {
        return generalApplication(caseData, authToken, null);
    }

    private static DashboardTaskContext generalApplication(GeneralApplicationCaseData caseData,
                                                           String authToken,
                                                           CallbackParams callbackParams) {
        return new DashboardTaskContext(
            callbackParams,
            null,
            caseData,
            DashboardCaseType.GENERAL_APPLICATION,
            authToken
        );
    }

    public CallbackParams callbackParams() {
        return callbackParams;
    }

    public CaseData caseData() {
        return caseData;
    }

    public GeneralApplicationCaseData generalApplicationCaseData() {
        return generalApplicationCaseData;
    }

    public DashboardCaseType caseType() {
        return caseType;
    }

    public String authToken() {
        return authToken;
    }
}
