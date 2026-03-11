package uk.gov.hmcts.reform.civil.service.robotics.strategy;

import static org.apache.commons.lang3.StringUtils.left;
import static uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsEventSupport.buildMiscEvent;

import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.robotics.Event;
import uk.gov.hmcts.reform.civil.model.robotics.EventHistory;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowState;
import uk.gov.hmcts.reform.civil.service.flowstate.IStateFlowEngine;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsEventTextFormatter;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsSequenceGenerator;
import uk.gov.hmcts.reform.civil.stateflow.StateFlow;
import uk.gov.hmcts.reform.civil.stateflow.model.State;

@Slf4j
@Component
@RequiredArgsConstructor
public class SdoNotDrawnStrategy implements EventHistoryStrategy {

    private static final int MAX_TEXT_LENGTH = 250;

    private final RoboticsSequenceGenerator sequenceGenerator;
    private final RoboticsEventTextFormatter textFormatter;
    private final IStateFlowEngine stateFlowEngine;

    @Override
    public boolean supports(CaseData caseData) {
        return caseData != null && hasSdoNotDrawnState(caseData);
    }

    @Override
    public void contribute(EventHistory eventHistory, CaseData caseData, String authToken) {
        if (!supports(caseData)) {
            return;
        }
        log.info("Building SDO not drawn robotics event for caseId {}", caseData.getCcdCaseReference());

        String reason = caseData.getReasonNotSuitableSDO().getInput();
        String message =
                left(
                        textFormatter.caseProceedOffline(
                                "Judge / Legal Advisor did not draw a Direction's Order: " + reason),
                        MAX_TEXT_LENGTH);

        List<Event> updatedMiscellaneousEvents1 =
                eventHistory.getMiscellaneous() == null
                        ? new ArrayList<>()
                        : new ArrayList<>(eventHistory.getMiscellaneous());
        updatedMiscellaneousEvents1.add(
                buildMiscEvent(eventHistory, sequenceGenerator, message, caseData.getUnsuitableSDODate()));
        eventHistory.setMiscellaneous(updatedMiscellaneousEvents1);
    }

    private boolean hasSdoNotDrawnState(CaseData caseData) {
        StateFlow stateFlow = stateFlowEngine.evaluate(caseData);
        return stateFlow.getStateHistory().stream()
                .map(State::getName)
                .anyMatch(FlowState.Main.TAKEN_OFFLINE_SDO_NOT_DRAWN.fullName()::equals);
    }
}
