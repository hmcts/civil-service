package uk.gov.hmcts.reform.civil.service.flowstate.scenario;

import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.config.FlowStateAllowedEventsConfig;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.util.HashSet;
import java.util.Set;

import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_ONE;

public final class OneVOneSpecScenario extends AllowedEventsScenario {

    public OneVOneSpecScenario(FlowStateAllowedEventsConfig config) {
        super(config);
    }

    @Override
    public boolean appliesTo(CaseData cd) {
        // Keep this simple for now; refine with proper party-shape checks as you migrate
        return ONE_V_ONE.equals(MultiPartyScenario.getMultiPartyScenario(cd))
            && SPEC_CLAIM.equals(cd.getCaseAccessCategory());
    }

    @Override
    public Set<CaseEvent> loadBaseEvents(CaseData cd, String stateFullName) {
        // SPEC claims use the spec map that FlowStateAllowedEventService currently uses
        // TODO: update config to use scenarios
        return new HashSet<>(config.getAllowedEventsSpec(stateFullName));
    }

    @Override
    public Set<CaseEvent> applyFilters(CaseData cd, String stateFullName, Set<CaseEvent> base) {
        // Initially a no-op to preserve behaviour, wire filter predicates here
        return base;
    }
}
