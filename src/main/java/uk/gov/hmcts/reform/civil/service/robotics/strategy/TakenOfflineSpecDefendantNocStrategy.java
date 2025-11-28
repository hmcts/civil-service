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

import static uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsEventSupport.buildMiscEvent;

@Slf4j
@Component
@RequiredArgsConstructor
public class TakenOfflineSpecDefendantNocStrategy implements EventHistoryStrategy {

    private final RoboticsSequenceGenerator sequenceGenerator;
    private final RoboticsEventTextFormatter textFormatter;
    private final IStateFlowEngine stateFlowEngine;

    @Override
    public boolean supports(CaseData caseData) {
        return caseData != null
            && caseData.getTakenOfflineDate() != null
            && hasSpecDefendantNocState(caseData);
    }

    @Override
    public void contribute(EventHistory.EventHistoryBuilder builder, CaseData caseData, String authToken) {
        if (!supports(caseData)) {
            return;
        }

        log.info("Building taken offline spec defendant NOC robotics event for caseId {}", caseData.getCcdCaseReference());
        String message = textFormatter.noticeOfChangeFiled();
        builder.miscellaneous(buildMiscEvent(
            builder,
            sequenceGenerator,
            message,
            caseData.getTakenOfflineDate()
        ));
    }

    private boolean hasSpecDefendantNocState(CaseData caseData) {
        StateFlow stateFlow = stateFlowEngine.evaluate(caseData);
        return stateFlow.getStateHistory().stream()
            .map(State::getName)
            .anyMatch(name -> FlowState.Main.TAKEN_OFFLINE_SPEC_DEFENDANT_NOC.fullName().equals(name)
                || FlowState.Main.TAKEN_OFFLINE_SPEC_DEFENDANT_NOC_AFTER_JBA.fullName().equals(name));
    }
}
