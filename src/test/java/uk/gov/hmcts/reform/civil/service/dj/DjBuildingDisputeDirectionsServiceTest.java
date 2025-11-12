package uk.gov.hmcts.reform.civil.service.dj;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.TrialBuildingDispute;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.TrialHousingDisrepair;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DjBuildingDisputeDirectionsServiceTest {

    @Mock
    private DjDeadlineService deadlineService;

    private DjBuildingDisputeDirectionsService service;

    @BeforeEach
    void setUp() {
        service = new DjBuildingDisputeDirectionsService(deadlineService);
        when(deadlineService.nextWorkingDayInWeeks(anyInt()))
            .thenAnswer(invocation -> LocalDate.of(2025, 6, 1)
                .plusWeeks(invocation.getArgument(0, Integer.class)));
    }

    @Test
    void shouldBuildTrialBuildingDispute() {
        TrialBuildingDispute dispute = service.buildTrialBuildingDispute();

        assertThat(dispute.getDate1()).isEqualTo(LocalDate.of(2025, 6, 1).plusWeeks(10));
        assertThat(dispute.getDate2()).isEqualTo(LocalDate.of(2025, 6, 1).plusWeeks(12));
        assertThat(dispute.getInput1()).contains("Scott Schedule");
    }

    @Test
    void shouldBuildTrialHousingDisrepair() {
        TrialHousingDisrepair housing = service.buildTrialHousingDisrepair();

        assertThat(housing.getDate1()).isEqualTo(LocalDate.of(2025, 6, 1).plusWeeks(10));
        assertThat(housing.getDate2()).isEqualTo(LocalDate.of(2025, 6, 1).plusWeeks(12));
    }
}
