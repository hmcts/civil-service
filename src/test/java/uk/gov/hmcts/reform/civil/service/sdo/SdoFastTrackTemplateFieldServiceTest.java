package uk.gov.hmcts.reform.civil.service.sdo;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.enums.ComplexityBand;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.sdo.FastTrackHearingTimeEstimate;
import uk.gov.hmcts.reform.civil.enums.sdo.FastTrackMethodTelephoneHearing;
import uk.gov.hmcts.reform.civil.enums.sdo.FastTrackMethodVideoConferenceHearing;
import uk.gov.hmcts.reform.civil.enums.sdo.FastTrackTrialBundleType;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackAllocation;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackHearingTime;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackTrial;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SdoFastTrackTemplateFieldServiceTest {

    private final SdoFastTrackTemplateFieldService service = new SdoFastTrackTemplateFieldService();

    @Test
    void shouldResolveHearingMethodLabels() {
        CaseData caseData = CaseData.builder()
            .fastTrackMethodTelephoneHearing(FastTrackMethodTelephoneHearing.telephoneTheClaimant)
            .fastTrackMethodVideoConferenceHearing(FastTrackMethodVideoConferenceHearing.videoTheDefendant)
            .build();

        assertThat(service.getMethodTelephoneHearingLabel(caseData)).isEqualTo("the claimant");
        assertThat(service.getMethodVideoConferenceHearingLabel(caseData)).isEqualTo("the defendant");
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

        assertThat(service.getTrialBundleTypeText(caseData)).isEqualTo(expected);
    }

    @Test
    void shouldDescribeFastTrackAllocationWithoutBand() {
        CaseData caseData = CaseData.builder()
            .fastTrackAllocation(FastTrackAllocation.builder()
                                     .assignComplexityBand(YesOrNo.NO)
                                     .reasons("it is proportionate")
                                     .build())
            .build();

        assertThat(service.getAllocationSummary(caseData))
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

        assertThat(service.getAllocationSummary(caseData))
            .isEqualTo("The claim is allocated to the Fast Track and is assigned to complexity band 2 because complex evidence");
    }

    @Test
    void shouldReturnEmptyAllocationSummaryWhenMissing() {
        assertThat(service.getAllocationSummary(CaseData.builder().build())).isEmpty();
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

        assertThat(service.getHearingTimeLabel(otherDuration)).isEqualTo("2 hours 30 minutes");
        assertThat(service.getHearingTimeLabel(standardDuration)).isEqualTo("2 hours");
    }
}
