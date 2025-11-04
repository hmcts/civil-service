package uk.gov.hmcts.reform.civil.service.flowstate;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.config.FlowStateAllowedEventsConfig;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.stateflow.StateFlow;

import java.util.List;

import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_CLAIM_SPEC;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_LIP_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;

@Service
@RequiredArgsConstructor
public class FlowStateAllowedEventService {

    private final FlowStateAllowedEventsConfig config;
    private final IStateFlowEngine stateFlowEngine;
    private final CaseDetailsConverter caseDetailsConverter;

    public FlowState getFlowState(CaseData caseData) {
        StateFlow stateFlow = stateFlowEngine.evaluate(caseData);
        return FlowState.fromFullName(stateFlow.getState().getName());
    }

    public List<CaseEvent> getAllowedEvents(String stateFullName) {
        return config.getAllowedEvents(stateFullName);
    }

    public boolean isAllowedOnState(String stateFullName, CaseEvent caseEvent) {
        return config.getAllowedEvents(stateFullName).contains(caseEvent);
    }

    public boolean isAllowedOnStateForSpec(String stateFullName, CaseEvent caseEvent) {
        return config.getAllowedEventsSpec(stateFullName).contains(caseEvent);
    }

    public boolean isAllowed(CaseDetails caseDetails, CaseEvent caseEvent) {
        if (config.isWhitelistEvent(caseEvent)) {
            return true;
        }

        CaseData caseData = caseDetailsConverter.toCaseData(caseDetails);
        var isSpecOrLip = SPEC_CLAIM.equals(caseData.getCaseAccessCategory())
            || CREATE_CLAIM_SPEC.equals(caseEvent) || CREATE_LIP_CLAIM.equals(caseEvent);
        if (isSpecOrLip) {
            StateFlow stateFlow = stateFlowEngine.evaluateSpec(caseDetails);
            return isAllowedOnStateForSpec(stateFlow.getState().getName(), caseEvent);
        } else {
            StateFlow stateFlow = stateFlowEngine.evaluate(caseDetails);
            return isAllowedOnState(stateFlow.getState().getName(), caseEvent);
        }
    }

    public List<String> getAllowedStates(CaseEvent caseEvent) {
        if (caseEvent.equals(CREATE_CLAIM_SPEC)) {
            return config.getAllowedStatesSpec(caseEvent);
        }
        return config.getAllowedStates(caseEvent);
    }
}
