package uk.gov.hmcts.reform.civil.service.robotics.strategy;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.robotics.EventHistory;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowState;
import uk.gov.hmcts.reform.civil.service.flowstate.IStateFlowEngine;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsSequenceGenerator;
import uk.gov.hmcts.reform.civil.stateflow.StateFlow;
import uk.gov.hmcts.reform.civil.stateflow.model.State;

import static uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsEventSupport.buildMiscEvent;

@Slf4j
@Component
@RequiredArgsConstructor
public class ClaimNotifiedEventStrategy implements EventHistoryStrategy {

    private static final String MESSAGE = "Claimant has notified defendant.";

    private final RoboticsSequenceGenerator sequenceGenerator;
    private final IStateFlowEngine stateFlowEngine;

    @Override
    public boolean supports(CaseData caseData) {
        return caseData != null
            && hasClaimNotifiedState(caseData);
    }

    @Override
    public void contribute(EventHistory.EventHistoryBuilder builder, CaseData caseData, String authToken) {
        if (!supports(caseData)) {
            return;
        }
        log.info("Building claim notified robotics events for caseId {}", caseData.getCcdCaseReference());

        builder.miscellaneous(buildMiscEvent(
            builder,
            sequenceGenerator,
            MESSAGE,
            caseData.getClaimNotificationDate()
        ));
    }

    private boolean hasClaimNotifiedState(CaseData caseData) {
        StateFlow stateFlow = stateFlowEngine.evaluate(caseData);
        return stateFlow.getStateHistory().stream()
            .map(State::getName)
            .anyMatch(FlowState.Main.CLAIM_NOTIFIED.fullName()::equals);
    }
}
