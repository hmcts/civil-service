package uk.gov.hmcts.reform.civil.model.finalorders;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import uk.gov.hmcts.reform.civil.enums.finalorders.HearingLengthFinalOrderList;
import uk.gov.hmcts.reform.civil.enums.hearing.HearingChannel;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;

import java.time.LocalDate;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Accessors(chain = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FinalOrderFurtherHearing {

    @CCD(label = " ", hint = "For example, 16 4 2021", searchable = false)
    private LocalDate listFromDate;
    @CCD(label = " ", hint = "For example, 16 4 2021", searchable = false)
    private LocalDate dateToDate;
    @CCD(label = " ", searchable = false)
    private HearingLengthFinalOrderList lengthList;
    @CCD(label = " ", showCondition = "lengthList=\"OTHER\"", searchable = false)
    private CaseHearingLengthElement lengthListOther;
    @CCD(
            label = " ",
            showCondition = "hearingLocationList=\"OTHER_LOCATION\"",
            searchable = false,
            typeOverride = FieldType.DynamicList
    )
    private DynamicList alternativeHearingList;
    @CCD(
            label = " ",
            hint = "If you want to include any extra information or want to request a certain hearing platform, please include this in the Hearing notes (Optional) section below",
            searchable = false
    )
    private HearingChannel hearingMethodList;
    @CCD(
            label = " ",
            hint = "For example, potentially violent, reading time needed, CVP for hearing",
            searchable = false,
            typeOverride = FieldType.TextArea
    )
    private String hearingNotesText;
    @CCD(label = " ", searchable = false, typeOverride = FieldType.DynamicRadioList)
    private DynamicList hearingLocationList;
    @CCD(label = " ", showCondition = "datesToAvoidYesNo=\"Yes\"", searchable = false)
    private DatesFinalOrders datesToAvoidDateDropdown;

  // ==== ccd-definition-converter: synthesised definition-only fields (retrofit) ====
  @CCD(label = "### Date of new hearing", searchable = false, typeOverride = FieldType.Label)
  private String label;
  @CCD(label = "### List from", searchable = false, typeOverride = FieldType.Label)
  private String listFromLabel;
  @CCD(label = "### Date to (Optional)", searchable = false, typeOverride = FieldType.Label)
  private String dateToLabel;
  @CCD(label = "### Length of new hearing", searchable = false, typeOverride = FieldType.Label)
  private String lengthLabel;
  @CCD(
          label = "### Do you want the parties to provide dates to avoid?",
          searchable = false,
          typeOverride = FieldType.Label
  )
  private String datesToAvoidLabel;
  @CCD(label = " ", searchable = false)
  private uk.gov.hmcts.ccd.sdk.type.YesOrNo datesToAvoidYesNo;
  @CCD(label = "### Hearing location", searchable = false, typeOverride = FieldType.Label)
  private String alternativeHearingLabel;
  @CCD(
          label = "### Alternative hearing location",
          showCondition = "hearingLocationList=\"OTHER_LOCATION\"",
          searchable = false,
          typeOverride = FieldType.Label
  )
  private String alternativeHearingDropdownLabel;
  @CCD(label = "### Method of hearing", searchable = false, typeOverride = FieldType.Label)
  private String hearingMethodLabel;
  @CCD(label = "### Hearing notes (Optional)", searchable = false, typeOverride = FieldType.Label)
  private String hearingNotesLabel;
  // ==== end synthesised definition-only fields ====
}
