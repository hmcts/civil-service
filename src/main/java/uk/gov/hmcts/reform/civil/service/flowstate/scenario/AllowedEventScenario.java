package uk.gov.hmcts.reform.civil.service.flowstate.scenario;

import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.service.flowstate.repository.AllowedEventRepository;

import java.util.HashSet;
import java.util.Set;

public abstract class AllowedEventScenario {

    protected final AllowedEventRepository repo;

    protected AllowedEventScenario(AllowedEventRepository repo) {
        this.repo = repo;
    }

    protected abstract String scenarioFile();

    public abstract boolean appliesTo(boolean specOrLip);

    public Set<CaseEvent> loadBaseEvents(String state) {
        // Default: reuse existing unspec map; spec scenarios should override
        return new HashSet<>(repo.getAllowedEvents(scenarioFile(), state));
    }
}
