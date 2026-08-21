package uk.gov.hmcts.reform.civil.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.Element;

import java.util.List;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "HearingSupportRequirementsListDJ", generate = true)
@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class HearingSupportRequirementsDJ {

    @CCD(
            label = "Choose your preferred type of hearing ",
            hint = "A judge will still need to approve the hearing",
            searchable = false,
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "hearingType",
            typeParameterClass = HearingType.class
    )
    private String hearingType;
    @CCD(
            label = "Who will set up the telephone hearing ?",
            showCondition = "hearingType = \"TELEPHONE_HEARING\"",
            searchable = false
    )
    private String hearingTypeTelephoneHearing;
    @CCD(
            label = "Preferred location",
            hint = "This is needed if the judge decides to hold the hearing in person",
            searchable = false,
            typeOverride = FieldType.DynamicList
    )
    private DynamicList hearingTemporaryLocation;
    @CCD(label = "Preferred telephone number", searchable = false, typeOverride = FieldType.PhoneUK)
    private String hearingPreferredTelephoneNumber1;
    @CCD(label = "Preferred email", searchable = false, typeOverride = FieldType.Email)
    private String hearingPreferredEmail;
    @CCD(
            label = "How long do you estimate the hearing will take?",
            searchable = false,
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "HearingLengthEstimate",
            typeParameterClass = HearingLengthEstimate.class
    )
    private String hearingLengthEstimate;
    @CCD(
            label = "Hours",
            showCondition = "hearingLengthEstimate = \"OTHER\"",
            searchable = false,
            typeOverride = FieldType.Number
    )
    private String hoursEstimateOther;
    @CCD(
            label = "Are there any dates when you cannot attend a hearing within the next 3 months?",
            searchable = false,
            typeOverride = FieldType.YesOrNo
    )
    private YesOrNo hearingUnavailableDates;
    @CCD(ignore = true)
    private String[] hearingSupportRequirementsDisabledAccess;
    @CCD(ignore = true)
    private String[] hearingSupportRequirementHearingLoop;
    @CCD(ignore = true)
    private String[] hearingSupportRequirementInterpreter;
    @CCD(ignore = true)
    private String hearingSupportRequirementSignLanguageDropdown;
    @CCD(ignore = true)
    private String[] hearingSupportRequirementLanguage;
    @CCD(ignore = true)
    private String hearingSupportRequirementLanguageDropdown;
    @CCD(ignore = true)
    private String[] hearingSupportRequirementOther;
    @CCD(ignore = true)
    private String hearingSupportRequirementOtherDropdown;
    @CCD(
            label = " ",
            showCondition = "hearingUnavailableDates = \"Yes\"",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "hearingDatesParam"
    )
    private List<Element<HearingDates>> hearingDates;
    @CCD(
            label = "Does anyone require support for a court hearing?",
            searchable = false,
            typeOverride = FieldType.YesOrNo
    )
    private YesOrNo hearingSupportQuestion;
    @CCD(
            label = "Please name all people who need support and the kind of support they will need. For example, Jane Smith: requires wheelchair access.",
            showCondition = "hearingSupportQuestion=\"Yes\"",
            searchable = false,
            typeOverride = FieldType.TextArea
    )
    private String hearingSupportAdditional;

  // ==== ccd-definition-converter: synthesised definition-only fields (retrofit) ====
  @CCD(label = "Preferred Location", searchable = false)
  private String hearingPreferredLocation;
  // ==== end synthesised definition-only fields ====
}
