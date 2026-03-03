package uk.gov.hmcts.reform.civil.ga.service.dashboardnotifications.makedecision.scenario;

import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;

import java.util.Optional;

import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_GENERAL_APPLICATION_JUDGE_UNCLOAK_RESPONDENT;

public class JudgeUncloakedRule implements DecisionRule {

    @Override
    public Optional<String> evaluate(GeneralApplicationCaseData caseData, DecisionContext context) {
        boolean isWithoutNoticeAndUncloaked = isWithoutNotice(caseData) && isUncloaked(caseData);
        if (isWithoutNoticeAndUncloaked && caseData.getMakeAppVisibleToRespondents() != null) {
            return Optional.of(SCENARIO_AAA6_GENERAL_APPLICATION_JUDGE_UNCLOAK_RESPONDENT.getScenario());
        }
        return Optional.empty();
    }

    private boolean isWithoutNotice(GeneralApplicationCaseData caseData) {
        return caseData.getGeneralAppInformOtherParty() != null
            && NO.equals(caseData.getGeneralAppInformOtherParty().getIsWithNotice());
    }

    private boolean isUncloaked(GeneralApplicationCaseData caseData) {
        return caseData.getApplicationIsUncloakedOnce() != null
            && YES.equals(caseData.getApplicationIsUncloakedOnce());
    }
}
