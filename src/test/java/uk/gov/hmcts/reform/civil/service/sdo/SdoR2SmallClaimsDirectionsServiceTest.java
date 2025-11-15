package uk.gov.hmcts.reform.civil.service.sdo;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.enums.sdo.HearingMethod;
import uk.gov.hmcts.reform.civil.enums.sdo.HearingOnRadioOptions;
import uk.gov.hmcts.reform.civil.enums.sdo.SmallClaimsSdoR2PhysicalTrialBundleOptions;
import uk.gov.hmcts.reform.civil.enums.sdo.SmallClaimsSdoR2TimeEstimate;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2SmallClaimsBundleOfDocs;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2SmallClaimsHearing;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2SmallClaimsHearingLengthOther;

import static org.assertj.core.api.Assertions.assertThat;

class SdoR2SmallClaimsDirectionsServiceTest {

    private final SdoR2SmallClaimsDirectionsService service = new SdoR2SmallClaimsDirectionsService();

    @Test
    void shouldDetectHearingWindowToggle() {
        CaseData caseData = CaseData.builder()
            .sdoR2SmallClaimsHearing(SdoR2SmallClaimsHearing.builder()
                                         .trialOnOptions(HearingOnRadioOptions.HEARING_WINDOW)
                                         .build())
            .build();

        assertThat(service.hasHearingTrialWindow(caseData)).isTrue();
    }

    @Test
    void shouldDescribePhysicalBundleText() {
        CaseData partyBundle = CaseData.builder()
            .sdoR2SmallClaimsHearing(SdoR2SmallClaimsHearing.builder()
                                         .physicalBundleOptions(SmallClaimsSdoR2PhysicalTrialBundleOptions.PARTY)
                                         .sdoR2SmallClaimsBundleOfDocs(SdoR2SmallClaimsBundleOfDocs.builder()
                                                                          .physicalBundlePartyTxt("Claimant")
                                                                          .build())
                                         .build())
            .build();

        assertThat(service.getPhysicalTrialBundleText(partyBundle)).isEqualTo("Claimant");

        CaseData noneBundle = CaseData.builder()
            .sdoR2SmallClaimsHearing(SdoR2SmallClaimsHearing.builder()
                                         .physicalBundleOptions(SmallClaimsSdoR2PhysicalTrialBundleOptions.NO)
                                         .build())
            .build();
        assertThat(service.getPhysicalTrialBundleText(noneBundle)).isEqualTo("None");
    }

    @Test
    void shouldFormatHearingTime() {
        CaseData other = CaseData.builder()
            .sdoR2SmallClaimsHearing(SdoR2SmallClaimsHearing.builder()
                                         .lengthList(SmallClaimsSdoR2TimeEstimate.OTHER)
                                         .lengthListOther(SdoR2SmallClaimsHearingLengthOther.builder()
                                                              .trialLengthDays(1)
                                                              .trialLengthHours(2)
                                                              .trialLengthMinutes(30)
                                                              .build())
                                         .build())
            .build();

        CaseData fixed = CaseData.builder()
            .sdoR2SmallClaimsHearing(SdoR2SmallClaimsHearing.builder()
                                         .lengthList(SmallClaimsSdoR2TimeEstimate.ONE_HOUR)
                                         .build())
            .build();

        assertThat(service.getHearingTime(other)).isEqualTo("1 days, 2 hours, 30 minutes");
        assertThat(service.getHearingTime(fixed)).isEqualTo(SmallClaimsSdoR2TimeEstimate.ONE_HOUR.getLabel());
    }

    @Test
    void shouldReturnHearingMethodLabel() {
        CaseData caseData = CaseData.builder()
            .sdoR2SmallClaimsHearing(SdoR2SmallClaimsHearing.builder()
                                         .methodOfHearing(DynamicList.builder()
                                                             .value(DynamicListElement.builder()
                                                                        .label(HearingMethod.VIDEO.getLabel())
                                                                        .build())
                                                             .build())
                                         .build())
            .build();

        assertThat(service.getHearingMethod(caseData)).isEqualTo("by video conference");
    }

    @Test
    void shouldResolveHearingLocations() {
        DynamicListElement otherLocation = DynamicListElement.builder().code("OTHER_LOCATION").label("Other").build();
        DynamicListElement altLocation = DynamicListElement.builder().code("ALT").label("Alt Location").build();
        CaseData caseData = CaseData.builder()
            .sdoR2SmallClaimsHearing(SdoR2SmallClaimsHearing.builder()
                                         .hearingCourtLocationList(DynamicList.builder().value(otherLocation).build())
                                         .altHearingCourtLocationList(DynamicList.builder().value(altLocation).build())
                                         .build())
            .build();

        assertThat(service.getHearingLocation(caseData).getValue().getLabel()).isEqualTo("Alt Location");
    }
}
