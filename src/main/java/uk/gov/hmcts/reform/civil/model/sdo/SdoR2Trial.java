package uk.gov.hmcts.reform.civil.model.sdo;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import uk.gov.hmcts.reform.civil.enums.sdo.TrialOnRadioOptions;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class SdoR2Trial {

    private TrialOnRadioOptions trialOnOptions;
    private SdoR2TrialFirstOpenDateAfter sdoR2TrialFirstOpenDateAfter;
    private SdoR2TrialWindow sdoR2TrialWindow;
    private SdoR2TrialHearingLengthOther lengthListOther;
    private DynamicList hearingCourtLocationList;
    private DynamicList altHearingCourtLocationList;
    private String physicalBundlePartyTxt;
    private String hearingNotesTxt;

}
