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
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import static org.assertj.core.api.Assertions.assertThat;

class SdoR2SmallClaimsDirectionsServiceTest {

    private final SdoR2SmallClaimsDirectionsService service = new SdoR2SmallClaimsDirectionsService();

    @Test
    void shouldDetectHearingWindowToggle() {
        CaseData caseData = CaseDataBuilder.builder().build();
        SdoR2SmallClaimsHearing hearing = new SdoR2SmallClaimsHearing();
        hearing.setTrialOnOptions(HearingOnRadioOptions.HEARING_WINDOW);
        caseData.setSdoR2SmallClaimsHearing(hearing);

        assertThat(service.hasHearingTrialWindow(caseData)).isTrue();
    }

    @Test
    void shouldDescribePhysicalBundleText() {
        SdoR2SmallClaimsHearing partyHearing = new SdoR2SmallClaimsHearing();
        partyHearing.setPhysicalBundleOptions(SmallClaimsSdoR2PhysicalTrialBundleOptions.PARTY);
        SdoR2SmallClaimsBundleOfDocs partyDocs = new SdoR2SmallClaimsBundleOfDocs();
        partyDocs.setPhysicalBundlePartyTxt("Claimant");
        partyHearing.setSdoR2SmallClaimsBundleOfDocs(partyDocs);
        CaseData partyBundle = CaseDataBuilder.builder().build();
        partyBundle.setSdoR2SmallClaimsHearing(partyHearing);

        assertThat(service.getPhysicalTrialBundleText(partyBundle)).isEqualTo("Claimant");

        CaseData noneBundle = CaseDataBuilder.builder().build();
        SdoR2SmallClaimsHearing noneHearing = new SdoR2SmallClaimsHearing();
        noneHearing.setPhysicalBundleOptions(SmallClaimsSdoR2PhysicalTrialBundleOptions.NO);
        noneBundle.setSdoR2SmallClaimsHearing(noneHearing);
        assertThat(service.getPhysicalTrialBundleText(noneBundle)).isEqualTo("None");
    }

    @Test
    void shouldFormatHearingTime() {
        SdoR2SmallClaimsHearing otherHearing = new SdoR2SmallClaimsHearing();
        otherHearing.setLengthList(SmallClaimsSdoR2TimeEstimate.OTHER);
        SdoR2SmallClaimsHearingLengthOther lengthOther = new SdoR2SmallClaimsHearingLengthOther();
        lengthOther.setTrialLengthDays(1);
        lengthOther.setTrialLengthHours(2);
        lengthOther.setTrialLengthMinutes(30);
        otherHearing.setLengthListOther(lengthOther);
        CaseData other = CaseDataBuilder.builder().build();
        other.setSdoR2SmallClaimsHearing(otherHearing);

        CaseData fixed = CaseDataBuilder.builder().build();
        SdoR2SmallClaimsHearing fixedHearing = new SdoR2SmallClaimsHearing();
        fixedHearing.setLengthList(SmallClaimsSdoR2TimeEstimate.ONE_HOUR);
        fixed.setSdoR2SmallClaimsHearing(fixedHearing);

        assertThat(service.getHearingTime(other)).isEqualTo("1 days, 2 hours, 30 minutes");
        assertThat(service.getHearingTime(fixed)).isEqualTo(SmallClaimsSdoR2TimeEstimate.ONE_HOUR.getLabel());
    }

    @Test
    void shouldReturnHearingMethodLabel() {
        DynamicListElement video = new DynamicListElement();
        video.setLabel(HearingMethod.VIDEO.getLabel());
        DynamicList methodList = new DynamicList();
        methodList.setValue(video);
        SdoR2SmallClaimsHearing hearing = new SdoR2SmallClaimsHearing();
        hearing.setMethodOfHearing(methodList);
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setSdoR2SmallClaimsHearing(hearing);

        assertThat(service.getHearingMethod(caseData)).isEqualTo("by video");
    }

    @Test
    void shouldResolveHearingLocations() {
        DynamicListElement otherLocation = new DynamicListElement();
        otherLocation.setCode("OTHER_LOCATION");
        otherLocation.setLabel("Other");
        DynamicListElement altLocation = new DynamicListElement();
        altLocation.setCode("ALT");
        altLocation.setLabel("Alt Location");
        DynamicList hearingCourtList = new DynamicList();
        hearingCourtList.setValue(otherLocation);
        DynamicList altCourtList = new DynamicList();
        altCourtList.setValue(altLocation);
        CaseData caseData = CaseDataBuilder.builder().build();
        SdoR2SmallClaimsHearing hearing = new SdoR2SmallClaimsHearing();
        hearing.setHearingCourtLocationList(hearingCourtList);
        hearing.setAltHearingCourtLocationList(altCourtList);
        caseData.setSdoR2SmallClaimsHearing(hearing);

        assertThat(service.getHearingLocation(caseData).getValue().getLabel()).isEqualTo("Alt Location");
    }
}
