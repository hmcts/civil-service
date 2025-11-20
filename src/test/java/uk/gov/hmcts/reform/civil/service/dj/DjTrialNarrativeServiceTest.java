package uk.gov.hmcts.reform.civil.service.dj;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.TrialHearingDisclosureOfDocuments;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.TrialHearingJudgesRecital;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.TrialHearingNotes;
import uk.gov.hmcts.reform.civil.model.sdo.TrialHearingTimeDJ;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.TrialHearingWitnessOfFact;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.FAST_TRACK_TRIAL_MANUAL_BUNDLE_GUIDANCE;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.SMALL_CLAIMS_WITNESS_LATE_WARNING;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.TRIAL_WITNESS_STATEMENT_UPLOAD_NOTICE;

@ExtendWith(MockitoExtension.class)
class DjTrialNarrativeServiceTest {

    private static final String JUDGE = "Recorder Blue";

    @Mock
    private DjDeadlineService deadlineService;

    private DjTrialNarrativeService service;

    @BeforeEach
    void setUp() {
        service = new DjTrialNarrativeService(deadlineService);
    }

    @Test
    void shouldBuildJudgesRecital() {
        TrialHearingJudgesRecital recital = service.buildJudgesRecital(JUDGE);

        assertThat(recital.getJudgeNameTitle()).isEqualTo(JUDGE);
        assertThat(recital.getInput()).isEqualTo(JUDGE + ",");
    }

    @Test
    void shouldBuildDisclosureOfDocumentsWithExpectedDates() {
        LocalDate fourWeeks = LocalDate.of(2025, 1, 1);
        LocalDate sixWeeks = LocalDate.of(2025, 1, 8);
        LocalDate eightWeeks = LocalDate.of(2025, 1, 15);
        when(deadlineService.nextWorkingDayInWeeks(eq(4))).thenReturn(fourWeeks);
        when(deadlineService.nextWorkingDayInWeeks(eq(6))).thenReturn(sixWeeks);
        when(deadlineService.nextWorkingDayInWeeks(eq(8))).thenReturn(eightWeeks);

        TrialHearingDisclosureOfDocuments result = service.buildDisclosureOfDocuments();

        assertThat(result.getDate1()).isEqualTo(fourWeeks);
        assertThat(result.getDate2()).isEqualTo(sixWeeks);
        assertThat(result.getDate3()).isEqualTo(eightWeeks);
    }

    @Test
    void shouldBuildTrialHearingTimeWithToggleDefaults() {
        LocalDate twentyTwoWeeks = LocalDate.of(2025, 6, 1);
        LocalDate thirtyWeeks = LocalDate.of(2025, 7, 27);
        when(deadlineService.weeksFromNow(eq(22))).thenReturn(twentyTwoWeeks);
        when(deadlineService.weeksFromNow(eq(30))).thenReturn(thirtyWeeks);

        TrialHearingTimeDJ result = service.buildTrialHearingTime();

        assertThat(result.getDate1()).isEqualTo(twentyTwoWeeks);
        assertThat(result.getDate2()).isEqualTo(thirtyWeeks);
        assertThat(result.getDateToToggle()).hasSize(1);
        assertThat(result.getHelpText2()).isEqualTo(FAST_TRACK_TRIAL_MANUAL_BUNDLE_GUIDANCE);
    }

    @Test
    void shouldBuildTrialHearingNotesWithDeadline() {
        LocalDate oneWeek = LocalDate.of(2025, 2, 1);
        when(deadlineService.nextWorkingDayInWeeks(eq(1))).thenReturn(oneWeek);

        TrialHearingNotes result = service.buildTrialHearingNotes();

        assertThat(result.getDate()).isEqualTo(oneWeek);
    }

    @Test
    void shouldBuildWitnessOfFactWithSharedText() {
        LocalDate eightWeeks = LocalDate.of(2025, 3, 1);
        when(deadlineService.nextWorkingDayInWeeks(eq(8))).thenReturn(eightWeeks);

        TrialHearingWitnessOfFact result = service.buildWitnessOfFact();

        assertThat(result.getInput1()).isEqualTo(TRIAL_WITNESS_STATEMENT_UPLOAD_NOTICE);
        assertThat(result.getInput9()).isEqualTo(SMALL_CLAIMS_WITNESS_LATE_WARNING);
        assertThat(result.getDate1()).isEqualTo(eightWeeks);
    }
}
