package uk.gov.hmcts.reform.civil.service.flowstate.scenario;

import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.flowstate.repository.AllowedEventRepository;

import static uk.gov.hmcts.reform.civil.enums.CaseCategory.UNSPEC_CLAIM;

public final class UnspecScenario extends AllowedEventsScenario {

    private static final String SCENARIO_FILE = "allowed-unspec-events.yml";

    public UnspecScenario(AllowedEventRepository repo) {
        super(repo);
    }

    @Override
    protected String scenarioFile() {
        return SCENARIO_FILE;
    }

    @Override
    public boolean appliesTo(CaseData caseData) {
        return UNSPEC_CLAIM.equals(caseData.getCaseAccessCategory());
    }

}
