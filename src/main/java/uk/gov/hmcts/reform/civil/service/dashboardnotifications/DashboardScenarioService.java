package uk.gov.hmcts.reform.civil.service.dashboardnotifications;

import com.google.common.base.Strings;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public abstract class DashboardScenarioService {

    protected final DashboardScenariosService dashboardScenariosService;
    protected final DashboardNotificationsParamsMapper mapper;

    protected DashboardScenarioService(DashboardScenariosService dashboardScenariosService,
                                       DashboardNotificationsParamsMapper mapper) {
        this.dashboardScenariosService = dashboardScenariosService;
        this.mapper = mapper;
    }

    protected void recordScenario(CaseData caseData, String authToken) {
        ScenarioRequestParams scenarioParams = ScenarioRequestParams.builder()
            .params(mapper.mapCaseDataToParams(caseData))
            .build();

        String scenario = getScenario(caseData);
        if (!Strings.isNullOrEmpty(scenario) && shouldRecordScenario(caseData)) {
            beforeRecordScenario(caseData, authToken);
            dashboardScenariosService.recordScenarios(
                authToken,
                scenario,
                caseData.getCcdCaseReference().toString(),
                scenarioParams
            );
        }

        String extraScenario = getExtraScenario();
        if (!Strings.isNullOrEmpty(extraScenario) && shouldRecordExtraScenario(caseData)) {
            dashboardScenariosService.recordScenarios(
                authToken,
                extraScenario,
                caseData.getCcdCaseReference().toString(),
                scenarioParams
            );
        }

        Optional.ofNullable(getScenarios(caseData)).orElse(new HashMap<>())
            .entrySet().stream()
            .filter(entry -> !Strings.isNullOrEmpty(entry.getKey()) && Boolean.TRUE.equals(entry.getValue()))
            .forEach(entry -> dashboardScenariosService.recordScenarios(
                authToken,
                entry.getKey(),
                caseData.getCcdCaseReference().toString(),
                scenarioParams
            ));
    }

    protected abstract String getScenario(CaseData caseData);

    protected String getExtraScenario() {
        return null;
    }

    protected Map<String, Boolean> getScenarios(CaseData caseData) {
        return new HashMap<>();
    }

    protected boolean shouldRecordScenario(CaseData caseData) {
        return true;
    }

    protected boolean shouldRecordExtraScenario(CaseData caseData) {
        return false;
    }

    protected void beforeRecordScenario(CaseData caseData, String authToken) {
        // hook for subclasses
    }
}
