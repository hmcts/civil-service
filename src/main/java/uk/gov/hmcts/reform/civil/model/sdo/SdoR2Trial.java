package uk.gov.hmcts.reform.civil.model.sdo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.civil.enums.sdo.FastTrackHearingTimeEstimate;
import uk.gov.hmcts.reform.civil.enums.sdo.PhysicalTrialBundleOptions;
import uk.gov.hmcts.reform.civil.enums.sdo.TrialOnRadioOptions;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SdoR2Trial {

    @CCD(label = " ", searchable = false)
    private TrialOnRadioOptions trialOnOptions;
    @CCD(label = " ", showCondition = "trialOnOptions = \"OPEN_DATE\"", searchable = false)
    private SdoR2TrialFirstOpenDateAfter sdoR2TrialFirstOpenDateAfter;
    @CCD(label = " ", showCondition = "trialOnOptions = \"TRIAL_WINDOW\"", searchable = false)
    private SdoR2TrialWindow sdoR2TrialWindow;
    @CCD(label = " ", searchable = false)
    private FastTrackHearingTimeEstimate lengthList;
    @CCD(label = "Length of hearing", showCondition = "lengthList = \"OTHER\"", searchable = false)
    private SdoR2TrialHearingLengthOther lengthListOther;
    @CCD(label = " ", searchable = false, typeOverride = FieldType.DynamicRadioList)
    private DynamicList hearingCourtLocationList;
    @CCD(
            label = " ",
            showCondition = "hearingCourtLocationList=\"OTHER_LOCATION\"",
            searchable = false,
            typeOverride = FieldType.DynamicList
    )
    private DynamicList altHearingCourtLocationList;
    @CCD(
            label = " ",
            hint = "If you want to include any extra information or want to request a certain hearing platform, please include this in the Hearing notes section below",
            searchable = false,
            typeOverride = FieldType.DynamicRadioList
    )
    private DynamicList methodOfHearing;
    @CCD(label = " ", searchable = false)
    private PhysicalTrialBundleOptions physicalBundleOptions;
    @CCD(
            label = " ",
            showCondition = "physicalBundleOptions = \"PARTY\"",
            searchable = false,
            typeOverride = FieldType.TextArea
    )
    private String physicalBundlePartyTxt;
    @CCD(label = "This is only seen by the listing officer", searchable = false, typeOverride = FieldType.TextArea)
    private String hearingNotesTxt;

  // ==== ccd-definition-converter: synthesised definition-only fields (retrofit) ====
  @CCD(label = "### A trial will take place on", searchable = false, typeOverride = FieldType.Label)
  private String sdoR2TrialLabel;
  @CCD(label = "### Length of trial", searchable = false, typeOverride = FieldType.Label)
  private String lengthListLabel;
  @CCD(
          label = "If either party considers that the time estimate is insufficient, they must inform the court within 7 days of the date of this order.",
          searchable = false,
          typeOverride = FieldType.Label
  )
  private String label;
  @CCD(label = "### Hearing location", searchable = false, typeOverride = FieldType.Label)
  private String hearingCourtLocationListLabel;
  @CCD(
          label = "### Alternative hearing location",
          showCondition = "hearingCourtLocationList=\"OTHER_LOCATION\"",
          searchable = false,
          typeOverride = FieldType.Label
  )
  private String altHearingCourtLocationListLabel;
  @CCD(label = "### Method of hearing", searchable = false, typeOverride = FieldType.Label)
  private String methodOfHearingLabel;
  @CCD(label = "### Physical trial bundle", searchable = false, typeOverride = FieldType.Label)
  private String physicalBundleOptionsLabel;
  @CCD(
          label = "### Bundle of documents",
          showCondition = "physicalBundleOptions = \"PARTY\"",
          searchable = false,
          typeOverride = FieldType.Label
  )
  private String physicalBundlePartyTxtLabel;
  @CCD(label = "### Hearing notes", searchable = false, typeOverride = FieldType.Label)
  private String hearingNotesLbl;
  // ==== end synthesised definition-only fields ====
}
