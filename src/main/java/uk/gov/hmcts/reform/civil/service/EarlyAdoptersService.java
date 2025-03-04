package uk.gov.hmcts.reform.civil.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.model.CaseData;

@Slf4j
@Service
@RequiredArgsConstructor
public class EarlyAdoptersService {

    private final FeatureToggleService featureToggleService;

    public boolean isPartOfHmcLipEarlyAdoptersRollout(CaseData caseData) {
        boolean isWhiteListed = featureToggleService.isLocationWhiteListedForCaseProgression(
            caseData.getCaseManagementLocation().getBaseLocation());
        logWhiteListingOutcome(caseData.getCcdCaseReference(), "HMC", isWhiteListed);
        return isWhiteListed;
    }

    private void logWhiteListingOutcome(Long caseId, String featureName, boolean whitelisted) {
        log.info(("Case {} is{}whitelisted for {} rollout."), caseId, whitelisted ? " " : " NOT ", featureName);
    }

}
