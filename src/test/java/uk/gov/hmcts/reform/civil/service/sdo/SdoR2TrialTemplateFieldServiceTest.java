package uk.gov.hmcts.reform.civil.service.sdo;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.sdo.FastTrackHearingTimeEstimate;
import uk.gov.hmcts.reform.civil.enums.sdo.HearingMethod;
import uk.gov.hmcts.reform.civil.enums.sdo.PhysicalTrialBundleOptions;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2ApplicationToRelyOnFurther;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2QuestionsClaimantExpert;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2RestrictPages;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2RestrictWitness;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2ScheduleOfLoss;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2Trial;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2TrialHearingLengthOther;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2WitnessOfFact;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import static org.assertj.core.api.Assertions.assertThat;

class SdoR2TrialTemplateFieldServiceTest {

    private final SdoR2TrialTemplateFieldService service = new SdoR2TrialTemplateFieldService();

    @Test
    void shouldReturnPrimaryHearingLocationWhenPresent() {
        DynamicListElement location = new DynamicListElement();
        location.setCode("ABC");
        location.setLabel("Court A");
        DynamicList courtList = new DynamicList();
        courtList.setValue(location);
        CaseData caseData = CaseDataBuilder.builder().build();
        SdoR2Trial trial = new SdoR2Trial();
        trial.setHearingCourtLocationList(courtList);
        caseData.setSdoR2Trial(trial);

        assertThat(service.getHearingLocation(caseData)).isNotNull();
    }

    @Test
    void shouldFallbackToAltLocationWhenPrimaryIsOther() {
        DynamicListElement other = new DynamicListElement();
        other.setCode("OTHER_LOCATION");
        other.setLabel("Other");
        DynamicListElement alt = new DynamicListElement();
        alt.setCode("ALT");
        alt.setLabel("Alt Court");
        DynamicList hearingList = new DynamicList();
        hearingList.setValue(other);
        DynamicList altList = new DynamicList();
        altList.setValue(alt);
        SdoR2Trial trial = new SdoR2Trial();
        trial.setHearingCourtLocationList(hearingList);
        trial.setAltHearingCourtLocationList(altList);
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setSdoR2Trial(trial);

        assertThat(service.getHearingLocation(caseData).getValue().getLabel()).isEqualTo("Alt Court");
    }

    @Test
    void shouldDescribePhysicalBundlePartyText() {
        CaseData caseData = CaseDataBuilder.builder().build();
        SdoR2Trial trial = new SdoR2Trial();
        trial.setPhysicalBundleOptions(PhysicalTrialBundleOptions.PARTY);
        trial.setPhysicalBundlePartyTxt("Claimant to supply");
        caseData.setSdoR2Trial(trial);

        assertThat(service.getPhysicalBundlePartyText(caseData)).isEqualTo("Claimant to supply");
    }

    @Test
    void shouldDetectRestrictFlags() {
        SdoR2RestrictWitness restrictWitness = new SdoR2RestrictWitness();
        restrictWitness.setIsRestrictWitness(YesOrNo.YES);
        SdoR2RestrictPages restrictPages = new SdoR2RestrictPages();
        restrictPages.setIsRestrictPages(YesOrNo.NO);
        SdoR2WitnessOfFact witnessOfFact = new SdoR2WitnessOfFact();
        witnessOfFact.setSdoR2RestrictWitness(restrictWitness);
        witnessOfFact.setSdoRestrictPages(restrictPages);
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setSdoR2WitnessesOfFact(witnessOfFact);

        assertThat(service.hasRestrictWitness(caseData)).isTrue();
        assertThat(service.hasRestrictPages(caseData)).isFalse();
    }

    @Test
    void shouldReportApplicationAndPecuniaryLossFlags() {
        CaseData caseData = CaseDataBuilder.builder().build();
        SdoR2ApplicationToRelyOnFurther applicationToRely = new SdoR2ApplicationToRelyOnFurther();
        applicationToRely.setDoRequireApplicationToRely(YesOrNo.YES);
        SdoR2QuestionsClaimantExpert questionsClaimantExpert = new SdoR2QuestionsClaimantExpert();
        questionsClaimantExpert.setSdoApplicationToRelyOnFurther(applicationToRely);
        caseData.setSdoR2QuestionsClaimantExpert(questionsClaimantExpert);
        SdoR2ScheduleOfLoss scheduleOfLoss = new SdoR2ScheduleOfLoss();
        scheduleOfLoss.setIsClaimForPecuniaryLoss(YesOrNo.NO);
        caseData.setSdoR2ScheduleOfLoss(scheduleOfLoss);

        assertThat(service.hasApplicationToRelyOnFurther(caseData)).isTrue();
        assertThat(service.hasClaimForPecuniaryLoss(caseData)).isFalse();
    }

    @Test
    void shouldFormatTrialLengthAndMethod() {
        SdoR2Trial trial = new SdoR2Trial();
        trial.setLengthList(FastTrackHearingTimeEstimate.OTHER);
        SdoR2TrialHearingLengthOther hearingLengthOther = new SdoR2TrialHearingLengthOther();
        hearingLengthOther.setTrialLengthDays(2);
        hearingLengthOther.setTrialLengthHours(3);
        hearingLengthOther.setTrialLengthMinutes(30);
        trial.setLengthListOther(hearingLengthOther);
        DynamicListElement telephone = new DynamicListElement();
        telephone.setLabel(HearingMethod.TELEPHONE.getLabel());
        DynamicList methodList = new DynamicList();
        methodList.setValue(telephone);
        trial.setMethodOfHearing(methodList);
        CaseData otherLength = CaseDataBuilder.builder().build();
        otherLength.setSdoR2Trial(trial);

        assertThat(service.getTrialHearingTimeAllocated(otherLength)).isEqualTo("2 days, 3 hours and 30 minutes");
        assertThat(service.getTrialMethodOfHearing(otherLength)).isEqualTo("by telephone");
    }
}
