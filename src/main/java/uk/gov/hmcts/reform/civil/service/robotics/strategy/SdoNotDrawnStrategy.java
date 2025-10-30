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

import static org.apache.commons.lang3.StringUtils.left;

@Component
@Order(33)
@RequiredArgsConstructor
public class SdoNotDrawnStrategy implements EventHistoryStrategy {

    private static final int MAX_TEXT_LENGTH = 250;

    private final RoboticsSequenceGenerator sequenceGenerator;
    private final RoboticsEventTextFormatter textFormatter;
    private final IStateFlowEngine stateFlowEngine;

    @Override
    public boolean supports(CaseData caseData) {
        return caseData != null
            && caseData.getUnsuitableSDODate() != null
            && caseData.getReasonNotSuitableSDO() != null
            && caseData.getReasonNotSuitableSDO().getInput() != null
            && hasSdoNotDrawnState(caseData);
    }

    @Override
    public void contribute(EventHistory.EventHistoryBuilder builder, CaseData caseData, String authToken) {
        if (!supports(caseData)) {
            return;
        }

        String reason = caseData.getReasonNotSuitableSDO().getInput();
        String message = left(
            textFormatter.caseProceedOffline("Judge / Legal Advisor did not draw a Direction's Order: " + reason),
            MAX_TEXT_LENGTH
        );

        builder.miscellaneous(Event.builder()
            .eventSequence(sequenceGenerator.nextSequence(builder.build()))
            .eventCode(EventType.MISCELLANEOUS.getCode())
            .dateReceived(caseData.getUnsuitableSDODate())
            .eventDetailsText(message)
            .eventDetails(EventDetails.builder().miscText(message).build())
            .build());
    }

    private boolean hasSdoNotDrawnState(CaseData caseData) {
        StateFlow stateFlow = stateFlowEngine.evaluate(caseData);
        return stateFlow.getStateHistory().stream()
            .map(State::getName)
            .anyMatch(FlowState.Main.TAKEN_OFFLINE_SDO_NOT_DRAWN.fullName()::equals);
    }
}
