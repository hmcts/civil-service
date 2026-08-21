package uk.gov.hmcts.reform.civil.model.genapplication;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.dq.GAHearingDuration;
import uk.gov.hmcts.reform.civil.enums.dq.GAHearingSupportRequirements;
import uk.gov.hmcts.reform.civil.enums.dq.GAHearingType;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.Element;

import java.time.LocalDate;
import java.util.List;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.reform.civil.model.GAHearingSupportRequirementsGAspec;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "GAHearingDetailsGAspec", generate = true)
@Setter
@Data
@NoArgsConstructor
@Accessors(chain = true)
public class GAHearingDetails {

    @CCD(label = "Do you have any other hearing scheduled?", searchable = false, typeOverride = FieldType.YesOrNo)
    private YesOrNo hearingYesorNo;
    @CCD(label = "What is the date of hearing?", showCondition = "hearingYesorNo = \"Yes\"", searchable = false)
    private LocalDate hearingDate;
    @CCD(
            label = "Is your case reserved to a judge?",
            showCondition = "judgeRequiredYesOrNo = \"DEPRECATED_DO_NOT_SHOW\"",
            searchable = false,
            typeOverride = FieldType.YesOrNo
    )
    private YesOrNo judgeRequiredYesOrNo;
    @CCD(label = "Judge name", showCondition = "judgeRequiredYesOrNo = \"Yes\"", searchable = false, max = 40)
    private String judgeName;
    @CCD(label = "Has the trial been fixed?", searchable = false, typeOverride = FieldType.YesOrNo)
    private YesOrNo trialRequiredYesOrNo;
    @CCD(label = "Date from", showCondition = "trialRequiredYesOrNo = \"Yes\"", searchable = false)
    private LocalDate trialDateFrom;
    @CCD(label = "Date to", showCondition = "trialRequiredYesOrNo = \"Yes\"", searchable = false)
    private LocalDate trialDateTo;
    @JsonProperty("HearingPreferencesPreferredType")
    @CCD(ignore = true)
    private GAHearingType hearingPreferencesPreferredType;
    @JsonProperty("TelephoneHearingPreferredType")
    @CCD(ignore = true)
    private String telephoneHearingPreferredType;
    @JsonProperty("ReasonForPreferredHearingType")
    @CCD(ignore = true)
    private String reasonForPreferredHearingType;
    @JsonProperty("HearingPreferredLocation")
    @CCD(ignore = true)
    private DynamicList hearingPreferredLocation;
    @JsonProperty("HearingDetailsTelephoneNumber")
    @CCD(ignore = true)
    private String hearingDetailsTelephoneNumber;
    @JsonProperty("HearingDetailsEmailID")
    @CCD(ignore = true)
    private String hearingDetailsEmailID;
    @JsonProperty("HearingDuration")
    @CCD(ignore = true)
    private GAHearingDuration hearingDuration;
    @CCD(
            label = "Day(s)",
            showCondition = "HearingDuration = \"OTHER\"",
            searchable = false,
            max = 9,
            typeOverride = FieldType.Number
    )
    private String generalAppHearingDays;
    @CCD(
            label = "Hours(s)",
            showCondition = "HearingDuration = \"OTHER\"",
            searchable = false,
            max = 99,
            typeOverride = FieldType.Number
    )
    private String generalAppHearingHours;
    @CCD(
            label = "Minute(s)",
            showCondition = "HearingDuration = \"OTHER\"",
            searchable = false,
            max = 99,
            typeOverride = FieldType.Number
    )
    private String generalAppHearingMinutes;
    @CCD(
            label = "Are there any dates when you cannot attend a hearing within the next 3 months?",
            searchable = false,
            typeOverride = FieldType.YesOrNo
    )
    private YesOrNo unavailableTrialRequiredYesOrNo;
    @CCD(label = "  ", showCondition = "unavailableTrialRequiredYesOrNo = \"Yes\"", searchable = false)
    private List<Element<GAUnavailabilityDates>> generalAppUnavailableDates;
    @CCD(
            label = "Is anyone who will participate in the hearing vulnerable?",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            typeParameterOverride = "GAHearingSupportRequirementsGAspec",
            typeParameterClass = GAHearingSupportRequirementsGAspec.class
    )
    private YesOrNo vulnerabilityQuestionsYesOrNo;
    @CCD(
            label = "Please give details of the vulnerability and the support or adjustments that you want the court to consider.",
            showCondition = "vulnerabilityQuestionsYesOrNo = \"Yes\"",
            searchable = false,
            typeOverride = FieldType.TextArea
    )
    private String vulnerabilityQuestion;
    @JsonProperty("SupportRequirement")
    @CCD(ignore = true)
    private List<GAHearingSupportRequirements> supportRequirement;
    @JsonProperty("SupportRequirementSignLanguage")
    @CCD(ignore = true)
    private String supportRequirementSignLanguage;
    @JsonProperty("SupportRequirementLanguageInterpreter")
    @CCD(ignore = true)
    private String supportRequirementLanguageInterpreter;
    @JsonProperty("SupportRequirementOther")
    @CCD(ignore = true)
    private String supportRequirementOther;
    @CCD(ignore = true)
    private String respondentResponsePartyName;

