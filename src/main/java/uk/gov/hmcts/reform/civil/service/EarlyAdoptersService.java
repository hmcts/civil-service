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

    public boolean isPartOfHmcEarlyAdoptersRollout(CaseData caseData, String hearingLocationEpimms) {
        boolean isWhiteListedForHmc = featureToggleService.isLocationWhiteListedForCaseProgression(
            caseData.getCaseManagementLocation().getBaseLocation())
            && featureToggleService.isLocationWhiteListedForCaseProgression(hearingLocationEpimms);
        log.info(("Case {} is{}whitelisted for HMC rollout."),
                 caseData.getCcdCaseReference(), isWhiteListedForHmc ? " " : " NOT ");
        return isWhiteListedForHmc;
    }

}
