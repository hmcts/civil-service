package uk.gov.hmcts.reform.civil.service.sdo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackDisclosureOfDocuments;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.FAST_TRACK_DISCLOSURE_INSPECTION;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.FAST_TRACK_DISCLOSURE_REQUESTS_WITHIN_SEVEN_DAYS_SDO;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.FAST_TRACK_DISCLOSURE_STANDARD_SDO;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.FAST_TRACK_DISCLOSURE_UPLOAD_DEADLINE;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.FAST_TRACK_DISCLOSURE_UPLOAD_PREFIX;

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
        LocalDate date4Weeks = LocalDate.of(2024, 9, 1);
        LocalDate date5Weeks = LocalDate.of(2024, 9, 8);
        LocalDate date8Weeks = LocalDate.of(2024, 9, 29);
        when(deadlineService.nextWorkingDayFromNowWeeks(4)).thenReturn(date4Weeks);
        when(deadlineService.nextWorkingDayFromNowWeeks(5)).thenReturn(date5Weeks);
        when(deadlineService.nextWorkingDayFromNowWeeks(8)).thenReturn(date8Weeks);

        CaseData caseData = CaseData.builder().build();

        service.populateFastTrackDisclosureOfDocuments(caseData);

        FastTrackDisclosureOfDocuments disclosure = caseData.getFastTrackDisclosureOfDocuments();
        assertThat(disclosure).isNotNull();
        assertThat(disclosure.getDate1()).isEqualTo(date4Weeks);
        assertThat(disclosure.getDate2()).isEqualTo(date5Weeks);
        assertThat(disclosure.getDate3()).isEqualTo(date8Weeks);
        assertThat(disclosure.getInput1()).isEqualTo(FAST_TRACK_DISCLOSURE_STANDARD_SDO);
        assertThat(disclosure.getInput2()).isEqualTo(FAST_TRACK_DISCLOSURE_INSPECTION);
        assertThat(disclosure.getInput3()).isEqualTo(FAST_TRACK_DISCLOSURE_REQUESTS_WITHIN_SEVEN_DAYS_SDO);
        assertThat(disclosure.getInput4())
            .isEqualTo(FAST_TRACK_DISCLOSURE_UPLOAD_PREFIX + " " + FAST_TRACK_DISCLOSURE_UPLOAD_DEADLINE);
    }
}
