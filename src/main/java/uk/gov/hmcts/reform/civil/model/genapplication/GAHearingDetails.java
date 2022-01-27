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

import java.time.LocalDate;
import java.util.List;

@Setter
@Data
@Builder(toBuilder = true)
public class GAHearingDetails {

    private final String judgeName;
    private final LocalDate hearingDate;
    private final LocalDate trialDateFrom;
    private final LocalDate trialDateTo;
    private final YesOrNo hearingYesorNo;
    private final GAHearingDuration hearingDuration;
    private final List<GAHearingSupportRequirements> supportRequirement;
    private final YesOrNo judgeRequiredYesOrNo;
    private final YesOrNo trialRequiredYesOrNo;
    private final String hearingDetailsEmailID;
    private final LocalDate unavailableTrailDateTo;
    private final String supportRequirementOther;
    private final DynamicList hearingPreferredLocation;
    private final LocalDate unavailableTrailDateFrom;
    private final String hearingDetailsTelephoneNumber;
    private final String reasonForPreferredHearingType;
    private final String telephoneHearingPreferredType;
    private final String supportRequirementSignLanguage;
    private final GAHearingType hearingPreferencesPreferredType;
    private final YesOrNo unavailableTrailRequiredYesOrNo;
    private final String supportReqLanguageInterpreter;

    @JsonCreator
    GAHearingDetails(@JsonProperty("judgeName") String judgeName,
                     @JsonProperty("hearingDate") LocalDate hearingDate,
                     @JsonProperty("trialDateFrom") LocalDate trialDateFrom,
                     @JsonProperty("trialDateTo") LocalDate trialDateTo,
                     @JsonProperty("hearingYesorNo") YesOrNo hearingYesorNo,
                     @JsonProperty("HearingDuration") GAHearingDuration hearingDuration,
                     @JsonProperty("SupportRequirement") List<GAHearingSupportRequirements> supportRequirement,
                     @JsonProperty("judgeRequiredYesOrNo") YesOrNo judgeRequiredYesOrNo,
                     @JsonProperty("trialRequiredYesOrNo") YesOrNo trialRequiredYesOrNo,
                     @JsonProperty("HearingDetailsEmailID") String hearingDetailsEmailID,
                     @JsonProperty("UnavailableTrailDateTo") LocalDate unavailableTrailDateTo,
                     @JsonProperty("SupportRequirementOther") String supportRequirementOther,
                     @JsonProperty("HearingPreferredLocation") DynamicList hearingPreferredLocation,
                     @JsonProperty("UnavailableTrailDateFrom") LocalDate unavailableTrailDateFrom,
                     @JsonProperty("HearingDetailsTelephoneNumber") String hearingDetailsTelephoneNumber,
                     @JsonProperty("ReasonForPreferredHearingType") String reasonForPreferredHearingType,
                     @JsonProperty("TelephoneHearingPreferredType") String telephoneHearingPreferredType,
                     @JsonProperty("SupportRequirementSignLanguage") String supportRequirementSignLanguage,
                     @JsonProperty("HearingPreferencesPreferredType") GAHearingType hearingPreferencesPreferredType,
                     @JsonProperty("UnavailableTrailRequiredYesOrNo") YesOrNo unavailableTrailRequiredYesOrNo,
                     @JsonProperty("SupportReqLanguageInterpreter")
                         String supportReqLanguageInterpreter) {
        this.judgeName = judgeName;
        this.hearingDate = hearingDate;
        this.trialDateFrom = trialDateFrom;
        this.trialDateTo = trialDateTo;
        this.hearingYesorNo = hearingYesorNo;
        this.hearingDuration = hearingDuration;
        this.supportRequirement = supportRequirement;
        this.judgeRequiredYesOrNo = judgeRequiredYesOrNo;
        this.trialRequiredYesOrNo = trialRequiredYesOrNo;
        this.hearingDetailsEmailID = hearingDetailsEmailID;
        this.unavailableTrailDateTo = unavailableTrailDateTo;
        this.supportRequirementOther = supportRequirementOther;
        this.hearingPreferredLocation = hearingPreferredLocation;
        this.unavailableTrailDateFrom = unavailableTrailDateFrom;
        this.hearingDetailsTelephoneNumber = hearingDetailsTelephoneNumber;
        this.reasonForPreferredHearingType = reasonForPreferredHearingType;
        this.telephoneHearingPreferredType = telephoneHearingPreferredType;
        this.supportRequirementSignLanguage = supportRequirementSignLanguage;
        this.hearingPreferencesPreferredType = hearingPreferencesPreferredType;
        this.unavailableTrailRequiredYesOrNo = unavailableTrailRequiredYesOrNo;
        this.supportReqLanguageInterpreter = supportReqLanguageInterpreter;
    }
}
