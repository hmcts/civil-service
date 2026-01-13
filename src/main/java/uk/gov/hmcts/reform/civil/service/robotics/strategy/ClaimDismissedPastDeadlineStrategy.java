package uk.gov.hmcts.reform.civil.service.robotics.strategy;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.robotics.EventHistory;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowState;
import uk.gov.hmcts.reform.civil.service.flowstate.IStateFlowEngine;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsEventTextFormatter;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsSequenceGenerator;
import uk.gov.hmcts.reform.civil.stateflow.StateFlow;
import uk.gov.hmcts.reform.civil.stateflow.model.State;

import java.util.List;

import static uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsEventSupport.buildMiscEvent;

@Slf4j
@Component
@RequiredArgsConstructor
public class ClaimDismissedPastDeadlineStrategy implements EventHistoryStrategy {

    private final RoboticsSequenceGenerator sequenceGenerator;
    private final RoboticsEventTextFormatter textFormatter;
    private final IStateFlowEngine stateFlowEngine;

    @Override
    public boolean supports(CaseData caseData) {
        return caseData != null
            && caseData.getClaimDismissedDate() != null
            && hasRequiredHistory(caseData)
            && isDismissedPastDeadline(caseData);
    }

    @Override
    public void contribute(EventHistory.EventHistoryBuilder builder, CaseData caseData, String authToken) {
        if (!supports(caseData)) {
            return;
        }
        log.info("Building claim dismissed past deadline robotics event for caseId {}", caseData.getCcdCaseReference());

        FlowState.Main previous = determinePreviousState(caseData);
        String message = switch (previous) {
            case CLAIM_NOTIFIED, CLAIM_DETAILS_NOTIFIED ->
                textFormatter.claimDismissedAfterNoDefendantResponse();
            case NOTIFICATION_ACKNOWLEDGED,
                NOTIFICATION_ACKNOWLEDGED_TIME_EXTENSION,
                CLAIM_DETAILS_NOTIFIED_TIME_EXTENSION,
                PAST_CLAIM_DISMISSED_DEADLINE_AWAITING_CAMUNDA,
                PAST_CLAIM_NOTIFICATION_DEADLINE_AWAITING_CAMUNDA ->
                textFormatter.claimDismissedNoUserActionForSixMonths();
            default ->
                throw new IllegalStateException("Unexpected flow state " + previous.fullName());
        };

        builder.miscellaneous(buildMiscEvent(builder, sequenceGenerator, message, caseData.getClaimDismissedDate()));
    }

    private boolean hasRequiredHistory(CaseData caseData) {
        List<State> history = stateFlowEngine.evaluate(caseData).getStateHistory();
        return history.size() > 1;
    }

    private boolean isDismissedPastDeadline(CaseData caseData) {
        return stateFlowEngine.evaluate(caseData).getStateHistory().stream()
            .map(State::getName)
            .anyMatch(name -> FlowState.Main.CLAIM_DISMISSED_PAST_CLAIM_DISMISSED_DEADLINE.fullName().equals(name));
    }

    private FlowState.Main determinePreviousState(CaseData caseData) {
        StateFlow stateFlow = stateFlowEngine.evaluate(caseData);
        List<State> history = stateFlow.getStateHistory();
        if (history.size() <= 1) {
            throw new IllegalStateException("Flow state history should have at least two items: " + history);
        }
        State previous = history.get(history.size() - 2);
        return (FlowState.Main) FlowState.fromFullName(previous.getName());
    }
}
