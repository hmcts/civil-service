package uk.gov.hmcts.reform.civil.service.flowstate;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.flowstate.repository.AllowedEventRepository;
import uk.gov.hmcts.reform.civil.service.flowstate.scenario.AllowedEventsScenario;
import uk.gov.hmcts.reform.civil.stateflow.StateFlow;

import java.util.List;

import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_CLAIM_SPEC;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_LIP_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;

@Slf4j
@RequiredArgsConstructor
public class AllowedEventOrchestrator implements AllowedEventProvider {

    private final AllowedEventRepository repo;
    private final IStateFlowEngine stateFlowEngine;
    private final CaseDetailsConverter caseDetailsConverter;
    private final List<AllowedEventsScenario> scenarios;

    @Override
    public boolean isAllowed(CaseDetails caseDetails, CaseEvent event) {
        var caseData = caseDetailsConverter.toCaseData(caseDetails);
        return isAllowed(caseData, event);
    }

    public boolean isAllowed(CaseData caseData, CaseEvent event) {
        log.debug("Checking if event {} is allowed", event);

        // Preserve whitelist behaviour exactly as existing
        if (repo.isWhitelistEvent(event)) {
            log.debug("Event {} is whitelisted", event);
            return true;
        }

        log.debug("Finding Scenario ({}:{})",
                 MultiPartyScenario.getMultiPartyScenario(caseData),
                 caseData.getCaseAccessCategory());

        // Find scenario (or default) chain first match wins
        // AllowedEventsConfig.java for strict precedence
        AllowedEventsScenario scenario = scenarios.stream()
            .filter(s -> s.appliesTo(caseData))
            .findFirst()
            .orElse(null);

        if (scenario == null) {
            log.warn("Scenario not found ({}:{})",
                     MultiPartyScenario.getMultiPartyScenario(caseData),
                     caseData.getCaseAccessCategory());
            return false;
        }

        log.debug("Scenario found ({}:{})",
                  MultiPartyScenario.getMultiPartyScenario(caseData),
                  caseData.getCaseAccessCategory());

        // Use the same state selection logic as existing (evaluateSpec vs evaluate)
        StateFlow stateFlow = selectStateFlow(caseData, event);
        String stateFullName = stateFlow.getState().getName();

        return scenario.loadBaseEvents(stateFullName).contains(event);
    }

    private StateFlow selectStateFlow(CaseData caseData, CaseEvent event) {
        // Mirrors FlowStateAllowedEventService current path
        boolean isSpecOrLip = SPEC_CLAIM.equals(caseData.getCaseAccessCategory())
                || CREATE_CLAIM_SPEC.equals(event)
                || CREATE_LIP_CLAIM.equals(event);
        return isSpecOrLip ? stateFlowEngine.evaluateSpec(caseData) : stateFlowEngine.evaluate(caseData);
    }

}
