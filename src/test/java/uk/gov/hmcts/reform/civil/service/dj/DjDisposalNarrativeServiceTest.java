package uk.gov.hmcts.reform.civil.service.dj;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class DjDisposalNarrativeServiceTest {

    private static final String JUDGE = "Judge Smith";

    @Mock
    private DjDeadlineService deadlineService;

    private DjDisposalNarrativeService service;

    @BeforeEach
    void setUp() {
        service = new DjDisposalNarrativeService(deadlineService);
        lenient().when(deadlineService.nextWorkingDayInWeeks(anyInt()))
            .thenAnswer(invocation -> LocalDate.of(2025, 1, 1)
                .plusWeeks(invocation.getArgument(0, Integer.class)));
        lenient().when(deadlineService.weeksFromNow(anyInt()))
            .thenAnswer(invocation -> LocalDate.now().plusWeeks(invocation.getArgument(0, Integer.class)));
    }

    @Test
    void shouldBuildJudgesRecitalWithName() {
        var recital = service.buildJudgesRecital(JUDGE);

        assertThat(recital.getJudgeNameTitle()).isEqualTo(JUDGE);
        assertThat(recital.getInput()).isEqualTo(JUDGE + ",");
    }

    @Test
    void shouldBuildDisclosureWithFourWeekDeadline() {
        var disclosure = service.buildDisclosureOfDocuments();

        assertThat(disclosure.getInput()).contains("The parties shall serve on each other");
        assertThat(disclosure.getDate()).isEqualTo(LocalDate.of(2025, 1, 1).plusWeeks(4));
    }

    @Test
    void shouldBuildSchedulesOfLossWithSequentialDeadlines() {
        var schedules = service.buildSchedulesOfLoss();

        assertThat(schedules.getDate1()).isEqualTo(LocalDate.of(2025, 1, 1).plusWeeks(10));
        assertThat(schedules.getDate2()).isEqualTo(LocalDate.of(2025, 1, 1).plusWeeks(12));
        assertThat(schedules.getInputText4()).contains("future pecuniary loss");
    }
}
