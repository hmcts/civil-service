package uk.gov.hmcts.reform.civil.service.flowstate.scenario;

import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.flowstate.repository.AllowedEventRepository;

import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;

public final class SpecScenario extends AllowedEventsScenario {

    private static final String SCENARIO_FILE = "allowed-spec-events.yml";

    public SpecScenario(AllowedEventRepository repo) {
        super(repo);
    }

    @Override
    protected String scenarioFile() {
        return SCENARIO_FILE;
    }

    @Override
    public boolean appliesTo(CaseData caseData) {
        return SPEC_CLAIM.equals(caseData.getCaseAccessCategory());
    }

}
