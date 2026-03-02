package uk.gov.hmcts.reform.civil.ga.service.dashboardnotifications.makedecision.scenario;

import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.ga.enums.dq.GAJudgeDecisionOption;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios;

import java.util.Optional;

public class HearingScheduledRule implements DecisionRule {

    private final DashboardScenarios scenario;

    public HearingScheduledRule(DashboardScenarios scenario) {
        this.scenario = scenario;
    }

    @Override
    public Optional<String> evaluate(GeneralApplicationCaseData caseData, DecisionContext context) {
        if (isHearingScheduledScenario(caseData)) {
            return Optional.of(scenario.getScenario());
        }
        return Optional.empty();
    }

    private boolean isHearingScheduledScenario(GeneralApplicationCaseData caseData) {
        if (caseData.getGaHearingNoticeApplication() == null
            || caseData.getGaHearingNoticeDetail() == null
            || caseData.getJudicialDecision() == null) {
            return false;
        }

        return CaseState.LISTING_FOR_A_HEARING.equals(caseData.getCcdState())
            && GAJudgeDecisionOption.LIST_FOR_A_HEARING.equals(caseData.getJudicialDecision().getDecision());
    }
}
