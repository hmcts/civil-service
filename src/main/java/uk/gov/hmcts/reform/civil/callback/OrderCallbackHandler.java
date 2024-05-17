package uk.gov.hmcts.reform.civil.callback;

import com.google.common.base.Strings;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.client.DashboardApiClient;
import uk.gov.hmcts.reform.civil.enums.AllocatedTrack;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;

import java.math.BigDecimal;
import java.util.HashMap;

import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.enums.AllocatedTrack.SMALL_CLAIM;

public abstract class OrderCallbackHandler extends DashboardWithParamsCallbackHandler {

    protected OrderCallbackHandler(DashboardApiClient dashboardApiClient, DashboardNotificationsParamsMapper mapper, FeatureToggleService featureToggleService) {
        super(dashboardApiClient, mapper, featureToggleService);
    }

    @Override
    public CallbackResponse configureDashboardScenario(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        CaseEvent caseEvent = CaseEvent.valueOf(callbackParams.getRequest().getEventId());
        HashMap<String, Object> paramsMap = (HashMap<String, Object>) mapper.mapCaseDataToParams(caseData, caseEvent);

        String authToken = callbackParams.getParams().get(BEARER_TOKEN).toString();
        String scenario = getScenario(caseData, callbackParams);
        if (!Strings.isNullOrEmpty(scenario) && shouldRecordScenario(caseData)) {
            dashboardApiClient.recordScenario(
                caseData.getCcdCaseReference().toString(),
                scenario,
                authToken,
                ScenarioRequestParams.builder().params(paramsMap).build()
            );
        }

        return AboutToStartOrSubmitCallbackResponse.builder().build();
    }

    protected boolean isEligibleForReconsideration(CaseData caseData) {
        return caseData.isSmallClaim() && caseData.getTotalClaimAmount() <= BigDecimal.valueOf(10000);
    }

    protected boolean hasTrackChanged(CaseData caseData) {
        return SMALL_CLAIM.equals(getPreviousAllocatedTrack(caseData))
            && !caseData.isSmallClaim();
    }

    protected AllocatedTrack getPreviousAllocatedTrack(CaseData caseData) {
        return AllocatedTrack.getAllocatedTrack(
            caseData.getTotalClaimAmount(),
            null,
            null
        );
    }

    protected boolean isCarmApplicableCase(CaseData caseData) {
        return getFeatureToggleService().isCarmEnabledForCase(caseData)
            && SMALL_CLAIM.equals(getPreviousAllocatedTrack(caseData));
    }

}
