package uk.gov.hmcts.reform.civil.service.flowstate.scenario;

import uk.gov.hmcts.reform.civil.service.flowstate.repository.AllowedEventRepository;

public final class UnspecScenario extends AllowedEventScenario {

    private static final String SCENARIO_FILE = "allowed-unspec-events.yml";

    public UnspecScenario(AllowedEventRepository repo) {
        super(repo);
    }

    @Override
    protected String scenarioFile() {
        return SCENARIO_FILE;
    }

    @Override
    public boolean appliesTo(boolean specOrLip) {
        return !specOrLip;
    }

}
