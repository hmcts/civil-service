package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications;

import com.fasterxml.jackson.databind.ObjectMapper;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.DashboardCallbackHandler;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.GaCallbackDataUtil;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;

/**
 * Dashboard callback base that resolves GA callback payloads into Civil {@link CaseData}
 * before invoking the standard dashboard behaviour.
 */
public abstract class GaDashboardCallbackHandler extends DashboardCallbackHandler {

    private final ObjectMapper objectMapper;

    protected GaDashboardCallbackHandler(DashboardScenariosService dashboardScenariosService,
                                         DashboardNotificationsParamsMapper mapper,
                                         FeatureToggleService featureToggleService,
                                         ObjectMapper objectMapper) {
        super(dashboardScenariosService, mapper, featureToggleService);
        this.objectMapper = objectMapper;
    }

    @Override
    protected CaseData resolveCaseData(CallbackParams callbackParams) {
        return GaCallbackDataUtil.resolveCaseData(callbackParams, objectMapper);
    }
}
