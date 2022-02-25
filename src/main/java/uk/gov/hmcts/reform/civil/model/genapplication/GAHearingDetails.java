package uk.gov.hmcts.reform.civil.model.genapplication;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.Setter;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.dq.GAHearingDuration;
import uk.gov.hmcts.reform.civil.enums.dq.GAHearingSupportRequirements;
import uk.gov.hmcts.reform.civil.enums.dq.GAHearingType;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.Element;

import java.time.LocalDate;
import java.util.List;

@Setter
@Data
@Builder(toBuilder = true)
public class GAHearingDetails {

    private final YesOrNo hearingYesorNo;
    private final LocalDate hearingDate;
    private final YesOrNo judgeRequiredYesOrNo;
    private final String judgeName;
    private final YesOrNo trialRequiredYesOrNo;
    private final LocalDate trialDateFrom;
    private final LocalDate trialDateTo;
    private final GAHearingType hearingPreferencesPreferredType;
    private final String telephoneHearingPreferredType;
    private final String reasonForPreferredHearingType;
    private final DynamicList hearingPreferredLocation;
    private final String hearingDetailsTelephoneNumber;
    private final String hearingDetailsEmailID;
    private final GAHearingDuration hearingDuration;
    private final String generalAppHearingDays;
    private final String generalAppHearingHours;
    private final String generalAppHearingMinutes;
    private final YesOrNo unavailableTrialRequiredYesOrNo;
    private final List<Element<GAUnavailabilityDates>> generalAppUnavailableDates;
    private final List<GAHearingSupportRequirements> supportRequirement;
    private final String supportRequirementSignLanguage;
    private final String supportRequirementLanguageInterpreter;
    private final String supportRequirementOther;

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
                     @JsonProperty("SupportRequirement") List<GAHearingSupportRequirements> supportRequirement,
                     @JsonProperty("SupportRequirementSignLanguage") String supportRequirementSignLanguage,
                     @JsonProperty("SupportRequirementLanguageInterpreter")
                             String supportRequirementLanguageInterpreter,
                     @JsonProperty("SupportRequirementOther") String supportRequirementOther) {
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
        this.supportRequirement = supportRequirement;
        this.supportRequirementSignLanguage = supportRequirementSignLanguage;
        this.supportRequirementLanguageInterpreter = supportRequirementLanguageInterpreter;
        this.supportRequirementOther = supportRequirementOther;
    }
}
