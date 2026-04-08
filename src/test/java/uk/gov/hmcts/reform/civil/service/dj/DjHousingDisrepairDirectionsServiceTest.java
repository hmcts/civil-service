package uk.gov.hmcts.reform.civil.service.dj;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.TrialHousingDisrepair;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.lenient;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.HOUSING_DISREPAIR_CLAUSE_A;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.HOUSING_SCHEDULE_CLAIMANT_INSTRUCTION;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.HOUSING_SCHEDULE_COLUMNS_DJ;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.HOUSING_SCHEDULE_DEFENDANT_INSTRUCTION;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.HOUSING_SCHEDULE_INTRO_DJ;

@ExtendWith(MockitoExtension.class)
class DjHousingDisrepairDirectionsServiceTest {

    @Mock
    private DjDeadlineService deadlineService;

    private DjHousingDisrepairDirectionsService service;

    @BeforeEach
    void setUp() {
        service = new DjHousingDisrepairDirectionsService(deadlineService);
        lenient().when(deadlineService.nextWorkingDayInWeeks(anyInt()))
            .thenAnswer(invocation -> LocalDate.of(2025, 6, 1)
                .plusWeeks(invocation.getArgument(0, Integer.class)));
    }

    @Test
    void shouldBuildTrialHousingDisrepair() {
        TrialHousingDisrepair housing = service.buildTrialHousingDisrepair();

        assertThat(housing.getDate1()).isEqualTo(LocalDate.of(2025, 6, 1).plusWeeks(10));
        assertThat(housing.getDate2()).isEqualTo(LocalDate.of(2025, 6, 1).plusWeeks(12));
        assertThat(housing.getInput1()).isEqualTo(HOUSING_SCHEDULE_INTRO_DJ);
        assertThat(housing.getInput2()).isEqualTo(HOUSING_SCHEDULE_COLUMNS_DJ);
        assertThat(housing.getInput3()).isEqualTo(HOUSING_SCHEDULE_CLAIMANT_INSTRUCTION);
        assertThat(housing.getInput4()).isEqualTo(HOUSING_SCHEDULE_DEFENDANT_INSTRUCTION);
    }

    @Test
    void shouldBuildTrialHousingDisrepairOtherRemedy() {
        TrialHousingDisrepair housing = service.buildTrialHousingDisrepairOtherRemedy();

        assertThat(housing.getClauseA()).isEqualTo(HOUSING_DISREPAIR_CLAUSE_A);
        assertThat(housing.getFirstReportDateBy()).isEqualTo(LocalDate.of(2025, 6, 1).plusWeeks(4));
        assertThat(housing.getJointStatementDateBy()).isEqualTo(LocalDate.of(2025, 6, 1).plusWeeks(8));
        assertThat(housing.getInput1()).isNull();
    }
}

