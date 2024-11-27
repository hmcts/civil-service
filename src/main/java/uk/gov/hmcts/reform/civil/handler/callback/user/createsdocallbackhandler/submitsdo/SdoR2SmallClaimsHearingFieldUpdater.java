package uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.submitsdo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.helpers.sdo.SdoHelper;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2SmallClaimsHearing;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

@Slf4j
@Component
@RequiredArgsConstructor
public class SdoR2SmallClaimsHearingFieldUpdater implements SdoCaseDataFieldUpdater {

    private final FeatureToggleService featureToggleService;
    private final SubmitSdoUtils submitSdoUtils;

    @Override
    public void update(CaseData caseData, CaseData.CaseDataBuilder<?, ?> dataBuilder) {
        if (featureToggleService.isSdoR2Enabled() && SdoHelper.isSDOR2ScreenForDRHSmallClaim(caseData)
                && caseData.getSdoR2SmallClaimsHearing() != null) {
            log.debug("Handling SDO R2 Small Claims Hearing for case {}", caseData.getCcdCaseReference());
            dataBuilder.sdoR2SmallClaimsHearing(updateHearingAfterDeletingLocationList(caseData.getSdoR2SmallClaimsHearing()));
        }
    }

    private SdoR2SmallClaimsHearing updateHearingAfterDeletingLocationList(SdoR2SmallClaimsHearing sdoR2SmallClaimsHearing) {
        if (sdoR2SmallClaimsHearing.getHearingCourtLocationList() != null) {
            sdoR2SmallClaimsHearing.setHearingCourtLocationList(submitSdoUtils.deleteLocationList(sdoR2SmallClaimsHearing.getHearingCourtLocationList()));
        }
        if (sdoR2SmallClaimsHearing.getAltHearingCourtLocationList() != null) {
            sdoR2SmallClaimsHearing.setAltHearingCourtLocationList(submitSdoUtils.deleteLocationList(sdoR2SmallClaimsHearing.getAltHearingCourtLocationList()));
        }
        return sdoR2SmallClaimsHearing;
    }
}
