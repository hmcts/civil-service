package uk.gov.hmcts.reform.civil.service.robotics.strategy;

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
public class TakenOfflinePastApplicantResponseStrategy implements EventHistoryStrategy {

    private final RoboticsSequenceGenerator sequenceGenerator;
    private final RoboticsEventTextFormatter textFormatter;
    private final IStateFlowEngine stateFlowEngine;

    @Override
    public boolean supports(CaseData caseData) {
        return caseData != null && hasPastApplicantResponseState(caseData);
    }

    @Override
    public void contribute(EventHistory eventHistory, CaseData caseData, String authToken) {
        if (!supports(caseData)) {
            return;
        }

        log.info(
                "Building taken offline past applicant response robotics event for caseId {}",
                caseData.getCcdCaseReference());
        String message = textFormatter.claimMovedOfflineAfterApplicantResponseDeadline();
        List<Event> updatedMiscellaneousEvents1 =
                eventHistory.getMiscellaneous() == null
                        ? new ArrayList<>()
                        : new ArrayList<>(eventHistory.getMiscellaneous());
        updatedMiscellaneousEvents1.add(
                buildMiscEvent(eventHistory, sequenceGenerator, message, caseData.getTakenOfflineDate()));
        eventHistory.setMiscellaneous(updatedMiscellaneousEvents1);
    }

    private boolean hasPastApplicantResponseState(CaseData caseData) {
        StateFlow stateFlow = stateFlowEngine.evaluate(caseData);
        return stateFlow.getStateHistory().stream()
                .map(State::getName)
                .anyMatch(FlowState.Main.TAKEN_OFFLINE_PAST_APPLICANT_RESPONSE_DEADLINE.fullName()::equals);
    }
}
