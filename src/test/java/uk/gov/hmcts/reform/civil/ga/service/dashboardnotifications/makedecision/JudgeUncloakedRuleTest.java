package uk.gov.hmcts.reform.civil.ga.service.dashboardnotifications.makedecision;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.ga.enums.MakeAppAvailableCheckGAspec;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.GAMakeApplicationAvailableCheck;
import uk.gov.hmcts.reform.civil.ga.service.dashboardnotifications.makedecision.scenario.DecisionContext;
import uk.gov.hmcts.reform.civil.ga.service.dashboardnotifications.makedecision.scenario.JudgeUncloakedRule;
import uk.gov.hmcts.reform.civil.model.genapplication.GAInformOtherParty;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_GENERAL_APPLICATION_JUDGE_UNCLOAK_RESPONDENT;

class JudgeUncloakedRuleTest {

    private final JudgeUncloakedRule rule = new JudgeUncloakedRule();

    @Test
    void shouldReturnScenarioWhenWithoutNoticeAndUncloakedAndVisible() {
        GeneralApplicationCaseData caseData = baseCase()
            .applicationIsUncloakedOnce(YesOrNo.YES)
            .makeAppVisibleToRespondents(
                new GAMakeApplicationAvailableCheck()
                    .setMakeAppAvailableCheck(List.of(MakeAppAvailableCheckGAspec.CONSENT_AGREEMENT_CHECKBOX))
            );

        assertThat(rule.evaluate(caseData, new DecisionContext(false, false, false, false)))
            .contains(SCENARIO_AAA6_GENERAL_APPLICATION_JUDGE_UNCLOAK_RESPONDENT.getScenario());
    }

    @Test
    void shouldReturnEmptyWhenNotVisibleToRespondent() {
        GeneralApplicationCaseData caseData = baseCase()
            .applicationIsUncloakedOnce(YesOrNo.YES)
            .build();

        assertThat(rule.evaluate(caseData, new DecisionContext(false, false, false, false))).isEmpty();
    }

    private GeneralApplicationCaseData baseCase() {
        return new GeneralApplicationCaseData()
            .generalAppInformOtherParty(GAInformOtherParty.builder().isWithNotice(YesOrNo.NO).build())
            .build();
    }
}
