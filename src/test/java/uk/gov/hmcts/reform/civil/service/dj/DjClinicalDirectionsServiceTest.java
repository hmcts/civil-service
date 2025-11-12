package uk.gov.hmcts.reform.civil.service.dj;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.TrialPersonalInjury;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DjClinicalDirectionsServiceTest {

    @Mock
    private DjDeadlineService deadlineService;

    private DjClinicalDirectionsService service;

    @BeforeEach
    void setUp() {
        service = new DjClinicalDirectionsService(deadlineService);
        lenient().when(deadlineService.nextWorkingDayInWeeks(anyInt()))
            .thenAnswer(invocation -> LocalDate.of(2025, 8, 1)
                .plusWeeks(invocation.getArgument(0, Integer.class)));
    }

    @Test
    void shouldBuildClinicalNegligenceInstructionsWithoutDates() {
        assertThat(service.buildTrialClinicalNegligence().getInput1())
            .contains("Documents should be retained");
    }

    @Test
    void shouldBuildPersonalInjuryWithExpectedDates() {
        TrialPersonalInjury injury = service.buildTrialPersonalInjury();

        assertThat(injury.getDate1()).isEqualTo(LocalDate.of(2025, 8, 1).plusWeeks(4));
        assertThat(injury.getDate2()).isEqualTo(LocalDate.of(2025, 8, 1).plusWeeks(8));
    }
}
