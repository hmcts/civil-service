package uk.gov.hmcts.reform.civil.service.flowstate;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.flowstate.repository.AllowedEventRepository;
import uk.gov.hmcts.reform.civil.service.flowstate.scenario.AllowedEventScenario;
import uk.gov.hmcts.reform.civil.stateflow.StateFlow;

import java.util.List;

import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_CLAIM_SPEC;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_LIP_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;

@Slf4j
@RequiredArgsConstructor
public class AllowedEventService {

    private final AllowedEventRepository repo;
    private final IStateFlowEngine stateFlowEngine;
    private final List<AllowedEventScenario> scenarios;

    public boolean isAllowed(CaseData caseData, CaseEvent event) {

        if (repo.getWhitelist().contains(event)) {
            return true;
        }

        // Find scenario (or default) chain first match wins
        // AllowedEventsConfig.java for strict precedence
        AllowedEventScenario scenario = scenarios.stream()
            .filter(s -> s.appliesTo(caseData))
            .findFirst()
            .orElse(null);

        if (scenario == null) {
            log.warn("Scenario not found ({}:{})",
                     MultiPartyScenario.getMultiPartyScenario(caseData),
                     caseData.getCaseAccessCategory());
            return false;
        }

        StateFlow stateFlow = selectStateFlow(caseData, event);
        String stateFullName = stateFlow.getState().getName();

        return scenario.loadBaseEvents(stateFullName).contains(event);
    }

    private StateFlow selectStateFlow(CaseData caseData, CaseEvent event) {
        boolean isSpecOrLip = SPEC_CLAIM.equals(caseData.getCaseAccessCategory())
                || CREATE_CLAIM_SPEC.equals(event)
                || CREATE_LIP_CLAIM.equals(event);
        return isSpecOrLip ? stateFlowEngine.evaluateSpec(caseData) : stateFlowEngine.evaluate(caseData);
    }

}
