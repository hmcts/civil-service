package uk.gov.hmcts.reform.civil.ga.model.genapplication;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import uk.gov.hmcts.reform.civil.ga.enums.GAJudicialHearingType;
import uk.gov.hmcts.reform.civil.enums.dq.GAHearingDuration;
import uk.gov.hmcts.reform.civil.enums.dq.SupportRequirements;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;

import java.util.List;

@Setter
@Data
@NoArgsConstructor
@Accessors(chain = true)
public class GAJudgesHearingListGAspec {

    private GAJudicialHearingType hearingPreferencesPreferredType;
    private DynamicList hearingPreferredLocation;
    private GAHearingDuration judicialTimeEstimate;
    private String judicialTimeEstimateDays;
    private String judicialTimeEstimateHours;
    private String judicialTimeEstimateMinutes;
    private List<SupportRequirements> judicialSupportRequirement;
    private String judgeSignLanguage;
    private String judgeLanguageInterpreter;
    private String judgeOtherSupport;
    private String addlnInfoCourtStaff;

    private String judgeHearingTimeEstimateText1;
    private String judgeHearingCourtLocationText1;
    private String hearingPreferencesPreferredTypeLabel1;
    private String judgeHearingSupportReqText1;
    private String judicialVulnerabilityText;

    @JsonCreator
    GAJudgesHearingListGAspec(@JsonProperty("hearingPreferencesPreferredType")
                                  GAJudicialHearingType hearingPreferencesPreferredType,
                              @JsonProperty("hearingPreferredLocation") DynamicList hearingPreferredLocation,
                              @JsonProperty("judicialTimeEstimate") GAHearingDuration judicialTimeEstimate,
                              @JsonProperty("judicialTimeEstimateDays") String judicialTimeEstimateDays,
                              @JsonProperty("judicialTimeEstimateHours") String judicialTimeEstimateHours,
                              @JsonProperty("judicialTimeEstimateMinutes") String judicialTimeEstimateMinutes,
                              @JsonProperty("judicialSupportRequirement")
                                  List<SupportRequirements> judicialSupportRequirement,
                              @JsonProperty("judgeSignLanguage") String judgeSignLanguage,
                              @JsonProperty("judgeLanguageInterpreter") String judgeLanguageInterpreter,
                              @JsonProperty("judgeOtherSupport") String judgeOtherSupport,
                              @JsonProperty("addlnInfoCourtStaff") String addlnInfoCourtStaff,
                              @JsonProperty("judgeHearingTimeEstimateText1") String judgeHearingTimeEstimateText1,
                              @JsonProperty("judgeHearingCourtLocationText1") String judgeHearingCourtLocationText1,
                              @JsonProperty("hearingPreferencesPreferredTypeLabel1")
                                  String hearingPreferencesPreferredTypeLabel1,
                              @JsonProperty("judgeHearingSupportReqText1") String judgeHearingSupportReqText1,
                              @JsonProperty("judicialVulnerabilityText") String judicialVulnerabilityText) {

        this.hearingPreferencesPreferredType = hearingPreferencesPreferredType;
        this.hearingPreferredLocation = hearingPreferredLocation;
        this.judicialSupportRequirement = judicialSupportRequirement;
        this.judicialTimeEstimate = judicialTimeEstimate;
        this.judicialTimeEstimateDays = judicialTimeEstimateDays;
        this.judicialTimeEstimateHours = judicialTimeEstimateHours;
        this.judicialTimeEstimateMinutes = judicialTimeEstimateMinutes;
        this.addlnInfoCourtStaff = addlnInfoCourtStaff;
        this.judgeHearingTimeEstimateText1 = judgeHearingTimeEstimateText1;
        this.judgeHearingCourtLocationText1 = judgeHearingCourtLocationText1;
        this.hearingPreferencesPreferredTypeLabel1 = hearingPreferencesPreferredTypeLabel1;
        this.judgeHearingSupportReqText1 = judgeHearingSupportReqText1;
        this.judicialVulnerabilityText = judicialVulnerabilityText;
        this.judgeOtherSupport = judgeOtherSupport;
        this.judgeSignLanguage = judgeSignLanguage;
        this.judgeLanguageInterpreter = judgeLanguageInterpreter;

    }

    public GAJudgesHearingListGAspec copy() {
        return new GAJudgesHearingListGAspec()
            .setHearingPreferencesPreferredType(hearingPreferencesPreferredType)
            .setHearingPreferredLocation(hearingPreferredLocation)
            .setJudicialTimeEstimate(judicialTimeEstimate)
            .setJudicialTimeEstimateDays(judicialTimeEstimateDays)
            .setJudicialTimeEstimateHours(judicialTimeEstimateHours)
            .setJudicialTimeEstimateMinutes(judicialTimeEstimateMinutes)
            .setJudicialSupportRequirement(judicialSupportRequirement)
            .setJudgeSignLanguage(judgeSignLanguage)
            .setJudgeLanguageInterpreter(judgeLanguageInterpreter)
            .setJudgeOtherSupport(judgeOtherSupport)
            .setAddlnInfoCourtStaff(addlnInfoCourtStaff)
            .setJudgeHearingTimeEstimateText1(judgeHearingTimeEstimateText1)
            .setJudgeHearingCourtLocationText1(judgeHearingCourtLocationText1)
            .setHearingPreferencesPreferredTypeLabel1(hearingPreferencesPreferredTypeLabel1)
            .setJudgeHearingSupportReqText1(judgeHearingSupportReqText1)
            .setJudicialVulnerabilityText(judicialVulnerabilityText);
    }
}
