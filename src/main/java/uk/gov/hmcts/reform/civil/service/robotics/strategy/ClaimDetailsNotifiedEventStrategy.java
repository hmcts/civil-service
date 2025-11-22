package uk.gov.hmcts.reform.civil.service.robotics.strategy;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.robotics.EventHistory;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowState;
import uk.gov.hmcts.reform.civil.service.flowstate.IStateFlowEngine;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsSequenceGenerator;
import uk.gov.hmcts.reform.civil.stateflow.StateFlow;
import uk.gov.hmcts.reform.civil.stateflow.model.State;

import static uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsEventSupport.buildMiscEvent;

@Component
@RequiredArgsConstructor
public class ClaimDetailsNotifiedEventStrategy implements EventHistoryStrategy {

    private static final String MESSAGE = "Claim details notified.";

    private final RoboticsSequenceGenerator sequenceGenerator;
    private final IStateFlowEngine stateFlowEngine;

    @Override
    public boolean supports(CaseData caseData) {
        return caseData != null
            && caseData.getClaimDetailsNotificationDate() != null
            && hasClaimDetailsNotifiedState(caseData);
    }

    @Override
    public void contribute(EventHistory.EventHistoryBuilder builder, CaseData caseData, String authToken) {
        if (!supports(caseData)) {
            return;
        }

        builder.miscellaneous(buildMiscEvent(
            builder,
            sequenceGenerator,
            MESSAGE,
            caseData.getClaimDetailsNotificationDate()
        ));
    }

    private boolean hasClaimDetailsNotifiedState(CaseData caseData) {
        StateFlow stateFlow = stateFlowEngine.evaluate(caseData);
        return stateFlow.getStateHistory().stream()
            .map(State::getName)
            .anyMatch(FlowState.Main.CLAIM_DETAILS_NOTIFIED.fullName()::equals);
    }
}
