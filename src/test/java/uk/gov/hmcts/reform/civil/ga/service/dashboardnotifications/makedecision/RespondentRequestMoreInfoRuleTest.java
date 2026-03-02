package uk.gov.hmcts.reform.civil.ga.service.dashboardnotifications.makedecision;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.ga.enums.dq.GAJudgeRequestMoreInfoOption;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.GAJudicialRequestMoreInfo;
import uk.gov.hmcts.reform.civil.ga.service.dashboardnotifications.makedecision.scenario.DecisionContext;
import uk.gov.hmcts.reform.civil.ga.service.dashboardnotifications.makedecision.scenario.RequestMoreInfoRespondentRule;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_GENERAL_APPLICATION_REQUEST_MORE_INFO_RESPONDENT;

class RespondentRequestMoreInfoRuleTest {

    private final RequestMoreInfoRespondentRule rule = new RequestMoreInfoRespondentRule();

    @Test
    void shouldReturnScenarioWhenRequestMoreInfoAndAwaitingDecisionState() {
        GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
            .ccdState(CaseState.APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION)
            .judicialDecisionRequestMoreInfo(new GAJudicialRequestMoreInfo()
                                                 .setRequestMoreInfoOption(GAJudgeRequestMoreInfoOption.REQUEST_MORE_INFORMATION))
            .build();

        assertThat(rule.evaluate(caseData, new DecisionContext(false, false, false, false)))
            .contains(SCENARIO_AAA6_GENERAL_APPLICATION_REQUEST_MORE_INFO_RESPONDENT.getScenario());
    }

    @Test
    void shouldReturnEmptyWhenOptionIsSendToOtherParty() {
        GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
            .ccdState(CaseState.APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION)
            .judicialDecisionRequestMoreInfo(
                new GAJudicialRequestMoreInfo().setRequestMoreInfoOption(GAJudgeRequestMoreInfoOption.SEND_APP_TO_OTHER_PARTY)
            )
            .build();

        assertThat(rule.evaluate(caseData, new DecisionContext(false, false, false, false))).isEmpty();
    }

    @Test
    void shouldReturnEmptyWhenStateIsNotAwaitingDecision() {
        GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
            .ccdState(CaseState.AWAITING_RESPONDENT_RESPONSE)
            .judicialDecisionRequestMoreInfo(
                new GAJudicialRequestMoreInfo().setRequestMoreInfoOption(GAJudgeRequestMoreInfoOption.REQUEST_MORE_INFORMATION)
            )
            .build();

        assertThat(rule.evaluate(caseData, new DecisionContext(false, false, false, false))).isEmpty();
    }
}
