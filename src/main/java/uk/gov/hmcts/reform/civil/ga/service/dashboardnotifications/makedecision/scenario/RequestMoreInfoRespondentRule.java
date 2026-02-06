package uk.gov.hmcts.reform.civil.ga.service.dashboardnotifications.makedecision.scenario;

import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.ga.enums.dq.GAJudgeRequestMoreInfoOption;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;

import java.util.Optional;

import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_GENERAL_APPLICATION_REQUEST_MORE_INFO_RESPONDENT;

public class RequestMoreInfoRespondentRule implements DecisionRule {

    @Override
    public Optional<String> evaluate(GeneralApplicationCaseData caseData, DecisionContext context) {
        return matches(caseData)
            ? Optional.of(SCENARIO_AAA6_GENERAL_APPLICATION_REQUEST_MORE_INFO_RESPONDENT.getScenario())
            : Optional.empty();
    }

    private boolean matches(GeneralApplicationCaseData caseData) {
        if (caseData.getJudicialDecisionRequestMoreInfo() == null) {
            return false;
        }
        return caseData.getJudicialDecisionRequestMoreInfo().getRequestMoreInfoOption()
            != GAJudgeRequestMoreInfoOption.SEND_APP_TO_OTHER_PARTY
            && CaseState.APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION.equals(caseData.getCcdState());
    }
}
