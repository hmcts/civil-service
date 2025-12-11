package uk.gov.hmcts.reform.civil.service.dashboardnotifications.caseproceedsoffline;

import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import java.util.Map;

/**
 * Base scenario helper for claimant/defendant case proceed offline flows.
 */
public abstract class CaseProceedOfflinePartyScenarioService extends CaseProceedOfflineScenarioService {

    protected CaseProceedOfflinePartyScenarioService(FeatureToggleService featureToggleService) {
        super(featureToggleService);
    }

    public Map<String, Boolean> resolveAdditionalScenarios(CaseData caseData) {
        return additionalScenarios(
            caseData,
            inactiveScenarioId(),
            availableScenarioId(),
            queryScenarioId()
        );
    }

    protected abstract String inactiveScenarioId();

    protected abstract String availableScenarioId();

    protected abstract String queryScenarioId();
}