    @JsonCreator
    GAHearingDetails(@JsonProperty("hearingYesorNo") YesOrNo hearingYesorNo,
                     @JsonProperty("hearingDate") LocalDate hearingDate,
                     @JsonProperty("judgeRequiredYesOrNo") YesOrNo judgeRequiredYesOrNo,
                     @JsonProperty("judgeName") String judgeName,
                     @JsonProperty("trialRequiredYesOrNo") YesOrNo trialRequiredYesOrNo,
                     @JsonProperty("trialDateFrom") LocalDate trialDateFrom,
                     @JsonProperty("trialDateTo") LocalDate trialDateTo,
                     @JsonProperty("HearingPreferencesPreferredType") GAHearingType hearingPreferencesPreferredType,
                     @JsonProperty("TelephoneHearingPreferredType") String telephoneHearingPreferredType,
                     @JsonProperty("ReasonForPreferredHearingType") String reasonForPreferredHearingType,
                     @JsonProperty("HearingPreferredLocation") DynamicList hearingPreferredLocation,
                     @JsonProperty("HearingDetailsTelephoneNumber") String hearingDetailsTelephoneNumber,
                     @JsonProperty("HearingDetailsEmailID") String hearingDetailsEmailID,
                     @JsonProperty("HearingDuration") GAHearingDuration hearingDuration,
                     @JsonProperty("generalAppHearingDays") String generalAppHearingDays,
                     @JsonProperty("generalAppHearingHours") String generalAppHearingHours,
                     @JsonProperty("generalAppHearingMinutes") String generalAppHearingMinutes,
                     @JsonProperty("unavailableTrialRequiredYesOrNo") YesOrNo unavailableTrialRequiredYesOrNo,
                     @JsonProperty("generalAppUnavailableDates") List<Element<GAUnavailabilityDates>>
                             generalAppUnavailableDates,
                     @JsonProperty("vulnerabilityQuestionsYesOrNo") YesOrNo vulnerabilityQuestionsYesOrNo,
                     @JsonProperty("vulnerabilityQuestion") String vulnerabilityQuestion,
                     @JsonProperty("SupportRequirement") List<GAHearingSupportRequirements> supportRequirement,
                     @JsonProperty("SupportRequirementSignLanguage") String supportRequirementSignLanguage,
                     @JsonProperty("SupportRequirementLanguageInterpreter")
                             String supportRequirementLanguageInterpreter,
                     @JsonProperty("SupportRequirementOther") String supportRequirementOther,
                     @JsonProperty("respondentResponsePartyName") String respondentResponsePartyName) {
        this.hearingYesorNo = hearingYesorNo;
        this.hearingDate = hearingDate;
        this.judgeRequiredYesOrNo = judgeRequiredYesOrNo;
        this.judgeName = judgeName;
        this.trialRequiredYesOrNo = trialRequiredYesOrNo;
        this.trialDateFrom = trialDateFrom;
        this.trialDateTo = trialDateTo;
        this.hearingPreferencesPreferredType = hearingPreferencesPreferredType;
        this.telephoneHearingPreferredType = telephoneHearingPreferredType;
        this.reasonForPreferredHearingType = reasonForPreferredHearingType;
        this.hearingPreferredLocation = hearingPreferredLocation;
        this.hearingDetailsTelephoneNumber = hearingDetailsTelephoneNumber;
        this.hearingDetailsEmailID = hearingDetailsEmailID;
        this.hearingDuration = hearingDuration;
        this.generalAppHearingDays = generalAppHearingDays;
        this.generalAppHearingHours = generalAppHearingHours;
        this.generalAppHearingMinutes = generalAppHearingMinutes;
        this.unavailableTrialRequiredYesOrNo = unavailableTrialRequiredYesOrNo;
        this.generalAppUnavailableDates = generalAppUnavailableDates;
        this.vulnerabilityQuestionsYesOrNo = vulnerabilityQuestionsYesOrNo;
        this.vulnerabilityQuestion = vulnerabilityQuestion;
        this.supportRequirement = supportRequirement;
        this.supportRequirementSignLanguage = supportRequirementSignLanguage;
        this.supportRequirementLanguageInterpreter = supportRequirementLanguageInterpreter;
        this.supportRequirementOther = supportRequirementOther;
        this.respondentResponsePartyName = respondentResponsePartyName;
    }

