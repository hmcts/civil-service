package uk.gov.hmcts.reform.civil.ga.service.dashboardnotifications.makedecision;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.ga.enums.dq.GAJudgeDecisionOption;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.GAHearingNoticeApplication;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.GAHearingNoticeDetail;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.GAJudicialDecision;
import uk.gov.hmcts.reform.civil.ga.service.dashboardnotifications.makedecision.scenario.DecisionContext;
import uk.gov.hmcts.reform.civil.ga.service.dashboardnotifications.makedecision.scenario.HearingScheduledRule;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_GENERAL_APPLICATION_HEARING_SCHEDULED_APPLICANT;

class HearingScheduledRuleTest {

    private final HearingScheduledRule rule =
        new HearingScheduledRule(SCENARIO_AAA6_GENERAL_APPLICATION_HEARING_SCHEDULED_APPLICANT);

    @Test
    void shouldReturnScenarioWhenHearingScheduled() {
        GeneralApplicationCaseData caseData = GeneralApplicationCaseData.builder()
            .ccdState(CaseState.LISTING_FOR_A_HEARING)
            .judicialDecision(GAJudicialDecision.builder().decision(GAJudgeDecisionOption.LIST_FOR_A_HEARING).build())
            .gaHearingNoticeApplication(GAHearingNoticeApplication.builder().build())
            .gaHearingNoticeDetail(GAHearingNoticeDetail.builder().build())
            .build();

        assertThat(rule.evaluate(caseData, new DecisionContext(false, false, false, false)))
            .contains(SCENARIO_AAA6_GENERAL_APPLICATION_HEARING_SCHEDULED_APPLICANT.getScenario());
    }

    @Test
    void shouldReturnEmptyWhenHearingDetailsMissing() {
        GeneralApplicationCaseData caseData = GeneralApplicationCaseData.builder()
            .ccdState(CaseState.LISTING_FOR_A_HEARING)
            .judicialDecision(GAJudicialDecision.builder().decision(GAJudgeDecisionOption.LIST_FOR_A_HEARING).build())
            .gaHearingNoticeApplication(GAHearingNoticeApplication.builder().build())
            .build();

        assertThat(rule.evaluate(caseData, new DecisionContext(false, false, false, false))).isEmpty();
    }
}
