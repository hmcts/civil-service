package uk.gov.hmcts.reform.civil.ga.service.dashboardnotifications.makedecision;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.ga.enums.dq.GAJudgeRequestMoreInfoOption;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.GAJudicialRequestMoreInfo;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.GAJudicialWrittenRepresentations;
import uk.gov.hmcts.reform.civil.ga.service.JudicialDecisionHelper;
import uk.gov.hmcts.reform.civil.ga.service.dashboardnotifications.makedecision.scenario.DecisionContext;
import uk.gov.hmcts.reform.civil.ga.service.dashboardnotifications.makedecision.scenario.RequestMoreInfoApplicantRule;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_GENERAL_APPLICATION_ADDITIONAL_PAYMENT_APPLICANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_GENERAL_APPLICATION_REQUEST_MORE_INFO_APPLICANT;

@ExtendWith(MockitoExtension.class)
class ApplicantRequestMoreInfoRuleTest {

    @Mock
    private JudicialDecisionHelper judicialDecisionHelper;

    @Test
    void shouldReturnAdditionalPaymentScenarioWhenUncloakedWithAdditionalFee() {
        GeneralApplicationCaseData caseData = baseCase()
            .judicialDecisionRequestMoreInfo(
                new GAJudicialRequestMoreInfo().setRequestMoreInfoOption(GAJudgeRequestMoreInfoOption.SEND_APP_TO_OTHER_PARTY)
            )
            .build();
        when(judicialDecisionHelper.isApplicationUncloakedWithAdditionalFee(caseData)).thenReturn(true);

        RequestMoreInfoApplicantRule rule = new RequestMoreInfoApplicantRule(judicialDecisionHelper);
        DecisionContext context = DecisionContext.from(caseData);

        assertThat(rule.evaluate(caseData, context))
            .contains(SCENARIO_AAA6_GENERAL_APPLICATION_ADDITIONAL_PAYMENT_APPLICANT.getScenario());
    }

    @Test
    void shouldReturnRequestMoreInfoScenarioWhenNoAdditionalFee() {
        GeneralApplicationCaseData caseData = baseCase()
            .judicialDecisionRequestMoreInfo(
                new GAJudicialRequestMoreInfo().setRequestMoreInfoOption(GAJudgeRequestMoreInfoOption.SEND_APP_TO_OTHER_PARTY)
            )
            .build();
        when(judicialDecisionHelper.isApplicationUncloakedWithAdditionalFee(caseData)).thenReturn(false);

        RequestMoreInfoApplicantRule rule = new RequestMoreInfoApplicantRule(judicialDecisionHelper);
        DecisionContext context = DecisionContext.from(caseData);

        assertThat(rule.evaluate(caseData, context))
            .contains(SCENARIO_AAA6_GENERAL_APPLICATION_REQUEST_MORE_INFO_APPLICANT.getScenario());
    }

    @Test
    void shouldReturnEmptyWhenWrittenRepresentationsPresent() {
        GeneralApplicationCaseData caseData = baseCase()
            .judicialDecisionRequestMoreInfo(
                new GAJudicialRequestMoreInfo().setRequestMoreInfoOption(GAJudgeRequestMoreInfoOption.REQUEST_MORE_INFORMATION)
            )
            .judicialDecisionMakeAnOrderForWrittenRepresentations(new GAJudicialWrittenRepresentations())
            .build();

        RequestMoreInfoApplicantRule rule = new RequestMoreInfoApplicantRule(judicialDecisionHelper);
        DecisionContext context = DecisionContext.from(caseData);

        assertThat(rule.evaluate(caseData, context)).isEmpty();
    }

    private GeneralApplicationCaseData baseCase() {
        return new GeneralApplicationCaseData()
            .ccdState(CaseState.APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION)
            .build();
    }
}
