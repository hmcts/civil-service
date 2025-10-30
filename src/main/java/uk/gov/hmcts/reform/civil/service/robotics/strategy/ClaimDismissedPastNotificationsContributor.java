package uk.gov.hmcts.reform.civil.service.robotics.strategy;

import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.robotics.Event;
import uk.gov.hmcts.reform.civil.model.robotics.EventDetails;
import uk.gov.hmcts.reform.civil.model.robotics.EventHistory;
import uk.gov.hmcts.reform.civil.model.robotics.EventType;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowState;
import uk.gov.hmcts.reform.civil.service.flowstate.IStateFlowEngine;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsEventTextFormatter;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsSequenceGenerator;
import uk.gov.hmcts.reform.civil.stateflow.StateFlow;
import uk.gov.hmcts.reform.civil.stateflow.model.State;

import java.util.EnumMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

@Component
@Order(11)
@RequiredArgsConstructor
public class ClaimDismissedPastNotificationsContributor implements EventHistoryContributor {

    private static final Map<FlowState.Main, MessageResolver> MESSAGE_RESOLVERS = new EnumMap<>(FlowState.Main.class);

    static {
        MESSAGE_RESOLVERS.put(
            FlowState.Main.CLAIM_DISMISSED_PAST_CLAIM_NOTIFICATION_DEADLINE,
            RoboticsEventTextFormatter::claimDismissedNoActionSinceIssue
        );
        MESSAGE_RESOLVERS.put(
            FlowState.Main.CLAIM_DISMISSED_PAST_CLAIM_DETAILS_NOTIFICATION_DEADLINE,
            RoboticsEventTextFormatter::claimDismissedNoClaimDetailsWithinWindow
        );
    }

    private final RoboticsSequenceGenerator sequenceGenerator;
    private final RoboticsEventTextFormatter textFormatter;
    private final IStateFlowEngine stateFlowEngine;

    @Override
    public boolean supports(CaseData caseData) {
        return caseData != null
            && caseData.getClaimDismissedDate() != null
            && hasRelevantState(caseData);
    }

    @Override
    public void contribute(EventHistory.EventHistoryBuilder builder, CaseData caseData, String authToken) {
        if (!supports(caseData)) {
            return;
        }

        Set<FlowState.Main> matchedStates = orderedMatchedStates(caseData);
        matchedStates.forEach(state -> emitDismissedEvent(builder, caseData, state));
    }

    private void emitDismissedEvent(EventHistory.EventHistoryBuilder builder, CaseData caseData, FlowState.Main state) {
        String message = MESSAGE_RESOLVERS.get(state).resolve(textFormatter);
        builder.miscellaneous(
            Event.builder()
                .eventSequence(sequenceGenerator.nextSequence(builder.build()))
                .eventCode(EventType.MISCELLANEOUS.getCode())
                .dateReceived(caseData.getClaimDismissedDate())
                .eventDetailsText(message)
                .eventDetails(EventDetails.builder().miscText(message).build())
                .build()
        );
    }

    private Set<FlowState.Main> orderedMatchedStates(CaseData caseData) {
        StateFlow stateFlow = stateFlowEngine.evaluate(caseData);
        Set<FlowState.Main> matched = new LinkedHashSet<>();
        stateFlow.getStateHistory().stream()
            .map(State::getName)
            .map(FlowState::fromFullName)
            .filter(MESSAGE_RESOLVERS::containsKey)
            .map(FlowState.Main.class::cast)
            .forEach(matched::add);
        return matched;
    }

    private boolean hasRelevantState(CaseData caseData) {
        return !orderedMatchedStates(caseData).isEmpty();
    }

    @FunctionalInterface
    private interface MessageResolver {
        String resolve(RoboticsEventTextFormatter formatter);
    }
}
