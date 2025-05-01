package uk.gov.hmcts.reform.civil.model.sdo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.civil.enums.sdo.SmallClaimsSdoR2PhysicalTrialBundleOptions;
import uk.gov.hmcts.reform.civil.enums.sdo.SmallClaimsSdoR2TimeEstimate;
import uk.gov.hmcts.reform.civil.enums.sdo.HearingOnRadioOptions;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class SdoR2SmallClaimsHearing {

    private HearingOnRadioOptions trialOnOptions;
    private SdoR2SmallClaimsHearingFirstOpenDateAfter sdoR2SmallClaimsHearingFirstOpenDateAfter;
    private SdoR2SmallClaimsHearingWindow sdoR2SmallClaimsHearingWindow;
    private SmallClaimsSdoR2TimeEstimate lengthList;
    private SdoR2SmallClaimsHearingLengthOther lengthListOther;
    private DynamicList hearingCourtLocationList;
    private DynamicList altHearingCourtLocationList;
    private DynamicList methodOfHearing;
    private SmallClaimsSdoR2PhysicalTrialBundleOptions physicalBundleOptions;
    private SdoR2SmallClaimsBundleOfDocs sdoR2SmallClaimsBundleOfDocs;
    private String hearingNotesTxt;
}
