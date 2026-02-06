package uk.gov.hmcts.reform.civil.ga.service.dashboardnotifications.makedecision.scenario;

import uk.gov.hmcts.reform.civil.ga.enums.dq.GAJudgeRequestMoreInfoOption;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.ga.service.JudicialDecisionHelper;

import java.util.Optional;

import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_GENERAL_APPLICATION_ADDITIONAL_PAYMENT_APPLICANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_GENERAL_APPLICATION_REQUEST_MORE_INFO_APPLICANT;

public class RequestMoreInfoApplicantRule implements DecisionRule {

    private final JudicialDecisionHelper judicialDecisionHelper;

    public RequestMoreInfoApplicantRule(JudicialDecisionHelper judicialDecisionHelper) {
        this.judicialDecisionHelper = judicialDecisionHelper;
    }

    @Override
    public Optional<String> evaluate(GeneralApplicationCaseData caseData, DecisionContext context) {
        if (!isRequestMoreInfoScenario(caseData, context)) {
            return Optional.empty();
        }

        return isSendToOtherPartyWithAdditionalFee(caseData)
            ? Optional.of(SCENARIO_AAA6_GENERAL_APPLICATION_ADDITIONAL_PAYMENT_APPLICANT.getScenario())
            : Optional.of(SCENARIO_AAA6_GENERAL_APPLICATION_REQUEST_MORE_INFO_APPLICANT.getScenario());
    }

    private boolean isSendToOtherPartyWithAdditionalFee(GeneralApplicationCaseData caseData) {
        return GAJudgeRequestMoreInfoOption.SEND_APP_TO_OTHER_PARTY
            == caseData.getJudicialDecisionRequestMoreInfo().getRequestMoreInfoOption()
            && judicialDecisionHelper.isApplicationUncloakedWithAdditionalFee(caseData);
    }

    private boolean isRequestMoreInfoScenario(GeneralApplicationCaseData caseData, DecisionContext context) {
        if (context.hasWrittenRepresentationsOrder() || caseData.judgeHasMadeAnOrder()) {
            return false;
        }

        return context.hasRequestMoreInfo()
            && (context.isRequestMoreInfoDecision() || context.isAwaitingDecisionState());
    }
}
