package uk.gov.hmcts.reform.civil.service.sdo;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

@Service
@RequiredArgsConstructor
public class SdoFeatureToggleService {

    private final FeatureToggleService featureToggleService;

    public boolean isWelshJourneyEnabled(CaseData caseData) {
        return featureToggleService.isWelshEnabledForMainCase()
            && (caseData.isClaimantBilingual() || caseData.isRespondentResponseBilingual());
    }

    public boolean isCarmEnabled(CaseData caseData) {
        return featureToggleService.isCarmEnabledForCase(caseData);
    }

    public boolean isMultiOrIntermediateTrackEnabled(CaseData caseData) {
        return featureToggleService.isMultiOrIntermediateTrackEnabled(caseData);
    }

    public boolean isCaseProgressionEnabled() {
        return featureToggleService.isCaseProgressionEnabled();
    }

    public boolean isDefendantNoCOnlineForCase(CaseData caseData) {
        return featureToggleService.isDefendantNoCOnlineForCase(caseData);
    }

    public boolean isCaseProgressionEnabledAndLocationWhiteListed(String baseLocation) {
        return featureToggleService.isCaseProgressionEnabledAndLocationWhiteListed(baseLocation);
    }

    public boolean isWelshEnabledForMainCase() {
        return featureToggleService.isWelshEnabledForMainCase();
    }
}
