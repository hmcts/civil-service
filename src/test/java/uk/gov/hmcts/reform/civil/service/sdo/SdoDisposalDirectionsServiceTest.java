package uk.gov.hmcts.reform.civil.service.sdo;

import org.junit.jupiter.api.Test;
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
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SdoDisposalDirectionsServiceTest {

    private final SdoDisposalDirectionsService service = new SdoDisposalDirectionsService();

    @Test
    void shouldReturnFinalHearingTimeLabel() {
        CaseData caseData = CaseDataBuilder.builder().build();
        DisposalHearingFinalDisposalHearing finalDisposalHearing = new DisposalHearingFinalDisposalHearing();
        finalDisposalHearing.setTime(DisposalHearingFinalDisposalHearingTimeEstimate.THIRTY_MINUTES);
        caseData.setDisposalHearingFinalDisposalHearing(finalDisposalHearing);

        assertThat(service.getFinalHearingTimeLabel(caseData)).isEqualTo("30 minutes");
    }

    @Test
    void shouldReturnTelephoneAndVideoLabels() {
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setDisposalHearingMethodTelephoneHearing(DisposalHearingMethodTelephoneHearing.telephoneTheClaimant);
        caseData.setDisposalHearingMethodVideoConferenceHearing(DisposalHearingMethodVideoConferenceHearing.videoTheDefendant);

        assertThat(service.getTelephoneHearingLabel(caseData)).isEqualTo("the claimant");
        assertThat(service.getVideoConferenceHearingLabel(caseData)).isEqualTo("the defendant");
    }

    @Test
    void shouldFormatBundleTypes() {
        CaseData caseData = CaseDataBuilder.builder().build();
        DisposalHearingBundle bundle = new DisposalHearingBundle();
        bundle.setType(List.of(DisposalHearingBundleType.DOCUMENTS, DisposalHearingBundleType.ELECTRONIC));
        caseData.setDisposalHearingBundle(bundle);

        String expected = DisposalHearingBundleType.DOCUMENTS.getLabel()
            + " / "
            + DisposalHearingBundleType.ELECTRONIC.getLabel();
        assertThat(service.getBundleTypeText(caseData)).isEqualTo(expected);
    }

    @Test
    void shouldDetectDisposalVariables() {
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setDisposalHearingWitnessOfFactToggle(List.of(OrderDetailsPagesSectionsToggle.SHOW));
        TrialHearingTimeDJ hearingTime = new TrialHearingTimeDJ();
        hearingTime.setDateToToggle(List.of(DateToShowToggle.SHOW));
        caseData.setTrialHearingTimeDJ(hearingTime);

        assertThat(service.hasDisposalVariable(caseData, "disposalHearingWitnessOfFactToggle")).isTrue();
        assertThat(service.hasDisposalVariable(caseData, "disposalHearingDateToToggle")).isTrue();
        assertThat(service.hasDisposalVariable(caseData, "unknownToggle")).isFalse();
    }

    @Test
    void shouldFormatOtherHearingTime() {
        DisposalHearingHearingTime hearingTime = new DisposalHearingHearingTime();
        hearingTime.setTime(DisposalHearingFinalDisposalHearingTimeEstimate.OTHER);
        hearingTime.setOtherHours("1");
        hearingTime.setOtherMinutes("30");
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setDisposalHearingHearingTime(hearingTime);

        assertThat(service.getHearingTimeLabel(caseData)).isEqualTo("1 hour 30 minutes");
    }

    @Test
    void shouldReturnEmptyWhenHearingTimeMissing() {
        CaseData caseData = CaseDataBuilder.builder().build();

        assertThat(service.getHearingTimeLabel(caseData)).isEmpty();
    }
}
