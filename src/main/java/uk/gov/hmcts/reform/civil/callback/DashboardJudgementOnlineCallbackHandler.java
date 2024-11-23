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

import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;

@RequiredArgsConstructor
public abstract class DashboardJudgementOnlineCallbackHandler extends CallbackHandler {

    protected final DashboardApiClient dashboardApiClient;
    protected final DashboardNotificationsParamsMapper mapper;
    protected final FeatureToggleService featureToggleService;

    @Override
    protected Map<String, Callback> callbacks() {
        return featureToggleService.isJudgmentOnlineLive()
            ? Map.of(callbackKey(ABOUT_TO_SUBMIT), this::configureDashboardScenario,
                     callbackKey(SUBMITTED), this::emptySubmittedCallbackResponse)
            : Map.of(callbackKey(ABOUT_TO_SUBMIT), this::emptyCallbackResponse,
                     callbackKey(SUBMITTED), this::emptySubmittedCallbackResponse);
    }

    protected abstract String getScenario(CaseData caseData);

    /**
     * Depending on the case data, the scenario may or may not be applicable.
     *
     * @param caseData case's data
     * @return true if the scenario/notification should be recorded
     */
    protected boolean shouldRecordScenario(CaseData caseData) {
        return true;
    }

    public CallbackResponse configureDashboardScenario(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        String authToken = callbackParams.getParams().get(BEARER_TOKEN).toString();
        String scenario = getScenario(caseData);
        ScenarioRequestParams scenarioParams = ScenarioRequestParams.builder().params(mapper.mapCaseDataToParams(
            caseData)).build();

        if (!Strings.isNullOrEmpty(scenario) && shouldRecordScenario(caseData)) {
            dashboardApiClient.recordScenario(
                caseData.getCcdCaseReference().toString(),
                scenario,
                authToken,
                scenarioParams
            );
        }

        return AboutToStartOrSubmitCallbackResponse.builder().build();
    }
}
