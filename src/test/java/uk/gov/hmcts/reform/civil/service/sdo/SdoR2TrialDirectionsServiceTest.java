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

import static org.assertj.core.api.Assertions.assertThat;

class SdoR2TrialDirectionsServiceTest {

    private final SdoR2TrialDirectionsService service = new SdoR2TrialDirectionsService();

    @Test
    void shouldReturnPrimaryHearingLocationWhenPresent() {
        DynamicListElement location = DynamicListElement.builder().code("ABC").label("Court A").build();
        CaseData caseData = CaseData.builder()
            .sdoR2Trial(SdoR2Trial.builder()
                            .hearingCourtLocationList(DynamicList.builder().value(location).build())
                            .build())
            .build();

        assertThat(service.getHearingLocation(caseData)).isNotNull();
    }

    @Test
    void shouldFallbackToAltLocationWhenPrimaryIsOther() {
        DynamicListElement other = DynamicListElement.builder().code("OTHER_LOCATION").label("Other").build();
        DynamicListElement alt = DynamicListElement.builder().code("ALT").label("Alt Court").build();
        CaseData caseData = CaseData.builder()
            .sdoR2Trial(SdoR2Trial.builder()
                            .hearingCourtLocationList(DynamicList.builder().value(other).build())
                            .altHearingCourtLocationList(DynamicList.builder().value(alt).build())
                            .build())
            .build();

        assertThat(service.getHearingLocation(caseData).getValue().getLabel()).isEqualTo("Alt Court");
    }

    @Test
    void shouldDescribePhysicalBundlePartyText() {
        CaseData caseData = CaseData.builder()
            .sdoR2Trial(SdoR2Trial.builder()
                            .physicalBundleOptions(PhysicalTrialBundleOptions.PARTY)
                            .physicalBundlePartyTxt("Claimant to supply")
                            .build())
            .build();

        assertThat(service.getPhysicalBundlePartyText(caseData)).isEqualTo("Claimant to supply");
    }

    @Test
    void shouldDetectRestrictFlags() {
        CaseData caseData = CaseData.builder()
            .sdoR2WitnessesOfFact(SdoR2WitnessOfFact.builder()
                                      .sdoR2RestrictWitness(SdoR2RestrictWitness.builder()
                                                                .isRestrictWitness(YesOrNo.YES).build())
                                      .sdoRestrictPages(SdoR2RestrictPages.builder()
                                                            .isRestrictPages(YesOrNo.NO).build())
                                      .build())
            .build();

        assertThat(service.hasRestrictWitness(caseData)).isTrue();
        assertThat(service.hasRestrictPages(caseData)).isFalse();
    }

    @Test
    void shouldReportApplicationAndPecuniaryLossFlags() {
        CaseData caseData = CaseData.builder()
            .sdoR2QuestionsClaimantExpert(SdoR2QuestionsClaimantExpert.builder()
                                              .sdoApplicationToRelyOnFurther(
                                                  SdoR2ApplicationToRelyOnFurther.builder()
                                                      .doRequireApplicationToRely(YesOrNo.YES)
                                                      .build())
                                              .build())
            .sdoR2ScheduleOfLoss(SdoR2ScheduleOfLoss.builder()
                                     .isClaimForPecuniaryLoss(YesOrNo.NO)
                                     .build())
            .build();

        assertThat(service.hasApplicationToRelyOnFurther(caseData)).isTrue();
        assertThat(service.hasClaimForPecuniaryLoss(caseData)).isFalse();
    }

    @Test
    void shouldFormatTrialLengthAndMethod() {
        CaseData otherLength = CaseData.builder()
            .sdoR2Trial(SdoR2Trial.builder()
                            .lengthList(FastTrackHearingTimeEstimate.OTHER)
                            .lengthListOther(SdoR2TrialHearingLengthOther.builder()
                                                 .trialLengthDays(2)
                                                 .trialLengthHours(3)
                                                 .trialLengthMinutes(30)
                                                 .build())
                            .methodOfHearing(DynamicList.builder()
                                                 .value(DynamicListElement.builder()
                                                            .label(HearingMethod.TELEPHONE.getLabel())
                                                            .build())
                                                 .build())
                            .build())
            .build();

        assertThat(service.getTrialHearingTimeAllocated(otherLength)).isEqualTo("2 days, 3 hours and 30 minutes");
        assertThat(service.getTrialMethodOfHearing(otherLength)).isEqualTo("by telephone");
    }
}
