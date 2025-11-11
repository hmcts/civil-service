package uk.gov.hmcts.reform.civil.service.sdo;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.enums.sdo.DateToShowToggle;
import uk.gov.hmcts.reform.civil.enums.sdo.FastTrack;
import uk.gov.hmcts.reform.civil.enums.sdo.FastTrackHearingTimeEstimate;
import uk.gov.hmcts.reform.civil.enums.sdo.OrderDetailsPagesSectionsToggle;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackHearingTime;

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

        assertThat(service.hasFastAdditionalDirections(viaFastClaims, FastTrack.fastClaimPersonalInjury)).isTrue();
        assertThat(service.hasFastAdditionalDirections(viaFastClaims, FastTrack.fastClaimRoadTrafficAccident)).isFalse();
        assertThat(service.hasFastAdditionalDirections(viaTrialSelections, FastTrack.fastClaimRoadTrafficAccident)).isTrue();
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

        assertThat(service.hasFastTrackVariable(caseData, FastTrackVariable.ALT_DISPUTE_RESOLUTION)).isTrue();
        assertThat(service.hasFastTrackVariable(caseData, FastTrackVariable.TRIAL_DATE_TO_TOGGLE)).isTrue();
        assertThat(service.hasFastTrackVariable(caseData, FastTrackVariable.TRIAL_BUNDLE_TOGGLE)).isTrue();
        assertThat(service.hasFastTrackVariable(caseData, FastTrackVariable.METHOD_TOGGLE)).isTrue();
    }
}
