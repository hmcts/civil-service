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
public class CaseProceedsInCasemanStrategy implements EventHistoryStrategy {

    private final RoboticsEventTextFormatter textFormatter;
    private final RoboticsSequenceGenerator sequenceGenerator;
    private final IStateFlowEngine stateFlowEngine;

    @Override
    public boolean supports(CaseData caseData) {
        if (caseData == null) {
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

        return sdoDrawnAndFiled && caseData.getTakenOfflineDate() != null;
    }

    @Override
    public void contribute(EventHistory.EventHistoryBuilder builder, CaseData caseData, String authToken) {
        if (!supports(caseData)) {
            return;
        }
        log.info("Building case proceeds in Caseman robotics event for caseId {}", caseData.getCcdCaseReference());
        String message = textFormatter.caseProceedsInCaseman();
        boolean hasState = hasState(stateFlowEngine.evaluate(caseData));
        boolean hasSdoDocument = caseData.getTakenOfflineDate() != null
            && caseData.getOrderSDODocumentDJ() != null;

        builder.miscellaneous(buildMiscEvent(builder, sequenceGenerator, message, caseData.getTakenOfflineDate()));
        if (hasState && hasSdoDocument) {
            builder.miscellaneous(buildMiscEvent(builder, sequenceGenerator, message, caseData.getTakenOfflineDate()));
        }
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
