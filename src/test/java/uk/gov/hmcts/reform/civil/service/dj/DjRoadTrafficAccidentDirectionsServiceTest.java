package uk.gov.hmcts.reform.civil.service.dj;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.TrialRoadTrafficAccident;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.ROAD_TRAFFIC_ACCIDENT_UPLOAD_DJ;

@ExtendWith(MockitoExtension.class)
class DjRoadTrafficAccidentDirectionsServiceTest {

    @Mock
    private DjDeadlineService deadlineService;

    private DjRoadTrafficAccidentDirectionsService service;

    @BeforeEach
    void setUp() {
        service = new DjRoadTrafficAccidentDirectionsService(deadlineService);
        when(deadlineService.nextWorkingDayInWeeks(anyInt()))
            .thenAnswer(invocation -> LocalDate.of(2025, 9, 1)
                .plusWeeks(invocation.getArgument(0, Integer.class)));
    }

    @Test
    void shouldBuildRoadTrafficAccidentDirections() {
        TrialRoadTrafficAccident accident = service.buildTrialRoadTrafficAccident();

        assertThat(accident.getDate1()).isEqualTo(LocalDate.of(2025, 9, 1).plusWeeks(4));
        assertThat(accident.getInput()).isEqualTo(ROAD_TRAFFIC_ACCIDENT_UPLOAD_DJ);
    }
}
