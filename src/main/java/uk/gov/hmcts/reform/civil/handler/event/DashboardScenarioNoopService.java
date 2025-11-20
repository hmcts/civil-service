package uk.gov.hmcts.reform.civil.handler.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;

/**
 * Lightweight stub used when persistence is disabled (e.g. contract tests).
 */
@Service
@Slf4j
@ConditionalOnProperty(value = "app.jpa.enabled", havingValue = "false")
public class DashboardScenarioNoopService implements IDashboardScenarioService {

    @Override
    public void createScenario(String bearerToken,
                               DashboardScenarios scenario,
                               String caseReference,
                               ScenarioRequestParams params) {
        log.debug("Skipping dashboard scenario {} for case {} because dashboard persistence is disabled",
            scenario, caseReference);
    }
}
