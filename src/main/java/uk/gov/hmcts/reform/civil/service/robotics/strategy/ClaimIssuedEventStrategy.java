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

@Component
@Order(5)
@RequiredArgsConstructor
public class ClaimIssuedEventStrategy implements EventHistoryStrategy {

    private static final String CLAIM_ISSUED_TEXT = "Claim issued in CCD.";

    private final RoboticsSequenceGenerator sequenceGenerator;
    private final IStateFlowEngine stateFlowEngine;

    @Override
    public boolean supports(CaseData caseData) {
        return caseData != null
            && caseData.getIssueDate() != null
            && hasClaimIssuedState(caseData);
    }

    @Override
    public void contribute(EventHistory.EventHistoryBuilder builder, CaseData caseData, String authToken) {
        if (!supports(caseData)) {
            return;
        }

        Event event = Event.builder()
            .eventSequence(sequenceGenerator.nextSequence(builder.build()))
            .eventCode(EventType.MISCELLANEOUS.getCode())
            .dateReceived(caseData.getIssueDate().atStartOfDay())
            .eventDetailsText(CLAIM_ISSUED_TEXT)
            .eventDetails(EventDetails.builder().miscText(CLAIM_ISSUED_TEXT).build())
            .build();

        builder.miscellaneous(event);
    }

    private boolean hasClaimIssuedState(CaseData caseData) {
        StateFlow stateFlow = stateFlowEngine.evaluate(caseData);
        return stateFlow.getStateHistory().stream()
            .map(State::getName)
            .anyMatch(FlowState.Main.CLAIM_ISSUED.fullName()::equals);
    }
}
