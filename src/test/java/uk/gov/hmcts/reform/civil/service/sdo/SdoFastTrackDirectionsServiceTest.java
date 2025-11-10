package uk.gov.hmcts.reform.civil.service.sdo;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.enums.ComplexityBand;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.sdo.DateToShowToggle;
import uk.gov.hmcts.reform.civil.enums.sdo.FastTrack;
import uk.gov.hmcts.reform.civil.enums.sdo.FastTrackHearingTimeEstimate;
import uk.gov.hmcts.reform.civil.enums.sdo.FastTrackMethodTelephoneHearing;
import uk.gov.hmcts.reform.civil.enums.sdo.FastTrackMethodVideoConferenceHearing;
import uk.gov.hmcts.reform.civil.enums.sdo.FastTrackTrialBundleType;
import uk.gov.hmcts.reform.civil.enums.sdo.OrderDetailsPagesSectionsToggle;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackAllocation;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackHearingTime;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackTrial;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SdoFastTrackDirectionsServiceTest {

    private final SdoFastTrackDirectionsService service = new SdoFastTrackDirectionsService();

    @Test
    void shouldResolveAdditionalDirectionsFromEitherSource() {
        CaseData viaFastClaims = CaseData.builder()
            .fastClaims(List.of(FastTrack.fastClaimPersonalInjury))
            .build();

        CaseData viaTrialSelections = CaseData.builder()
            .trialAdditionalDirectionsForFastTrack(List.of(FastTrack.fastClaimRoadTrafficAccident))
            .build();

        assertThat(service.hasFastAdditionalDirections(viaFastClaims, "fastClaimPersonalInjury")).isTrue();
        assertThat(service.hasFastAdditionalDirections(viaFastClaims, "fastClaimRoadTrafficAccident")).isFalse();
        assertThat(service.hasFastAdditionalDirections(viaTrialSelections, "fastClaimRoadTrafficAccident")).isTrue();
    }

    @Test
    void shouldDetectFastTrackVariableStates() {
        CaseData caseData = CaseData.builder()
            .fastTrackAltDisputeResolutionToggle(List.of(OrderDetailsPagesSectionsToggle.SHOW))
            .fastTrackTrialBundleToggle(List.of(OrderDetailsPagesSectionsToggle.SHOW))
            .fastTrackHearingTime(FastTrackHearingTime.builder()
                                      .dateToToggle(List.of(DateToShowToggle.SHOW))
                                      .hearingDuration(FastTrackHearingTimeEstimate.TWO_HOURS)
                                      .build())
            .build();

        assertThat(service.hasFastTrackVariable(caseData, "fastTrackAltDisputeResolutionToggle")).isTrue();
        assertThat(service.hasFastTrackVariable(caseData, "fastTrackTrialDateToToggle")).isTrue();
        assertThat(service.hasFastTrackVariable(caseData, "fastTrackTrialBundleToggle")).isTrue();
        assertThat(service.hasFastTrackVariable(caseData, "unknownToggle")).isFalse();
    }

    @Test
    void shouldReturnHearingMethodLabels() {
        CaseData caseData = CaseData.builder()
            .fastTrackMethodTelephoneHearing(FastTrackMethodTelephoneHearing.telephoneTheClaimant)
            .fastTrackMethodVideoConferenceHearing(FastTrackMethodVideoConferenceHearing.videoTheDefendant)
            .build();

        assertThat(service.getFastTrackMethodTelephoneHearingLabel(caseData)).isEqualTo("the claimant");
        assertThat(service.getFastTrackMethodVideoConferenceHearingLabel(caseData)).isEqualTo("the defendant");
    }

    @Test
    void shouldFormatFastTrackHearingTime() {
        CaseData otherDuration = CaseData.builder()
            .fastTrackHearingTime(FastTrackHearingTime.builder()
                                      .hearingDuration(FastTrackHearingTimeEstimate.OTHER)
                                      .otherHours("2")
                                      .otherMinutes("30")
                                      .build())
            .build();

        CaseData standardDuration = CaseData.builder()
            .fastTrackHearingTime(FastTrackHearingTime.builder()
                                      .hearingDuration(FastTrackHearingTimeEstimate.TWO_HOURS)
                                      .build())
            .build();

        assertThat(service.getFastClaimsHearingTimeLabel(otherDuration)).isEqualTo("2 hours 30 minutes");
        assertThat(service.getFastClaimsHearingTimeLabel(standardDuration)).isEqualTo("2 hours");
    }

    @Test
    void shouldDescribeTrialBundleSelections() {
        CaseData caseData = CaseData.builder()
            .fastTrackTrial(FastTrackTrial.builder()
                                .type(List.of(
                                    FastTrackTrialBundleType.DOCUMENTS,
                                    FastTrackTrialBundleType.ELECTRONIC))
                                .build())
            .build();

        String expected = FastTrackTrialBundleType.DOCUMENTS.getLabel()
            + " / "
            + FastTrackTrialBundleType.ELECTRONIC.getLabel();
        assertThat(service.getFastTrackTrialBundleTypeText(caseData)).isEqualTo(expected);
    }

    @Test
    void shouldDescribeFastTrackAllocationWithoutBand() {
        CaseData caseData = CaseData.builder()
            .fastTrackAllocation(FastTrackAllocation.builder()
                                     .assignComplexityBand(YesOrNo.NO)
                                     .reasons("it is proportionate")
                                     .build())
            .build();

        assertThat(service.getFastTrackAllocation(caseData))
            .isEqualTo("The claim is allocated to the Fast Track and is not assigned to a complexity band because it is proportionate");
    }

    @Test
    void shouldDescribeFastTrackAllocationWithBand() {
        CaseData caseData = CaseData.builder()
            .fastTrackAllocation(FastTrackAllocation.builder()
                                     .assignComplexityBand(YesOrNo.YES)
                                     .band(ComplexityBand.BAND_2)
                                     .reasons("complex evidence")
                                     .build())
            .build();

        assertThat(service.getFastTrackAllocation(caseData))
            .isEqualTo("The claim is allocated to the Fast Track and is assigned to complexity band 2 because complex evidence");
    }

    @Test
    void shouldReturnEmptyAllocationSummaryWhenMissing() {
        CaseData caseData = CaseData.builder().build();

        assertThat(service.getFastTrackAllocation(caseData)).isEmpty();
    }
}
