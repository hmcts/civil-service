package uk.gov.hmcts.reform.civil.service.dj;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.sdo.AddOrRemoveToggle;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.SdoDJR2TrialCreditHire;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.TrialBuildingDispute;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.TrialHousingDisrepair;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.TrialPersonalInjury;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.TrialRoadTrafficAccident;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DjSpecialistNarrativeServiceTest {

    @Mock
    private DjSpecialistDeadlineService deadlineService;

    private DjSpecialistNarrativeService service;

    @BeforeEach
    void setUp() {
        service = new DjSpecialistNarrativeService(deadlineService);
        when(deadlineService.nextWorkingDayInWeeks(anyInt()))
            .thenAnswer(invocation -> LocalDate.of(2025, 1, 1)
                .plusWeeks(invocation.getArgument(0, Integer.class)));
    }

    @Test
    void shouldBuildBuildingDisputeWithCalculatedDates() {
        TrialBuildingDispute dispute = service.buildTrialBuildingDispute();

        assertThat(dispute.getDate1()).isEqualTo(LocalDate.of(2025, 1, 1).plusWeeks(10));
        assertThat(dispute.getDate2()).isEqualTo(LocalDate.of(2025, 1, 1).plusWeeks(12));
        assertThat(dispute.getInput1()).contains("Scott Schedule");
        verify(deadlineService).nextWorkingDayInWeeks(10);
        verify(deadlineService).nextWorkingDayInWeeks(12);
    }

    @Test
    void shouldBuildCreditHireDirectionsWithToggles() {
        SdoDJR2TrialCreditHire creditHire = service.buildCreditHireDirections();

        assertThat(creditHire.getDetailsShowToggle()).isEqualTo(List.of(AddOrRemoveToggle.ADD));
        assertThat(creditHire.getDate3()).isEqualTo(LocalDate.of(2025, 1, 1).plusWeeks(12));
        assertThat(creditHire.getDate4()).isEqualTo(LocalDate.of(2025, 1, 1).plusWeeks(14));
        assertThat(creditHire.getSdoDJR2TrialCreditHireDetails().getDate1())
            .isEqualTo(LocalDate.of(2025, 1, 1).plusWeeks(8));
    }

    @Test
    void shouldBuildPersonalInjuryDirections() {
        TrialPersonalInjury personalInjury = service.buildTrialPersonalInjury();

        assertThat(personalInjury.getDate2()).isEqualTo(LocalDate.of(2025, 1, 1).plusWeeks(8));
        assertThat(personalInjury.getInput1()).contains("expert evidence");
    }

    @Test
    void shouldBuildRtaAndHousingDisrepairDirections() {
        TrialRoadTrafficAccident rta = service.buildTrialRoadTrafficAccident();
        TrialHousingDisrepair housing = service.buildTrialHousingDisrepair();

        assertThat(rta.getDate1()).isEqualTo(LocalDate.of(2025, 1, 1).plusWeeks(4));
        assertThat(housing.getDate2()).isEqualTo(LocalDate.of(2025, 1, 1).plusWeeks(12));
    }
}
