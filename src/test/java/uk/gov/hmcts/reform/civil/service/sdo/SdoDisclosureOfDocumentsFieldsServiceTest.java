package uk.gov.hmcts.reform.civil.service.sdo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackDisclosureOfDocuments;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.FAST_TRACK_DISCLOSURE_INSPECTION;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.FAST_TRACK_DISCLOSURE_REQUESTS_WITHIN_SEVEN_DAYS_SDO;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.FAST_TRACK_DISCLOSURE_STANDARD_SDO;

@ExtendWith(MockitoExtension.class)
class SdoDisclosureOfDocumentsFieldsServiceTest {

    @Mock
    private SdoDeadlineService deadlineService;

    private SdoDisclosureOfDocumentsFieldsService service;

    @BeforeEach
    void setUp() {
        service = new SdoDisclosureOfDocumentsFieldsService(deadlineService);
    }

    @Test
    void shouldPopulateDisclosureFieldsWithExpectedDates() {
        LocalDate date2Weeks = LocalDate.of(2024, 9, 1);
        LocalDate date3Weeks = LocalDate.of(2024, 9, 8);
        when(deadlineService.nextWorkingDayFromNowWeeks(2)).thenReturn(date2Weeks);
        when(deadlineService.nextWorkingDayFromNowWeeks(3)).thenReturn(date3Weeks);

        CaseData caseData = CaseDataBuilder.builder().build();

        service.populateFastTrackDisclosureOfDocuments(caseData);

        FastTrackDisclosureOfDocuments disclosure = caseData.getFastTrackDisclosureOfDocuments();
        assertThat(disclosure).isNotNull();
        assertThat(disclosure.getDate1()).isEqualTo(date2Weeks);
        assertThat(disclosure.getDate2()).isEqualTo(date3Weeks);
        assertThat(disclosure.getInput1()).isEqualTo(FAST_TRACK_DISCLOSURE_STANDARD_SDO);
        assertThat(disclosure.getInput2()).isEqualTo(FAST_TRACK_DISCLOSURE_INSPECTION);
        assertThat(disclosure.getInput3()).isEqualTo(FAST_TRACK_DISCLOSURE_REQUESTS_WITHIN_SEVEN_DAYS_SDO);
    }
}
