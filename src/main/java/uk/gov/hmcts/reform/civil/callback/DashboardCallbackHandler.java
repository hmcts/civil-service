package uk.gov.hmcts.reform.civil.callback;

import com.google.common.base.Strings;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.client.DashboardApiClient;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;

import java.util.HashMap;
import java.util.Map;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;

@RequiredArgsConstructor
public abstract class DashboardCallbackHandler extends CallbackHandler {

    protected final DashboardApiClient dashboardApiClient;
    protected final DashboardNotificationsParamsMapper mapper;
    protected final FeatureToggleService featureToggleService;

    @Override
    protected Map<String, Callback> callbacks() {
        return featureToggleService.isLipVLipEnabled()
            ? Map.of(callbackKey(ABOUT_TO_SUBMIT), this::configureDashboardScenario)
            : Map.of(callbackKey(ABOUT_TO_SUBMIT), this::emptyCallbackResponse);
    }

    protected abstract String getScenario(CaseData caseData);

    protected String getExtraScenario() {
        return null;
    }

    @SuppressWarnings("unused")
    protected Map<String, Boolean> getScenarios(CaseData caseData) {
        return new HashMap<>();
    }

    /**
     * Depending on the case data, the scenario may or may not be applicable.
     *
     * @param caseData case's data
     * @return true if the scenario/notification should be recorded
     */
    protected boolean shouldRecordScenario(CaseData caseData) {
        return true;
    }

    protected boolean shouldRecordExtraScenario(CaseData caseData) {
        return false;
    }

    /**
     * Called just before a scenario is recorded, when the scenario is known and should record is true.
     *
     * @param caseData case info
     * @param authToken auth token
     */
    protected void beforeRecordScenario(CaseData caseData, String authToken) {
        // to be overridden
    }

    public CallbackResponse configureDashboardScenario(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        String authToken = callbackParams.getParams().get(BEARER_TOKEN).toString();
        String scenario = getScenario(caseData);
        ScenarioRequestParams scenarioParams = ScenarioRequestParams.builder().params(mapper.mapCaseDataToParams(
            caseData)).build();

        if (!Strings.isNullOrEmpty(scenario) && shouldRecordScenario(caseData)) {
            beforeRecordScenario(caseData, authToken);

            dashboardApiClient.recordScenario(
                caseData.getCcdCaseReference().toString(),
                scenario,
                authToken,
                scenarioParams
            );
        }

        scenario = getExtraScenario();
        if (!Strings.isNullOrEmpty(scenario) && shouldRecordExtraScenario(caseData)) {
            dashboardApiClient.recordScenario(
                caseData.getCcdCaseReference().toString(),
                scenario,
                authToken,
                scenarioParams
            );
        }

        ofNullable(getScenarios(caseData)).orElse(new HashMap<>())
            .entrySet().stream()
            .filter(scenarioEntry -> !Strings.isNullOrEmpty(scenarioEntry.getKey()) && scenarioEntry.getValue())
            .forEach(scenarioEntry -> dashboardApiClient.recordScenario(
                caseData.getCcdCaseReference().toString(),
                scenarioEntry.getKey(),
                authToken,
                scenarioParams));

        return AboutToStartOrSubmitCallbackResponse.builder().build();
    }

    protected FeatureToggleService getFeatureToggleService() {
        return featureToggleService;
    }
}
