package uk.gov.hmcts.reform.civil.service.dj;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.SdoDJR2TrialCreditHire;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.TrialBuildingDispute;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.TrialHousingDisrepair;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.TrialPersonalInjury;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.TrialRoadTrafficAccident;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DjSpecialistNarrativeServiceTest {

    @Mock
    private DjBuildingDisputeDirectionsService buildingDisputeDirectionsService;
    @Mock
    private DjClinicalDirectionsService clinicalDirectionsService;
    @Mock
    private DjRoadTrafficAccidentDirectionsService roadTrafficAccidentDirectionsService;
    @Mock
    private DjCreditHireDirectionsService creditHireDirectionsService;

    private DjSpecialistNarrativeService service;

    @BeforeEach
    void setUp() {
        service = new DjSpecialistNarrativeService(
            buildingDisputeDirectionsService,
            clinicalDirectionsService,
            roadTrafficAccidentDirectionsService,
            creditHireDirectionsService
        );
    }

    @Test
    void shouldBuildBuildingDisputeWithCalculatedDates() {
        TrialBuildingDispute expected = TrialBuildingDispute.builder()
            .date1(LocalDate.now())
            .build();
        when(buildingDisputeDirectionsService.buildTrialBuildingDispute()).thenReturn(expected);

        TrialBuildingDispute dispute = service.buildTrialBuildingDispute();

        assertThat(dispute).isSameAs(expected);
        verify(buildingDisputeDirectionsService).buildTrialBuildingDispute();
    }

    @Test
    void shouldBuildCreditHireDirectionsWithToggles() {
        SdoDJR2TrialCreditHire expected = SdoDJR2TrialCreditHire.builder().build();
        when(creditHireDirectionsService.buildCreditHireDirections()).thenReturn(expected);

        SdoDJR2TrialCreditHire creditHire = service.buildCreditHireDirections();

        assertThat(creditHire).isSameAs(expected);
        verify(creditHireDirectionsService).buildCreditHireDirections();
        verifyNoMoreInteractions(creditHireDirectionsService);
    }

    @Test
    void shouldBuildPersonalInjuryDirections() {
        TrialPersonalInjury expected = TrialPersonalInjury.builder().date2(LocalDate.now()).build();
        when(clinicalDirectionsService.buildTrialPersonalInjury()).thenReturn(expected);

        TrialPersonalInjury personalInjury = service.buildTrialPersonalInjury();

        assertThat(personalInjury).isSameAs(expected);
        verify(clinicalDirectionsService).buildTrialPersonalInjury();
    }

    @Test
    void shouldBuildRtaAndHousingDisrepairDirections() {
        TrialRoadTrafficAccident expectedRta = TrialRoadTrafficAccident.builder().build();
        TrialHousingDisrepair expectedHousing = TrialHousingDisrepair.builder().build();
        when(roadTrafficAccidentDirectionsService.buildTrialRoadTrafficAccident()).thenReturn(expectedRta);
        when(buildingDisputeDirectionsService.buildTrialHousingDisrepair()).thenReturn(expectedHousing);

        TrialRoadTrafficAccident rta = service.buildTrialRoadTrafficAccident();
        TrialHousingDisrepair housing = service.buildTrialHousingDisrepair();

        assertThat(rta).isSameAs(expectedRta);
        assertThat(housing).isSameAs(expectedHousing);
        verify(roadTrafficAccidentDirectionsService).buildTrialRoadTrafficAccident();
        verify(buildingDisputeDirectionsService).buildTrialHousingDisrepair();
    }
}
