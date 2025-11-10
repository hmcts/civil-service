package uk.gov.hmcts.reform.civil.service.sdo;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.sdo.DateToShowToggle;
import uk.gov.hmcts.reform.civil.enums.sdo.DisposalHearingBundleType;
import uk.gov.hmcts.reform.civil.enums.sdo.DisposalHearingFinalDisposalHearingTimeEstimate;
import uk.gov.hmcts.reform.civil.enums.sdo.DisposalHearingMethodTelephoneHearing;
import uk.gov.hmcts.reform.civil.enums.sdo.DisposalHearingMethodVideoConferenceHearing;
import uk.gov.hmcts.reform.civil.enums.sdo.OrderDetailsPagesSectionsToggle;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingBundle;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingFinalDisposalHearing;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingHearingTime;
import uk.gov.hmcts.reform.civil.model.sdo.TrialHearingTimeDJ;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SdoDisposalDirectionsServiceTest {

    private final SdoDisposalDirectionsService service = new SdoDisposalDirectionsService();

    @Test
    void shouldReturnFinalHearingTimeLabel() {
        CaseData caseData = CaseData.builder()
            .disposalHearingFinalDisposalHearing(
                DisposalHearingFinalDisposalHearing.builder()
                    .time(DisposalHearingFinalDisposalHearingTimeEstimate.THIRTY_MINUTES)
                    .build())
            .build();

        assertThat(service.getFinalHearingTimeLabel(caseData)).isEqualTo("30 minutes");
    }

    @Test
    void shouldReturnTelephoneAndVideoLabels() {
        CaseData caseData = CaseData.builder()
            .disposalHearingMethodTelephoneHearing(DisposalHearingMethodTelephoneHearing.telephoneTheClaimant)
            .disposalHearingMethodVideoConferenceHearing(DisposalHearingMethodVideoConferenceHearing.videoTheDefendant)
            .build();

        assertThat(service.getTelephoneHearingLabel(caseData)).isEqualTo("the claimant");
        assertThat(service.getVideoConferenceHearingLabel(caseData)).isEqualTo("the defendant");
    }

    @Test
    void shouldFormatBundleTypes() {
        CaseData caseData = CaseData.builder()
            .disposalHearingBundle(DisposalHearingBundle.builder()
                                      .type(List.of(
                                          DisposalHearingBundleType.DOCUMENTS,
                                          DisposalHearingBundleType.ELECTRONIC))
                                      .build())
            .build();

        String expected = DisposalHearingBundleType.DOCUMENTS.getLabel()
            + " / "
            + DisposalHearingBundleType.ELECTRONIC.getLabel();
        assertThat(service.getBundleTypeText(caseData)).isEqualTo(expected);
    }

    @Test
    void shouldDetectDisposalVariables() {
        CaseData caseData = CaseData.builder()
            .disposalHearingWitnessOfFactToggle(List.of(OrderDetailsPagesSectionsToggle.SHOW))
            .trialHearingTimeDJ(TrialHearingTimeDJ.builder()
                                    .dateToToggle(List.of(DateToShowToggle.SHOW))
                                    .build())
            .build();

        assertThat(service.hasDisposalVariable(caseData, "disposalHearingWitnessOfFactToggle")).isTrue();
        assertThat(service.hasDisposalVariable(caseData, "disposalHearingDateToToggle")).isTrue();
        assertThat(service.hasDisposalVariable(caseData, "unknownToggle")).isFalse();
    }

    @Test
    void shouldFormatOtherHearingTime() {
        CaseData caseData = CaseData.builder()
            .disposalHearingHearingTime(DisposalHearingHearingTime.builder()
                                           .time(DisposalHearingFinalDisposalHearingTimeEstimate.OTHER)
                                           .otherHours("1")
                                           .otherMinutes("30")
                                           .build())
            .build();

        assertThat(service.getHearingTimeLabel(caseData)).isEqualTo("1 hour 30 minutes");
    }

    @Test
    void shouldReturnEmptyWhenHearingTimeMissing() {
        CaseData caseData = CaseData.builder().build();

        assertThat(service.getHearingTimeLabel(caseData)).isEmpty();
    }
}
