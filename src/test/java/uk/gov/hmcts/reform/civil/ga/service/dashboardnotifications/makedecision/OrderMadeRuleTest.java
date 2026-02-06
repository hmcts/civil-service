package uk.gov.hmcts.reform.civil.ga.service.dashboardnotifications.makedecision;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.ga.enums.dq.GAJudgeDecisionOption;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.GAJudicialDecision;
import uk.gov.hmcts.reform.civil.ga.service.dashboardnotifications.makedecision.scenario.DecisionContext;
import uk.gov.hmcts.reform.civil.ga.service.dashboardnotifications.makedecision.scenario.OrderMadeRule;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_GENERAL_APPLICATION_ORDER_MADE_APPLICANT;

class OrderMadeRuleTest {

    private final OrderMadeRule rule = new OrderMadeRule(SCENARIO_AAA6_GENERAL_APPLICATION_ORDER_MADE_APPLICANT);

    @Test
    void shouldReturnScenarioWhenOrderMadeAndAwaitingDecisionState() {
        GeneralApplicationCaseData caseData = GeneralApplicationCaseData.builder()
            .judicialDecision(GAJudicialDecision.builder().decision(GAJudgeDecisionOption.MAKE_AN_ORDER).build())
            .build();

        assertThat(rule.evaluate(caseData, new DecisionContext(true, false, false, false)))
            .contains(SCENARIO_AAA6_GENERAL_APPLICATION_ORDER_MADE_APPLICANT.getScenario());
    }

    @Test
    void shouldReturnEmptyWhenNotAwaitingDecisionState() {
        GeneralApplicationCaseData caseData = GeneralApplicationCaseData.builder()
            .judicialDecision(GAJudicialDecision.builder().decision(GAJudgeDecisionOption.MAKE_AN_ORDER).build())
            .build();

        assertThat(rule.evaluate(caseData, new DecisionContext(false, false, false, false))).isEmpty();
    }
}
