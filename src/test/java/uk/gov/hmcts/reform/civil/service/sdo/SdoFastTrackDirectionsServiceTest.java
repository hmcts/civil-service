package uk.gov.hmcts.reform.civil.service.sdo;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.enums.sdo.DateToShowToggle;
import uk.gov.hmcts.reform.civil.enums.sdo.FastTrack;
import uk.gov.hmcts.reform.civil.enums.sdo.FastTrackHearingTimeEstimate;
import uk.gov.hmcts.reform.civil.enums.sdo.OrderDetailsPagesSectionsToggle;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackHearingTime;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SdoFastTrackDirectionsServiceTest {

    private final SdoFastTrackDirectionsService service = new SdoFastTrackDirectionsService();

    @Test
    void shouldResolveAdditionalDirectionsFromEitherSource() {
        CaseData viaFastClaims = CaseDataBuilder.builder().build();
        viaFastClaims.setFastClaims(List.of(FastTrack.FAST_CLAIM_PERSONAL_INJURY));

        CaseData viaTrialSelections = CaseDataBuilder.builder().build();
        viaTrialSelections.setTrialAdditionalDirectionsForFastTrack(List.of(FastTrack.FAST_CLAIM_ROAD_TRAFFIC_ACCIDENT));

        assertThat(service.hasFastAdditionalDirections(viaFastClaims, FastTrack.FAST_CLAIM_PERSONAL_INJURY)).isTrue();
        assertThat(service.hasFastAdditionalDirections(viaFastClaims, FastTrack.FAST_CLAIM_ROAD_TRAFFIC_ACCIDENT)).isFalse();
        assertThat(service.hasFastAdditionalDirections(viaTrialSelections, FastTrack.FAST_CLAIM_ROAD_TRAFFIC_ACCIDENT)).isTrue();
    }

    @Test
    void shouldDetectFastTrackVariableStates() {
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setFastTrackAltDisputeResolutionToggle(List.of(OrderDetailsPagesSectionsToggle.SHOW));
        caseData.setFastTrackTrialBundleToggle(List.of(OrderDetailsPagesSectionsToggle.SHOW));
        FastTrackHearingTime hearingTime = new FastTrackHearingTime();
        hearingTime.setDateToToggle(List.of(DateToShowToggle.SHOW));
        hearingTime.setHearingDuration(FastTrackHearingTimeEstimate.TWO_HOURS);
        caseData.setFastTrackHearingTime(hearingTime);

        assertThat(service.hasFastTrackVariable(caseData, FastTrackVariable.ALT_DISPUTE_RESOLUTION)).isTrue();
        assertThat(service.hasFastTrackVariable(caseData, FastTrackVariable.TRIAL_DATE_TO_TOGGLE)).isTrue();
        assertThat(service.hasFastTrackVariable(caseData, FastTrackVariable.TRIAL_BUNDLE_TOGGLE)).isTrue();
        assertThat(service.hasFastTrackVariable(caseData, FastTrackVariable.METHOD_TOGGLE)).isTrue();
    }
}
