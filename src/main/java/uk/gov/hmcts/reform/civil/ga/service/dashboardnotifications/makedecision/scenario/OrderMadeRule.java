package uk.gov.hmcts.reform.civil.ga.service.dashboardnotifications.makedecision.scenario;

import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios;

import java.util.Optional;

public class OrderMadeRule implements DecisionRule {

    private final DashboardScenarios scenario;

    public OrderMadeRule(DashboardScenarios scenario) {
        this.scenario = scenario;
    }

    @Override
    public Optional<String> evaluate(GeneralApplicationCaseData caseData, DecisionContext context) {
        if (isOrderMadeAwaitingDecisionScenario(caseData, context)) {
            return Optional.of(scenario.getScenario());
        }
        return Optional.empty();
    }

    private boolean isOrderMadeAwaitingDecisionScenario(GeneralApplicationCaseData caseData, DecisionContext context) {
        return caseData.judgeHasMadeAnOrder() && context.isAwaitingDecisionState();
    }
}
