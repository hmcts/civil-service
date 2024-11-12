package uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.orderdetailspages;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackAllocation;

@Slf4j
@Component
public class ResetFieldsForReconsiderationFieldBuilder implements OrderDetailsPagesCaseFieldBuilder {

    @Override
    public void build(CaseData.CaseDataBuilder<?, ?> updatedData) {
        log.debug("Resetting fields related to reconsideration");
        updatedData.drawDirectionsOrderRequired(null);
        updatedData.drawDirectionsOrderSmallClaims(null);
        updatedData.fastClaims(null);
        updatedData.smallClaims(null);
        updatedData.claimsTrack(null);
        updatedData.orderType(null);
        updatedData.trialAdditionalDirectionsForFastTrack(null);
        updatedData.drawDirectionsOrderSmallClaimsAdditionalDirections(null);
        updatedData.fastTrackAllocation(FastTrackAllocation.builder().assignComplexityBand(null).build());
        updatedData.disposalHearingAddNewDirections(null);
        updatedData.smallClaimsAddNewDirections(null);
        updatedData.fastTrackAddNewDirections(null);
        updatedData.sdoHearingNotes(null);
        updatedData.fastTrackHearingNotes(null);
        updatedData.disposalHearingHearingNotes(null);
        updatedData.sdoR2SmallClaimsHearing(null);
        updatedData.sdoR2SmallClaimsUploadDoc(null);
        updatedData.sdoR2SmallClaimsPPI(null);
        updatedData.sdoR2SmallClaimsImpNotes(null);
        updatedData.sdoR2SmallClaimsWitnessStatements(null);
        updatedData.sdoR2SmallClaimsHearingToggle(null);
        updatedData.sdoR2SmallClaimsJudgesRecital(null);
        updatedData.sdoR2SmallClaimsWitnessStatementsToggle(null);
        updatedData.sdoR2SmallClaimsPPIToggle(null);
        updatedData.sdoR2SmallClaimsUploadDocToggle(null);
    }
}
