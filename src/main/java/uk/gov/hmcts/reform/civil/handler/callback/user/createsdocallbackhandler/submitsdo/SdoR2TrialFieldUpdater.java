package uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.submitsdo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2Trial;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

@Slf4j
@Component
@RequiredArgsConstructor
public class SdoR2TrialFieldUpdater implements SdoCaseDataFieldUpdater {

    private final FeatureToggleService featureToggleService;

    @Override
    public void update(CaseData caseData, CaseData.CaseDataBuilder<?, ?> dataBuilder) {
        if (featureToggleService.isSdoR2Enabled() && caseData.getSdoR2Trial() != null) {
            log.debug("Handling SDO R2 Trial for case {}", caseData.getCcdCaseReference());
            SdoR2Trial sdoR2Trial = caseData.getSdoR2Trial();
            if (caseData.getSdoR2Trial().getHearingCourtLocationList() != null) {
                sdoR2Trial.setHearingCourtLocationList(DynamicList.builder().value(
                        caseData.getSdoR2Trial().getHearingCourtLocationList().getValue()).build());
            }
            if (caseData.getSdoR2Trial().getAltHearingCourtLocationList() != null) {
                sdoR2Trial.setAltHearingCourtLocationList(DynamicList.builder().value(
                        caseData.getSdoR2Trial().getAltHearingCourtLocationList().getValue()).build());
            }
            dataBuilder.sdoR2Trial(sdoR2Trial);
        }
    }
}
