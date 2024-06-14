package uk.gov.hmcts.reform.civil.callback;

import uk.gov.hmcts.reform.civil.client.DashboardApiClient;
import uk.gov.hmcts.reform.civil.service.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;

public abstract class CaseProgressionDashboardCallbackHandler extends DashboardCallbackHandler {

    public CaseProgressionDashboardCallbackHandler(DashboardApiClient dashboardApiClient, DashboardNotificationsParamsMapper mapper, FeatureToggleService featureToggleService) {
        super(dashboardApiClient, mapper, featureToggleService);
    }

    @Override
    protected Map<String, Callback> callbacks() {
        return featureToggleService.isCaseProgressionEnabled()
            ? Map.of(callbackKey(ABOUT_TO_SUBMIT), this::configureDashboardScenario)
            : Map.of(callbackKey(ABOUT_TO_SUBMIT), this::emptyCallbackResponse);
    }
}
