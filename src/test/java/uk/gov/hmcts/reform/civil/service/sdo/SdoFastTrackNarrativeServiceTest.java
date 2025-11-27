package uk.gov.hmcts.reform.civil.service.sdo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackDisclosureOfDocuments;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackHearingTime;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackSchedulesOfLoss;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackTrial;

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
        CaseData caseData = CaseData.builder().build();

        service.populateFastTrackNarrative(caseData);

        LocalDate calendarBase = LocalDate.of(2025, 1, 1);
        LocalDate workingDayBase = LocalDate.of(2025, 2, 1);
        assertThat(caseData.getFastTrackJudgesRecital().getInput())
            .isEqualTo(JUDGES_RECITAL_STATEMENTS_OF_CASE_WITH_COMMA);
        assertThat(caseData.getFastTrackDisclosureOfDocuments())
            .extracting(FastTrackDisclosureOfDocuments::getInput1, FastTrackDisclosureOfDocuments::getDate1)
            .containsExactly(FAST_TRACK_DISCLOSURE_STANDARD_SDO, workingDayBase.plusWeeks(4));
        assertThat(caseData.getSdoR2FastTrackWitnessOfFact().getSdoWitnessDeadlineDate())
            .isEqualTo(calendarBase.plusDays(70));
        assertThat(caseData.getFastTrackSchedulesOfLoss())
            .extracting(
                FastTrackSchedulesOfLoss::getDate1,
                FastTrackSchedulesOfLoss::getDate2
            )
            .containsExactly(workingDayBase.plusWeeks(10), workingDayBase.plusWeeks(12));
        assertThat(caseData.getFastTrackTrial())
            .extracting(FastTrackTrial::getDate1, FastTrackTrial::getDate2)
            .containsExactly(calendarBase.plusDays(22 * 7), calendarBase.plusDays(30 * 7));
        assertThat(caseData.getFastTrackHearingTime())
            .extracting(FastTrackHearingTime::getDateFrom, FastTrackHearingTime::getDateTo)
            .containsExactly(calendarBase.plusDays(22 * 7), calendarBase.plusDays(30 * 7));
        assertThat(caseData.getFastTrackNotes().getDate())
            .isEqualTo(workingDayBase.plusWeeks(1));
    }

    @Test
    void shouldPopulateOrderWithoutHearingUsingDeadlineService() {
        CaseData caseData = CaseData.builder().build();

        service.populateFastTrackNarrative(caseData);

        String input = caseData.getFastTrackOrderWithoutJudgement().getInput();
        assertThat(input).startsWith(ORDER_WITHOUT_HEARING_RECEIVED_BY_COURT_NO_ARTICLE);
        assertThat(input).contains("15 June 2025");
    }
}
