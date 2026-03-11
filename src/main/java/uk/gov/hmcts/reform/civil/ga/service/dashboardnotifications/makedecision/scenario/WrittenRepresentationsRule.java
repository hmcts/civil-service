package uk.gov.hmcts.reform.civil.ga.service.dashboardnotifications.makedecision.scenario;

import uk.gov.hmcts.reform.civil.ga.enums.dq.GAJudgeDecisionOption;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios;

import java.util.Optional;

public class WrittenRepresentationsRule implements DecisionRule {

    private final DashboardScenarios scenario;

    public WrittenRepresentationsRule(DashboardScenarios scenario) {
        this.scenario = scenario;
    }

    @Override
    public Optional<String> evaluate(GeneralApplicationCaseData caseData, DecisionContext context) {
        return isWrittenRepresentationsScenario(caseData)
            ? Optional.of(scenario.getScenario())
            : Optional.empty();
    }

    private boolean isWrittenRepresentationsScenario(GeneralApplicationCaseData caseData) {
        if (caseData.getJudicialDecisionMakeAnOrderForWrittenRepresentations() == null
            || caseData.getJudicialDecision() == null) {
            return false;
        }

        return GAJudgeDecisionOption.MAKE_ORDER_FOR_WRITTEN_REPRESENTATIONS
            .equals(caseData.getJudicialDecision().getDecision());
    }
}
