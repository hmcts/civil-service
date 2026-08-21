package uk.gov.hmcts.reform.civil.model.sdo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.civil.enums.sdo.HearingOnRadioOptions;
import uk.gov.hmcts.reform.civil.enums.sdo.SmallClaimsSdoR2PhysicalTrialBundleOptions;
import uk.gov.hmcts.reform.civil.enums.sdo.SmallClaimsSdoR2TimeEstimate;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SdoR2SmallClaimsHearing {

    @CCD(label = " ", searchable = false)
    private HearingOnRadioOptions trialOnOptions;
    @CCD(label = " ", showCondition = "trialOnOptions = \"OPEN_DATE\"", searchable = false)
    private SdoR2SmallClaimsHearingFirstOpenDateAfter sdoR2SmallClaimsHearingFirstOpenDateAfter;
    @CCD(label = " ", showCondition = "trialOnOptions = \"HEARING_WINDOW\"", searchable = false)
    private SdoR2SmallClaimsHearingWindow sdoR2SmallClaimsHearingWindow;
    @CCD(label = " ", searchable = false)
    private SmallClaimsSdoR2TimeEstimate lengthList;
    @CCD(label = " ", showCondition = "lengthList = \"OTHER\"", searchable = false)
    private SdoR2SmallClaimsHearingLengthOther lengthListOther;
    @CCD(label = " ", searchable = false, typeOverride = FieldType.DynamicRadioList)
    private DynamicList hearingCourtLocationList;
    @CCD(
            label = "Alternative hearing location",
            showCondition = "hearingCourtLocationList=\"OTHER_LOCATION\"",
            searchable = false,
            typeOverride = FieldType.DynamicList
    )
    private DynamicList altHearingCourtLocationList;
    @CCD(
            label = " ",
            hint = "If you want to include any extra information or want to request a certain hearing platform, please include this in the 'Hearing notes' section below",
            searchable = false,
            typeOverride = FieldType.DynamicRadioList
    )
    private DynamicList methodOfHearing;
    @CCD(label = " ", searchable = false)
    private SmallClaimsSdoR2PhysicalTrialBundleOptions physicalBundleOptions;
    @CCD(label = " ", showCondition = "physicalBundleOptions = \"PARTY\"", searchable = false)
    private SdoR2SmallClaimsBundleOfDocs sdoR2SmallClaimsBundleOfDocs;
    @CCD(label = "This is only seen by the Listing Officer", searchable = false, typeOverride = FieldType.TextArea)
    private String hearingNotesTxt;

  // ==== ccd-definition-converter: synthesised definition-only fields (retrofit) ====
  @CCD(label = "**A hearing will take place on**", searchable = false, typeOverride = FieldType.Label)
  private String label;
  @CCD(label = "**Length of hearing**", searchable = false, typeOverride = FieldType.Label)
  private String lengthHearingLbl;
  @CCD(
          label = "If either party considers that the time estimate is insufficient, they must inform the court within 7 days of the date of this order.",
          searchable = false,
          typeOverride = FieldType.Label
  )
  private String eitherPartyLabel;
  @CCD(label = "**Hearing location**", searchable = false, typeOverride = FieldType.Label)
  private String hearingLocationLbl;
  @CCD(label = "**Method of hearing**", searchable = false, typeOverride = FieldType.Label)
  private String methodOfHearingLbl;
  @CCD(label = "**Physical hearing bundle**", searchable = false, typeOverride = FieldType.Label)
  private String physicalBundleOptionsLbl;
  @CCD(label = "**Hearing notes**", searchable = false, typeOverride = FieldType.Label)
  private String hearingNotesLbl;
  // ==== end synthesised definition-only fields ====
}
