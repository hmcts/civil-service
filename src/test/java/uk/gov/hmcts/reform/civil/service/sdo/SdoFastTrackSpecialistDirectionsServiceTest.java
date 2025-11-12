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
class SdoFastTrackSpecialistDirectionsServiceTest {

    @Mock
    private SdoDeadlineService deadlineService;

    private SdoFastTrackSpecialistDirectionsService service;

    @BeforeEach
    void setUp() {
        service = new SdoFastTrackSpecialistDirectionsService(deadlineService);
        lenient().when(deadlineService.nextWorkingDayFromNowWeeks(anyInt()))
            .thenAnswer(invocation -> LocalDate.now().plusWeeks(invocation.getArgument(0, Integer.class)));
    }

    @Test
    void shouldPopulateAllSpecialistSections() {
        CaseData.CaseDataBuilder<?, ?> builder = CaseData.builder();

        service.populateSpecialistDirections(builder);
        CaseData result = builder.build();

        assertThat(result.getFastTrackBuildingDispute()).isNotNull();
        assertThat(result.getFastTrackClinicalNegligence()).isNotNull();
        assertThat(result.getSdoR2FastTrackCreditHire()).isNotNull();
        assertThat(result.getFastTrackHousingDisrepair()).isNotNull();
        assertThat(result.getFastTrackPersonalInjury()).isNotNull();
        assertThat(result.getFastTrackRoadTrafficAccident()).isNotNull();
    }
}
