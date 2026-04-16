package uk.gov.hmcts.reform.civil.service.dj;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.TrialBuildingDispute;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.BUILDING_SCHEDULE_CLAIMANT_INSTRUCTION;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.BUILDING_SCHEDULE_COLUMNS_DJ;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.BUILDING_SCHEDULE_DEFENDANT_INSTRUCTION;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.BUILDING_SCHEDULE_INTRO_DJ;

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
        assertThat(dispute.getInput1()).isEqualTo(BUILDING_SCHEDULE_INTRO_DJ);
        assertThat(dispute.getInput2()).isEqualTo(BUILDING_SCHEDULE_COLUMNS_DJ);
        assertThat(dispute.getInput3()).isEqualTo(BUILDING_SCHEDULE_CLAIMANT_INSTRUCTION);
        assertThat(dispute.getInput4()).isEqualTo(BUILDING_SCHEDULE_DEFENDANT_INSTRUCTION);
    }
}
