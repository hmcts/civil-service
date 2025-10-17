package uk.gov.hmcts.reform.civil.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.enums.dq.GAHearingDuration;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.genapplication.GAJudgesHearingListGAspec;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import static org.assertj.core.api.Assertions.assertThat;

public class JudicialTimeEstimateHelperTest {

    private JudicialTimeEstimateHelper timeEstimateHelper;

    @BeforeEach
    void setUp() {
        timeEstimateHelper = new JudicialTimeEstimateHelper();
    }

    @Test
    void whenJudgeSpecifiesOption_ShouldGetHearingEstimateText() {
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData = caseData.toBuilder()
            .judicialListForHearing(GAJudgesHearingListGAspec.builder()
                                        .judicialTimeEstimate(GAHearingDuration.MINUTES_15)
                                        .build())
            .build();

        String timeEstimate = timeEstimateHelper.getEstimatedHearingLength(caseData);

        assertThat(timeEstimate).isEqualTo("15 minutes");
    }

    @Test
    void whenJudgeSpecifiesAllTimeFields_ShouldGetHearingEstimateText() {
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData = caseData.toBuilder()
            .judicialListForHearing(GAJudgesHearingListGAspec.builder()
                                        .judicialTimeEstimate(GAHearingDuration.OTHER)
                                        .judicialTimeEstimateDays("2")
                                        .judicialTimeEstimateHours("2")
                                        .judicialTimeEstimateMinutes("30").build())
            .build();

        String timeEstimate = timeEstimateHelper.getEstimatedHearingLength(caseData);

        assertThat(timeEstimate).isEqualTo("2 days, 2 hours and 30 minutes");
    }

    @Test
    void whenJudgeSpecifiesDayAndMinutes_ShouldGetHearingEstimateText() {
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData = caseData.toBuilder()
            .judicialListForHearing(GAJudgesHearingListGAspec.builder()
                                        .judicialTimeEstimate(GAHearingDuration.OTHER)
                                        .judicialTimeEstimateDays("1")
                                        .judicialTimeEstimateMinutes("30").build())
            .build();

        String timeEstimate = timeEstimateHelper.getEstimatedHearingLength(caseData);

        assertThat(timeEstimate).isEqualTo("1 day and 30 minutes");
    }

    @Test
    void whenJudgeSpecifiesDaysAndHours_ShouldGetHearingEstimateText() {
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData = caseData.toBuilder()
            .judicialListForHearing(GAJudgesHearingListGAspec.builder()
                                        .judicialTimeEstimate(GAHearingDuration.OTHER)
                                        .judicialTimeEstimateDays("2")
                                        .judicialTimeEstimateHours("6").build())
            .build();

        String timeEstimate = timeEstimateHelper.getEstimatedHearingLength(caseData);

        assertThat(timeEstimate).isEqualTo("2 days and 6 hours");
    }

    @Test
    void whenJudgeSpecifiesHoursAndMinutes_ShouldGetHearingEstimateText() {
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData = caseData.toBuilder()
            .judicialListForHearing(GAJudgesHearingListGAspec.builder()
                                        .judicialTimeEstimate(GAHearingDuration.OTHER)
                                        .judicialTimeEstimateHours("2")
                                        .judicialTimeEstimateMinutes("30").build())
            .build();

        String timeEstimate = timeEstimateHelper.getEstimatedHearingLength(caseData);

        assertThat(timeEstimate).isEqualTo("2 hours and 30 minutes");
    }

    @Test
    void whenJudgeSpecifiesDays_ShouldGetHearingEstimateText() {
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData = caseData.toBuilder()
            .judicialListForHearing(GAJudgesHearingListGAspec.builder()
                                        .judicialTimeEstimate(GAHearingDuration.OTHER)
                                        .judicialTimeEstimateDays("3").build())
            .build();

        String timeEstimate = timeEstimateHelper.getEstimatedHearingLength(caseData);

        assertThat(timeEstimate).isEqualTo("3 days");
    }

    @Test
    void whenJudgeSpecifiesHours_ShouldGetHearingEstimateText() {
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData = caseData.toBuilder()
            .judicialListForHearing(GAJudgesHearingListGAspec.builder()
                                        .judicialTimeEstimate(GAHearingDuration.OTHER)
                                        .judicialTimeEstimateHours("6").build())
            .build();

        String timeEstimate = timeEstimateHelper.getEstimatedHearingLength(caseData);

        assertThat(timeEstimate).isEqualTo("6 hours");
    }

    @Test
    void whenJudgeSpecifiesMinutes_ShouldGetHearingEstimateText() {
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData = caseData.toBuilder()
            .judicialListForHearing(GAJudgesHearingListGAspec.builder()
                                        .judicialTimeEstimate(GAHearingDuration.OTHER)
                                        .judicialTimeEstimateMinutes("45").build())
            .build();

        String timeEstimate = timeEstimateHelper.getEstimatedHearingLength(caseData);

        assertThat(timeEstimate).isEqualTo("45 minutes");
    }
}
