package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_GENERAL_APPLICATION_SUBMITTED_APPLICANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_GENERAL_APPS_HWF_FEE_PAID_APPLICANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_GENERAL_APPS_HWF_FULL_REMISSION_APPLICANT;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApplicationSubmittedDashboardNotificationHandler extends CallbackHandler {

    protected final DashboardScenariosService dashboardScenariosService;
    protected final DashboardNotificationsParamsMapper mapper;
    protected final FeatureToggleService featureToggleService;

    private static final List<CaseEvent> EVENTS = List.of(CaseEvent.UPDATE_GA_DASHBOARD_NOTIFICATION);

    @Override
    protected Map<String, Callback> callbacks() {
        return featureToggleService.isGaForLipsEnabled()
                ? Map.of(callbackKey(ABOUT_TO_SUBMIT), this::configureDashboardScenario)
                : Map.of(callbackKey(ABOUT_TO_SUBMIT), this::emptyCallbackResponse);
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    public List<String> getScenarios(CaseData caseData) {
        List<String> scenarios = new ArrayList<>();
        if (Objects.nonNull(caseData.getGaHwfDetails())) {
            if (caseData.gaApplicationFeeFullRemissionNotGrantedHWF(caseData)) {
                scenarios.add(SCENARIO_AAA6_GENERAL_APPS_HWF_FEE_PAID_APPLICANT.getScenario());
            } else {
                scenarios.add(SCENARIO_AAA6_GENERAL_APPS_HWF_FULL_REMISSION_APPLICANT.getScenario());
            }
        }
        scenarios.add(SCENARIO_AAA6_GENERAL_APPLICATION_SUBMITTED_APPLICANT.getScenario());
        return scenarios;
    }

    public CallbackResponse configureDashboardScenario(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        String authToken = callbackParams.getParams().get(BEARER_TOKEN).toString();
        List<String> scenarios = getScenarios(caseData);
        ScenarioRequestParams scenarioParams = ScenarioRequestParams.builder().params(mapper.mapCaseDataToParams(
                caseData)).build();
        scenarios.forEach(scenario -> dashboardScenariosService.recordScenarios(
                authToken,
                scenario,
                caseData.getCcdCaseReference().toString(),
                scenarioParams
        ));

        return AboutToStartOrSubmitCallbackResponse.builder().build();
    }

}
