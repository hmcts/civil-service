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

@ExtendWith(MockitoExtension.class)
class SdoFastTrackOrderDefaultsServiceTest {

    @Mock
    private SdoDeadlineService deadlineService;

    private SdoFastTrackOrderDefaultsService service;
    private SdoFastTrackSpecialistDirectionsService specialistDirectionsService;

    @BeforeEach
    void setUp() {
        specialistDirectionsService = new SdoFastTrackSpecialistDirectionsService(deadlineService);
        service = new SdoFastTrackOrderDefaultsService(deadlineService, specialistDirectionsService);
        lenient().when(deadlineService.nextWorkingDayFromNowWeeks(anyInt()))
            .thenAnswer(invocation -> LocalDate.of(2025, 3, 1).plusWeeks(invocation.getArgument(0, Integer.class)));
        lenient().when(deadlineService.nextWorkingDayFromNowDays(anyInt()))
            .thenAnswer(invocation -> LocalDate.of(2025, 4, 1).plusDays(invocation.getArgument(0, Integer.class)));
        lenient().when(deadlineService.orderSetAsideOrVariedApplicationDeadline(any(LocalDateTime.class)))
            .thenReturn(LocalDate.of(2025, 5, 1));
    }

    @Test
    void shouldPopulateFastTrackDefaults() {
        CaseData.CaseDataBuilder<?, ?> builder = CaseData.builder();

        service.populateFastTrackOrderDetails(builder);
        CaseData result = builder.build();

        assertThat(result.getFastTrackJudgesRecital()).isNotNull();
        assertThat(result.getFastTrackTrial()).isNotNull();
        assertThat(result.getSdoR2FastTrackWitnessOfFact()).isNotNull();
    }
}
