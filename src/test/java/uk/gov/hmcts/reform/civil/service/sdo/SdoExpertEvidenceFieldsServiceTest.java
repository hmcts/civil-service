package uk.gov.hmcts.reform.civil.service.sdo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackPersonalInjury;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.PERSONAL_INJURY_ANSWERS;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.PERSONAL_INJURY_DEFENDANT_QUESTIONS;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.PERSONAL_INJURY_PERMISSION_SDO;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.PERSONAL_INJURY_UPLOAD_BY_ASKING_PARTY;

@ExtendWith(MockitoExtension.class)
class SdoExpertEvidenceFieldsServiceTest {

    @Mock
    private SdoDeadlineService deadlineService;

    private SdoExpertEvidenceFieldsService service;

    @BeforeEach
    void setUp() {
        service = new SdoExpertEvidenceFieldsService(deadlineService);
    }

    @Test
    void shouldPopulateExpertEvidenceFieldsWithExpectedDates() {
        LocalDate date14 = LocalDate.of(2024, 10, 1);
        LocalDate date42 = LocalDate.of(2024, 11, 1);
        LocalDate date49 = LocalDate.of(2024, 11, 8);
        when(deadlineService.nextWorkingDayFromNowDays(eq(14))).thenReturn(date14);
        when(deadlineService.nextWorkingDayFromNowDays(eq(42))).thenReturn(date42);
        when(deadlineService.nextWorkingDayFromNowDays(eq(49))).thenReturn(date49);

        CaseData.CaseDataBuilder<?, ?> builder = CaseData.builder().build().toBuilder();

        service.populateFastTrackExpertEvidence(builder);

        FastTrackPersonalInjury personalInjury = builder.build().getFastTrackPersonalInjury();
        assertThat(personalInjury).isNotNull();
        assertThat(personalInjury.getDate2()).isEqualTo(date14);
        assertThat(personalInjury.getDate3()).isEqualTo(date42);
        assertThat(personalInjury.getDate4()).isEqualTo(date49);
        assertThat(personalInjury.getInput1()).isEqualTo(PERSONAL_INJURY_PERMISSION_SDO);
        assertThat(personalInjury.getInput2()).isEqualTo(PERSONAL_INJURY_DEFENDANT_QUESTIONS);
        assertThat(personalInjury.getInput3()).isEqualTo(PERSONAL_INJURY_ANSWERS);
        assertThat(personalInjury.getInput4()).isEqualTo(PERSONAL_INJURY_UPLOAD_BY_ASKING_PARTY);
    }
}
