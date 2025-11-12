package uk.gov.hmcts.reform.civil.service.sdo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class SdoDisposalNarrativeServiceTest {

    @Mock
    private SdoDeadlineService deadlineService;

    private SdoDisposalNarrativeService service;

    @BeforeEach
    void setUp() {
        service = new SdoDisposalNarrativeService(deadlineService);
        lenient().when(deadlineService.nextWorkingDayFromNowWeeks(anyInt()))
            .thenAnswer(invocation -> LocalDate.of(2025, 4, 1)
                .plusWeeks(invocation.getArgument(0, Integer.class)));
        lenient().when(deadlineService.workingDaysFromNow(anyInt()))
            .thenAnswer(invocation -> LocalDate.of(2025, 5, 1)
                .plusDays(invocation.getArgument(0, Integer.class)));
    }

    @Test
    void shouldPopulateDisclosureAndWitnessSections() {
        CaseData.CaseDataBuilder<?, ?> builder = CaseData.builder();

        service.applyDisclosureOfDocuments(builder);
        service.applyWitnessOfFact(builder);

        CaseData result = builder.build();
        assertThat(result.getDisposalHearingDisclosureOfDocuments().getDate1())
            .isEqualTo(LocalDate.of(2025, 4, 1).plusWeeks(10));
        assertThat(result.getDisposalHearingWitnessOfFact().getDate2())
            .isEqualTo(LocalDate.of(2025, 4, 1).plusWeeks(4));
    }

    @Test
    void shouldPopulateOrderWithoutHearingAndNotes() {
        CaseData.CaseDataBuilder<?, ?> builder = CaseData.builder();

        service.applyOrderWithoutHearing(builder);
        service.applyNotes(builder);

        CaseData result = builder.build();
        assertThat(result.getDisposalOrderWithoutHearing().getInput())
            .contains("This order has been made without hearing.");
        assertThat(result.getDisposalHearingNotes().getDate())
            .isEqualTo(LocalDate.of(2025, 4, 1).plusWeeks(1));
    }

    @Test
    void shouldPopulateHearingDatesUsingCurrentClock() {
        CaseData.CaseDataBuilder<?, ?> builder = CaseData.builder();

        service.applyFinalDisposalHearing(builder);
        service.applyHearingTime(builder);

        LocalDate expected = LocalDate.now().plusWeeks(16);
        CaseData result = builder.build();
        assertThat(result.getDisposalHearingFinalDisposalHearing().getDate()).isEqualTo(expected);
        assertThat(result.getDisposalHearingHearingTime().getDateTo()).isEqualTo(expected);
    }
}
