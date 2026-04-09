package uk.gov.hmcts.reform.civil.service.flowstate.scenario;

import uk.gov.hmcts.reform.civil.service.flowstate.repository.AllowedEventRepository;

public final class SpecScenario extends AllowedEventScenario {

    private static final String SCENARIO_FILE = "allowed-spec-events.yml";

    public SpecScenario(AllowedEventRepository repo) {
        super(repo);
    }

    @Override
    protected String scenarioFile() {
        return SCENARIO_FILE;
    }

    @Override
    public boolean appliesTo(boolean specOrLip) {
        return specOrLip;
    }

}
