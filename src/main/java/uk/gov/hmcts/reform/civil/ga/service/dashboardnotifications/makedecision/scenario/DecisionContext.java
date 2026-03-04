package uk.gov.hmcts.reform.civil.ga.service.dashboardnotifications.makedecision.scenario;

import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.ga.enums.dq.GAJudgeRequestMoreInfoOption;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;

import java.util.List;

public record DecisionContext(boolean isAwaitingDecisionState, boolean hasWrittenRepresentationsOrder,
                              boolean hasRequestMoreInfo, boolean isRequestMoreInfoDecision) {

    private static final List<CaseState> AWAITING_DECISION_STATES = List.of(
        CaseState.APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION,
        CaseState.ADDITIONAL_RESPONSE_TIME_EXPIRED
    );

    public static DecisionContext from(GeneralApplicationCaseData caseData) {
        CaseState ccdState = caseData.getCcdState();
        boolean isAwaitingDecisionState = ccdState != null && AWAITING_DECISION_STATES.contains(ccdState);
        boolean hasWrittenRepresentationsOrder = caseData.getJudicialDecisionMakeAnOrderForWrittenRepresentations() != null;
        var requestMoreInfo = caseData.getJudicialDecisionRequestMoreInfo();
        boolean hasRequestMoreInfo = requestMoreInfo != null;
        boolean isRequestMoreInfoDecision = requestMoreInfo != null
            && requestMoreInfo.getRequestMoreInfoOption() == GAJudgeRequestMoreInfoOption.REQUEST_MORE_INFORMATION;

        return new DecisionContext(
            isAwaitingDecisionState,
            hasWrittenRepresentationsOrder,
            hasRequestMoreInfo,
            isRequestMoreInfoDecision
        );
    }
}
