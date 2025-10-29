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
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsSequenceGenerator;
import uk.gov.hmcts.reform.civil.stateflow.StateFlow;
import uk.gov.hmcts.reform.civil.stateflow.model.State;

/**
 * Emits the “Claimant has notified defendant.” event when the claim enters the CLAIM_NOTIFIED state.
 */
@Component
@Order(6)
@RequiredArgsConstructor
public class ClaimNotifiedEventContributor implements EventHistoryContributor {

    private static final String MESSAGE = "Claimant has notified defendant.";

    private final RoboticsSequenceGenerator sequenceGenerator;
    private final IStateFlowEngine stateFlowEngine;

    @Override
    public boolean supports(CaseData caseData) {
        return caseData != null
            && caseData.getClaimNotificationDate() != null
            && hasClaimNotifiedState(caseData);
    }

    @Override
    public void contribute(EventHistory.EventHistoryBuilder builder, CaseData caseData, String authToken) {
        if (!supports(caseData)) {
            return;
        }

        builder.miscellaneous(
            Event.builder()
                .eventSequence(sequenceGenerator.nextSequence(builder.build()))
                .eventCode(EventType.MISCELLANEOUS.getCode())
                .dateReceived(caseData.getClaimNotificationDate())
                .eventDetailsText(MESSAGE)
                .eventDetails(EventDetails.builder().miscText(MESSAGE).build())
                .build()
        );
    }

    private boolean hasClaimNotifiedState(CaseData caseData) {
        StateFlow stateFlow = stateFlowEngine.evaluate(caseData);
        return stateFlow.getStateHistory().stream()
            .map(State::getName)
            .anyMatch(FlowState.Main.CLAIM_NOTIFIED.fullName()::equals);
    }
}
