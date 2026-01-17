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
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SdoFastTrackTemplateFieldServiceTest {

    private final SdoFastTrackTemplateFieldService service = new SdoFastTrackTemplateFieldService();

    @Test
    void shouldResolveHearingMethodLabels() {
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setFastTrackMethodTelephoneHearing(FastTrackMethodTelephoneHearing.telephoneTheClaimant);
        caseData.setFastTrackMethodVideoConferenceHearing(FastTrackMethodVideoConferenceHearing.videoTheDefendant);

        assertThat(service.getMethodTelephoneHearingLabel(caseData)).isEqualTo("the claimant");
        assertThat(service.getMethodVideoConferenceHearingLabel(caseData)).isEqualTo("the defendant");
    }

    @Test
    void shouldDescribeTrialBundleSelections() {
        CaseData caseData = CaseDataBuilder.builder().build();
        FastTrackTrial trial = new FastTrackTrial();
        trial.setType(List.of(FastTrackTrialBundleType.DOCUMENTS, FastTrackTrialBundleType.ELECTRONIC));
        caseData.setFastTrackTrial(trial);

        String expected = FastTrackTrialBundleType.DOCUMENTS.getLabel()
            + " / "
            + FastTrackTrialBundleType.ELECTRONIC.getLabel();

        assertThat(service.getTrialBundleTypeText(caseData)).isEqualTo(expected);
    }

    @Test
    void shouldDescribeFastTrackAllocationWithoutBand() {
        FastTrackAllocation allocation = new FastTrackAllocation();
        allocation.setAssignComplexityBand(YesOrNo.NO);
        allocation.setReasons("it is proportionate");
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setFastTrackAllocation(allocation);

        assertThat(service.getAllocationSummary(caseData))
            .isEqualTo("The claim is allocated to the Fast Track and is not assigned to a complexity band because it is proportionate");
    }

    @Test
    void shouldDescribeFastTrackAllocationWithBand() {
        FastTrackAllocation allocation = new FastTrackAllocation();
        allocation.setAssignComplexityBand(YesOrNo.YES);
        allocation.setBand(ComplexityBand.BAND_2);
        allocation.setReasons("complex evidence");
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setFastTrackAllocation(allocation);

        assertThat(service.getAllocationSummary(caseData))
            .isEqualTo("The claim is allocated to the Fast Track and is assigned to complexity band 2 because complex evidence");
    }

    @Test
    void shouldReturnEmptyAllocationSummaryWhenMissing() {
        assertThat(service.getAllocationSummary(CaseDataBuilder.builder().build())).isEmpty();
    }

    @Test
    void shouldFormatFastTrackHearingTime() {
        FastTrackHearingTime otherHearingTime = new FastTrackHearingTime();
        otherHearingTime.setHearingDuration(FastTrackHearingTimeEstimate.OTHER);
        otherHearingTime.setOtherHours("2");
        otherHearingTime.setOtherMinutes("30");
        CaseData otherDuration = CaseDataBuilder.builder().build();
        otherDuration.setFastTrackHearingTime(otherHearingTime);

        CaseData standardDuration = CaseDataBuilder.builder().build();
        FastTrackHearingTime standardHearingTime = new FastTrackHearingTime();
        standardHearingTime.setHearingDuration(FastTrackHearingTimeEstimate.TWO_HOURS);
        standardDuration.setFastTrackHearingTime(standardHearingTime);

        assertThat(service.getHearingTimeLabel(otherDuration)).isEqualTo("2 hours 30 minutes");
        assertThat(service.getHearingTimeLabel(standardDuration)).isEqualTo("2 hours");
    }
}
