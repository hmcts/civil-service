package uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.submitsdo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;

@Slf4j
@Component
@RequiredArgsConstructor
public class CourtLocationFieldUpdater implements SdoCaseDataFieldUpdater {

    private final FeatureToggleService featureToggleService;

    @Override
    public void update(CaseData caseData, CaseData.CaseDataBuilder<?, ?> dataBuilder) {
        boolean isLipCase = caseData.isApplicantLiP() || caseData.isRespondent1LiP() || caseData.isRespondent2LiP();
        boolean isLocationWhiteListed = featureToggleService.isLocationWhiteListedForCaseProgression(caseData.getCaseManagementLocation().getBaseLocation());

        if (!isLipCase) {
            log.info("Case {} is whitelisted for case progression.", caseData.getCcdCaseReference());
            dataBuilder.eaCourtLocation(YES);
            dataBuilder.hmcEaCourtLocation(!isLipCase && isLocationWhiteListed ? YES : NO);
        } else if (isLipCaseWithProgressionEnabledAndCourtWhiteListed(caseData)) {
            dataBuilder.eaCourtLocation(YesOrNo.YES);
        } else {
            log.info("Case {} is NOT whitelisted for case progression.", caseData.getCcdCaseReference());
            dataBuilder.eaCourtLocation(NO);
        }
    }

    private boolean isLipCaseWithProgressionEnabledAndCourtWhiteListed(CaseData caseData) {
        return (caseData.isLipvLipOneVOne() || caseData.isLRvLipOneVOne())
                && featureToggleService.isCaseProgressionEnabledAndLocationWhiteListed(caseData.getCaseManagementLocation().getBaseLocation());
    }
}
