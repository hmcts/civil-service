package uk.gov.hmcts.reform.civil.service.sdo;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.enums.AllocatedTrack;
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

    public boolean isDefendantNoCOnlineForCase(CaseData caseData) {
        return featureToggleService.isDefendantNoCOnlineForCase(caseData);
    }

    public boolean isCaseProgressionEnabledAndLocationWhiteListed(String baseLocation) {
        return featureToggleService.isCaseProgressionEnabledAndLocationWhiteListed(baseLocation);
    }

    public boolean isWelshEnabledForMainCase() {
        return featureToggleService.isWelshEnabledForMainCase();
    }

    public boolean isMultiOrIntermediateTrackCase(CaseData caseData) {
        if (!isMultiOrIntermediateTrackEnabled(caseData)) {
            return false;
        }

        AllocatedTrack allocatedTrack = caseData.getAllocatedTrack();
        String responseClaimTrack = caseData.getResponseClaimTrack();

        boolean isIntermediateTrack = AllocatedTrack.INTERMEDIATE_CLAIM.equals(allocatedTrack)
            || AllocatedTrack.INTERMEDIATE_CLAIM.name().equals(responseClaimTrack);
        boolean isMultiTrack = AllocatedTrack.MULTI_CLAIM.equals(allocatedTrack)
            || AllocatedTrack.MULTI_CLAIM.name().equals(responseClaimTrack);

        return isIntermediateTrack || isMultiTrack;
    }
}
