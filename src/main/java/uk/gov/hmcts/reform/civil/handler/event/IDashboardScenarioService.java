package uk.gov.hmcts.reform.civil.handler.event;

import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;

public interface IDashboardScenarioService {

    void createScenario(String bearerToken, DashboardScenarios scenario, String caseReference, ScenarioRequestParams params);
}
