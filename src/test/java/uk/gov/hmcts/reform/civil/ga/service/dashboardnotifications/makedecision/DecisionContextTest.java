package uk.gov.hmcts.reform.civil.ga.service.dashboardnotifications.makedecision;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.ga.enums.dq.GAJudgeRequestMoreInfoOption;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.GAJudicialRequestMoreInfo;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.GAJudicialWrittenRepresentations;
import uk.gov.hmcts.reform.civil.ga.service.dashboardnotifications.makedecision.scenario.DecisionContext;

import static org.assertj.core.api.Assertions.assertThat;

class DecisionContextTest {

    @Test
    void shouldBuildContextFromCaseData() {
        GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
            .ccdState(CaseState.APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION)
            .judicialDecisionMakeAnOrderForWrittenRepresentations(new GAJudicialWrittenRepresentations())
            .judicialDecisionRequestMoreInfo(
                new GAJudicialRequestMoreInfo().setRequestMoreInfoOption(GAJudgeRequestMoreInfoOption.REQUEST_MORE_INFORMATION)
            )
            .build();

        DecisionContext context = DecisionContext.from(caseData);

        assertThat(context.isAwaitingDecisionState()).isTrue();
        assertThat(context.hasWrittenRepresentationsOrder()).isTrue();
        assertThat(context.hasRequestMoreInfo()).isTrue();
        assertThat(context.isRequestMoreInfoDecision()).isTrue();
    }

    @Test
    void shouldHandleMissingFields() {
        GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
            .build();

        DecisionContext context = DecisionContext.from(caseData);

        assertThat(context.isAwaitingDecisionState()).isFalse();
        assertThat(context.hasWrittenRepresentationsOrder()).isFalse();
        assertThat(context.hasRequestMoreInfo()).isFalse();
        assertThat(context.isRequestMoreInfoDecision()).isFalse();
    }
}
