package uk.gov.hmcts.reform.civil.service.sdo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.lenient;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.FAST_TRACK_DISCLOSURE_STANDARD_SDO;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.JUDGES_RECITAL_STATEMENTS_OF_CASE_WITH_COMMA;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.ORDER_WITHOUT_HEARING_RECEIVED_BY_COURT_NO_ARTICLE;

@ExtendWith(MockitoExtension.class)
class SdoFastTrackNarrativeServiceTest {

    @Mock
    private SdoDeadlineService deadlineService;

    private SdoFastTrackNarrativeService service;

    @BeforeEach
    void setUp() {
        service = new SdoFastTrackNarrativeService(deadlineService);
        lenient().when(deadlineService.nextWorkingDayFromNowWeeks(anyInt()))
            .thenAnswer(invocation -> LocalDate.of(2025, 2, 1)
                .plusWeeks(invocation.getArgument(0, Integer.class)));
        lenient().when(deadlineService.calendarDaysFromNow(anyInt()))
            .thenAnswer(invocation -> LocalDate.of(2025, 1, 1)
                .plusDays(invocation.getArgument(0, Integer.class)));
        lenient().when(deadlineService.orderSetAsideOrVariedApplicationDeadline(any(LocalDateTime.class)))
            .thenReturn(LocalDate.of(2025, 6, 15));
    }

    @Test
    void shouldPopulateCoreFastTrackNarrative() {
        CaseData.CaseDataBuilder<?, ?> builder = CaseData.builder();

        service.populateFastTrackNarrative(builder);

        LocalDate calendarBase = LocalDate.of(2025, 1, 1);
        LocalDate workingDayBase = LocalDate.of(2025, 2, 1);
        CaseData result = builder.build();
        assertThat(result.getFastTrackJudgesRecital().getInput())
            .isEqualTo(JUDGES_RECITAL_STATEMENTS_OF_CASE_WITH_COMMA);
        assertThat(result.getFastTrackDisclosureOfDocuments().getInput1())
            .isEqualTo(FAST_TRACK_DISCLOSURE_STANDARD_SDO);
        assertThat(result.getFastTrackDisclosureOfDocuments().getDate1())
            .isEqualTo(workingDayBase.plusWeeks(4));
        assertThat(result.getSdoR2FastTrackWitnessOfFact().getSdoWitnessDeadlineDate())
            .isEqualTo(calendarBase.plusDays(70));
        assertThat(result.getFastTrackSchedulesOfLoss().getDate1())
            .isEqualTo(workingDayBase.plusWeeks(10));
        assertThat(result.getFastTrackSchedulesOfLoss().getDate2())
            .isEqualTo(workingDayBase.plusWeeks(12));
        assertThat(result.getFastTrackTrial().getDate1())
            .isEqualTo(calendarBase.plusDays(22 * 7));
        assertThat(result.getFastTrackTrial().getDate2())
            .isEqualTo(calendarBase.plusDays(30 * 7));
        assertThat(result.getFastTrackHearingTime().getDateFrom())
            .isEqualTo(calendarBase.plusDays(22 * 7));
        assertThat(result.getFastTrackHearingTime().getDateTo())
            .isEqualTo(calendarBase.plusDays(30 * 7));
        assertThat(result.getFastTrackNotes().getDate())
            .isEqualTo(workingDayBase.plusWeeks(1));
    }

    @Test
    void shouldPopulateOrderWithoutHearingUsingDeadlineService() {
        CaseData.CaseDataBuilder<?, ?> builder = CaseData.builder();

        service.populateFastTrackNarrative(builder);

        String input = builder.build().getFastTrackOrderWithoutJudgement().getInput();
        assertThat(input).startsWith(ORDER_WITHOUT_HEARING_RECEIVED_BY_COURT_NO_ARTICLE);
        assertThat(input).contains("15 June 2025");
    }
}
