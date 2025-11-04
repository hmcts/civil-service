package uk.gov.hmcts.reform.civil.service.flowstate;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.config.FlowStateAllowedEventsConfig;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.flowstate.scenario.AllowedEventsScenario;
import uk.gov.hmcts.reform.civil.stateflow.StateFlow;

import java.util.List;
import java.util.Set;

import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_CLAIM_SPEC;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_LIP_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;

@Slf4j
@RequiredArgsConstructor
public class AllowedEventsOrchestrator implements AllowedEvents{

    private final FlowStateAllowedEventsConfig config;
    private final IStateFlowEngine stateFlowEngine;
    private final CaseDetailsConverter caseDetailsConverter;
    private final List<AllowedEventsScenario> scenarios;

    public boolean isAllowed(CaseDetails caseDetails, CaseEvent event) {
        // TODO: temporary overload for backward compatability
        CaseData caseData = caseDetailsConverter.toCaseData(caseDetails);
        return isAllowed(caseData, event);
    }

    public boolean isAllowed(CaseData caseData, CaseEvent event) {
        // Preserve whitelist behaviour exactly as existing
        if (config.isWhitelistEvent(event)) {
            return true;
        }

        // Use the same state selection logic as existing (evaluateSpec vs evaluate)
        StateFlow stateFlow = selectStateFlow(caseData, event);
        String stateFullName = stateFlow.getState().getName();

        // Find scenario (or default) chain first match wins
        // AllowedEventsConfig.java for strict precedence
        AllowedEventsScenario scenario = scenarios.stream()
            .filter(s -> s.appliesTo(caseData))
            .findFirst()
            .orElseGet(() -> new DefaultCatchAllScenario(config));

        // Base events then filters
        // TODO: when decoupled from legacy sort out State usage and decommision test only methods
        Set<CaseEvent> base = scenario.loadBaseEvents(caseData, stateFullName);
        Set<CaseEvent> finalEvents = scenario.applyFilters(caseData, stateFullName, base);

        return finalEvents.contains(event);
    }

    private StateFlow selectStateFlow(CaseData caseData, CaseEvent event) {
        // Mirrors FlowStateAllowedEventService current path
        boolean isSpecOrLip = SPEC_CLAIM.equals(caseData.getCaseAccessCategory())
                || CREATE_CLAIM_SPEC.equals(event)
                || CREATE_LIP_CLAIM.equals(event);
        return isSpecOrLip ? stateFlowEngine.evaluateSpec(caseData) : stateFlowEngine.evaluate(caseData);
    }

    // Minimal default scenario to maintain current behaviour if no match
    static final class DefaultCatchAllScenario extends AllowedEventsScenario {
        DefaultCatchAllScenario(FlowStateAllowedEventsConfig config) { super(config); }
        public boolean appliesTo(CaseData cd) {
            log.debug("Default catch-all scenario applied for case data: {} {}",
                      MultiPartyScenario.getMultiPartyScenario(cd), cd.getCaseAccessCategory());
            return true;
        }
    }
}
