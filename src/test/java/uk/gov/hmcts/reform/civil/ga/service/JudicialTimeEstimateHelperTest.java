package uk.gov.hmcts.reform.civil.ga.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.enums.dq.GAHearingDuration;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.GAJudgesHearingListGAspec;
import uk.gov.hmcts.reform.civil.sampledata.GeneralApplicationCaseDataBuilder;

import static org.assertj.core.api.Assertions.assertThat;

public class JudicialTimeEstimateHelperTest {

    private JudicialTimeEstimateHelper timeEstimateHelper;

    @BeforeEach
    void setUp() {
        timeEstimateHelper = new JudicialTimeEstimateHelper();
    }

    @Test
    void whenJudgeSpecifiesOption_ShouldGetHearingEstimateText() {
        GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().build();
        caseData = caseData.copy()
            .judicialListForHearing(new GAJudgesHearingListGAspec()
                                        .setJudicialTimeEstimate(GAHearingDuration.MINUTES_15))
            .build();

        String timeEstimate = timeEstimateHelper.getEstimatedHearingLength(caseData);

        assertThat(timeEstimate).isEqualTo("15 minutes");
    }

    @Test
    void whenJudgeSpecifiesAllTimeFields_ShouldGetHearingEstimateText() {
        GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().build();
        caseData = caseData.copy()
            .judicialListForHearing(new GAJudgesHearingListGAspec()
                                        .setJudicialTimeEstimate(GAHearingDuration.OTHER)
                                        .setJudicialTimeEstimateDays("2")
                                        .setJudicialTimeEstimateHours("2")
                                        .setJudicialTimeEstimateMinutes("30"))
            .build();

        String timeEstimate = timeEstimateHelper.getEstimatedHearingLength(caseData);

        assertThat(timeEstimate).isEqualTo("2 days, 2 hours and 30 minutes");
    }

    @Test
    void whenJudgeSpecifiesDayAndMinutes_ShouldGetHearingEstimateText() {
        GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().build();
        caseData = caseData.copy()
            .judicialListForHearing(new GAJudgesHearingListGAspec()
                                        .setJudicialTimeEstimate(GAHearingDuration.OTHER)
                                        .setJudicialTimeEstimateDays("1")
                                        .setJudicialTimeEstimateMinutes("30"))
            .build();

        String timeEstimate = timeEstimateHelper.getEstimatedHearingLength(caseData);

        assertThat(timeEstimate).isEqualTo("1 day and 30 minutes");
    }

    @Test
    void whenJudgeSpecifiesDaysAndHours_ShouldGetHearingEstimateText() {
        GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().build();
        caseData = caseData.copy()
            .judicialListForHearing(new GAJudgesHearingListGAspec()
                                        .setJudicialTimeEstimate(GAHearingDuration.OTHER)
                                        .setJudicialTimeEstimateDays("2")
                                        .setJudicialTimeEstimateHours("6"))
            .build();

        String timeEstimate = timeEstimateHelper.getEstimatedHearingLength(caseData);

        assertThat(timeEstimate).isEqualTo("2 days and 6 hours");
    }

    @Test
    void whenJudgeSpecifiesHoursAndMinutes_ShouldGetHearingEstimateText() {
        GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().build();
        caseData = caseData.copy()
            .judicialListForHearing(new GAJudgesHearingListGAspec()
                                        .setJudicialTimeEstimate(GAHearingDuration.OTHER)
                                        .setJudicialTimeEstimateHours("2")
                                        .setJudicialTimeEstimateMinutes("30"))
            .build();

        String timeEstimate = timeEstimateHelper.getEstimatedHearingLength(caseData);

        assertThat(timeEstimate).isEqualTo("2 hours and 30 minutes");
    }

    @Test
    void whenJudgeSpecifiesDays_ShouldGetHearingEstimateText() {
        GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().build();
        caseData = caseData.copy()
            .judicialListForHearing(new GAJudgesHearingListGAspec()
                                        .setJudicialTimeEstimate(GAHearingDuration.OTHER)
                                        .setJudicialTimeEstimateDays("3"))
            .build();

        String timeEstimate = timeEstimateHelper.getEstimatedHearingLength(caseData);

        assertThat(timeEstimate).isEqualTo("3 days");
    }

    @Test
    void whenJudgeSpecifiesHours_ShouldGetHearingEstimateText() {
        GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().build();
        caseData = caseData.copy()
            .judicialListForHearing(new GAJudgesHearingListGAspec()
                                        .setJudicialTimeEstimate(GAHearingDuration.OTHER)
                                        .setJudicialTimeEstimateHours("6"))
            .build();

        String timeEstimate = timeEstimateHelper.getEstimatedHearingLength(caseData);

        assertThat(timeEstimate).isEqualTo("6 hours");
    }

    @Test
    void whenJudgeSpecifiesMinutes_ShouldGetHearingEstimateText() {
        GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().build();
        caseData = caseData.copy()
            .judicialListForHearing(new GAJudgesHearingListGAspec()
                                        .setJudicialTimeEstimate(GAHearingDuration.OTHER)
                                        .setJudicialTimeEstimateMinutes("45"))
            .build();

        String timeEstimate = timeEstimateHelper.getEstimatedHearingLength(caseData);

        assertThat(timeEstimate).isEqualTo("45 minutes");
    }
}
