package uk.gov.hmcts.reform.civil.service.flowstate;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
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
@ConditionalOnProperty(prefix = "feature.allowed-event-orchestrator", name = "enabled", havingValue = "false", matchIfMissing = true)
@RequiredArgsConstructor
public class FlowStateAllowedEventService implements AllowedEventProvider {

    private final FlowStateAllowedEventsConfig config;
    private final IStateFlowEngine stateFlowEngine;
    private final CaseDetailsConverter caseDetailsConverter;

    public List<CaseEvent> getAllowedEvents(String stateFullName) {
        return config.getAllowedEvents(stateFullName);
    }

    public boolean isAllowedOnState(String stateFullName, CaseEvent caseEvent) {
        return config.getAllowedEvents(stateFullName).contains(caseEvent);
    }

    public boolean isAllowedOnStateForSpec(String stateFullName, CaseEvent caseEvent) {
        return config.getAllowedEventsSpec(stateFullName).contains(caseEvent);
    }

    @Override
    public boolean isAllowed(CaseDetails caseDetails, CaseEvent caseEvent) {
        return isAllowed(caseDetailsConverter.toCaseData(caseDetails), caseEvent);
    }

    public boolean isAllowed(CaseData caseData, CaseEvent event) {
        if (config.isWhitelistEvent(event)) {
            return true;
        }

        var isSpecOrLip = SPEC_CLAIM.equals(caseData.getCaseAccessCategory())
            || CREATE_CLAIM_SPEC.equals(event) || CREATE_LIP_CLAIM.equals(event);
        if (isSpecOrLip) {
            StateFlow stateFlow = stateFlowEngine.evaluateSpec(caseData);
            return isAllowedOnStateForSpec(stateFlow.getState().getName(), event);
        } else {
            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);
            return isAllowedOnState(stateFlow.getState().getName(), event);
        }
    }

    public List<String> getAllowedStates(CaseEvent caseEvent) {
        if (caseEvent.equals(CREATE_CLAIM_SPEC)) {
            return config.getAllowedStatesSpec(caseEvent);
        }
        return config.getAllowedStates(caseEvent);
    }
}
