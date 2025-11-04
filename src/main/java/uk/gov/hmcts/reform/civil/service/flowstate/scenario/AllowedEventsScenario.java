package uk.gov.hmcts.reform.civil.service.flowstate.scenario;

import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.config.FlowStateAllowedEventsConfig;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.util.HashSet;
import java.util.Set;

public abstract class AllowedEventsScenario {

    protected final FlowStateAllowedEventsConfig config;

    protected AllowedEventsScenario(FlowStateAllowedEventsConfig config) {
        this.config = config;
    }

    // Determines whether this scenario applies to the given case shape
    public abstract boolean appliesTo(CaseData caseData);

    // Loads the base allowed events for this scenario at the given state
    public Set<CaseEvent> loadBaseEvents(CaseData caseData, String stateFullName) {
        // Default: reuse existing unspec map; spec scenarios should override
        return new HashSet<>(config.getAllowedEvents(stateFullName));
    }

    // Allows scenario to add/remove events using reusable predicates (filters)
    public Set<CaseEvent> applyFilters(CaseData caseData, String stateFullName, Set<CaseEvent> base) {
        // Default: no-op (behaviour preserving)
        return base;
    }
}
