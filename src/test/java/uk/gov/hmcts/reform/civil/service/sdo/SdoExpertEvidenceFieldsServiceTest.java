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
import static org.mockito.Mockito.when;

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
        when(deadlineService.nextWorkingDayFromNowDays(14)).thenReturn(date14);
        when(deadlineService.nextWorkingDayFromNowDays(42)).thenReturn(date42);
        when(deadlineService.nextWorkingDayFromNowDays(49)).thenReturn(date49);

        CaseData.CaseDataBuilder<?, ?> builder = CaseData.builder().build().toBuilder();

        service.populateFastTrackExpertEvidence(builder);

        FastTrackPersonalInjury personalInjury = builder.build().getFastTrackPersonalInjury();
        assertThat(personalInjury).isNotNull();
        assertThat(personalInjury.getDate1()).isNull();
        assertThat(personalInjury.getDate2()).isEqualTo(date14);
        assertThat(personalInjury.getDate3()).isEqualTo(date42);
        assertThat(personalInjury.getDate4()).isEqualTo(date49);
        assertThat(personalInjury.getInput1()).isEqualTo(
            "The Claimant has permission to rely upon the written expert evidence already uploaded to the"
                + " Digital Portal with the particulars of claim"
        );
        assertThat(personalInjury.getInput2()).isEqualTo(
            "The Defendant(s) may ask questions of the Claimant's expert which must be sent to the expert "
                + "directly and uploaded to the Digital Portal by 4pm on"
        );
        assertThat(personalInjury.getInput3()).isEqualTo("The answers to the questions shall be answered by the Expert by");
        assertThat(personalInjury.getInput4()).isEqualTo("and uploaded to the Digital Portal by the party who has asked the question by");
    }
}