    public GAHearingDetails copy() {
        return new GAHearingDetails()
            .setHearingYesorNo(hearingYesorNo)
            .setHearingDate(hearingDate)
            .setJudgeRequiredYesOrNo(judgeRequiredYesOrNo)
            .setJudgeName(judgeName)
            .setTrialRequiredYesOrNo(trialRequiredYesOrNo)
            .setTrialDateFrom(trialDateFrom)
            .setTrialDateTo(trialDateTo)
            .setHearingPreferencesPreferredType(hearingPreferencesPreferredType)
            .setTelephoneHearingPreferredType(telephoneHearingPreferredType)
            .setReasonForPreferredHearingType(reasonForPreferredHearingType)
            .setHearingPreferredLocation(hearingPreferredLocation)
            .setHearingDetailsTelephoneNumber(hearingDetailsTelephoneNumber)
            .setHearingDetailsEmailID(hearingDetailsEmailID)
            .setHearingDuration(hearingDuration)
            .setGeneralAppHearingDays(generalAppHearingDays)
            .setGeneralAppHearingHours(generalAppHearingHours)
            .setGeneralAppHearingMinutes(generalAppHearingMinutes)
            .setUnavailableTrialRequiredYesOrNo(unavailableTrialRequiredYesOrNo)
            .setGeneralAppUnavailableDates(generalAppUnavailableDates)
            .setVulnerabilityQuestionsYesOrNo(vulnerabilityQuestionsYesOrNo)
            .setVulnerabilityQuestion(vulnerabilityQuestion)
            .setSupportRequirement(supportRequirement)
            .setSupportRequirementSignLanguage(supportRequirementSignLanguage)
            .setSupportRequirementLanguageInterpreter(supportRequirementLanguageInterpreter)
            .setSupportRequirementOther(supportRequirementOther)
            .setRespondentResponsePartyName(respondentResponsePartyName);
    }

  // ==== ccd-definition-converter: synthesised definition-only fields (retrofit) ====
  @CCD(
          label = "<span class=\"form-label ng-star-inserted\">Enter the trial date or trial window</span>",
          showCondition = "trialRequiredYesOrNo = \"Yes\"",
          searchable = false,
          typeOverride = FieldType.Label
  )
  private String trialRequiredLabel;
  @CCD(label = "**Vulnerability questions**", searchable = false, typeOverride = FieldType.Label)
  private String vulnerabilityQuestionsLabel;
  @JsonProperty("HearingSupportRequirementsLabel")
  @CCD(label = "**Support requirements**", searchable = false, typeOverride = FieldType.Label)
  private String hearingSupportRequirementsLabel;
  // ==== end synthesised definition-only fields ====
}
