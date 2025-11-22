package uk.gov.hmcts.reform.civil.service.robotics.strategy;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.robotics.EventHistory;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowState;
import uk.gov.hmcts.reform.civil.service.flowstate.IStateFlowEngine;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsEventTextFormatter;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsSequenceGenerator;
import uk.gov.hmcts.reform.civil.stateflow.StateFlow;
import uk.gov.hmcts.reform.civil.stateflow.model.State;

import static uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsEventSupport.buildMiscEvent;

@Component
@RequiredArgsConstructor
public class CaseProceedsInCasemanStrategy implements EventHistoryStrategy {

    private final RoboticsEventTextFormatter textFormatter;
    private final RoboticsSequenceGenerator sequenceGenerator;
    private final IStateFlowEngine stateFlowEngine;

    @Override
    public boolean supports(CaseData caseData) {
        if (caseData == null || caseData.getTakenOfflineDate() == null) {
            return false;
        }

        StateFlow stateFlow = stateFlowEngine.evaluate(caseData);
        boolean takenOfflineAfterSdoState = hasState(stateFlow);

        if (takenOfflineAfterSdoState) {
            return true;
        }

        boolean sdoDrawnAndFiled = caseData.getOrderSDODocumentDJ() != null
            || (caseData.getOrderSDODocumentDJCollection() != null
            && !caseData.getOrderSDODocumentDJCollection().isEmpty());

        boolean takenOfflineAfterSdo = YesOrNo.NO.equals(caseData.getDrawDirectionsOrderRequired())
            && caseData.getReasonNotSuitableSDO() == null;

        return sdoDrawnAndFiled || takenOfflineAfterSdo;
    }

    @Override
    public void contribute(EventHistory.EventHistoryBuilder builder, CaseData caseData, String authToken) {
        if (!supports(caseData)) {
            return;
        }
        String message = textFormatter.caseProceedsInCaseman();
        builder.miscellaneous(buildMiscEvent(builder, sequenceGenerator, message, caseData.getTakenOfflineDate()));
    }

    private boolean hasState(StateFlow stateFlow) {
        if (stateFlow == null) {
            return false;
        }
        return stateFlow.getStateHistory().stream()
            .map(State::getName)
            .anyMatch(FlowState.Main.TAKEN_OFFLINE_AFTER_SDO.fullName()::equals);
    }
}
