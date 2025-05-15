package uk.gov.hmcts.reform.civil.notification.handlers.mediation;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

@Component
public class MediationHelper {

    private final FeatureToggleService featureToggleService;

    public MediationHelper(FeatureToggleService featureToggleService) {
        this.featureToggleService = featureToggleService;
    }

    public boolean isCarmEnabled(CaseData caseData) {
        return featureToggleService.isCarmEnabledForCase(caseData);
    }
}
