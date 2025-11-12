package uk.gov.hmcts.reform.civil.service.sdo;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackDisclosureOfDocuments;

import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.FAST_TRACK_DISCLOSURE_INSPECTION;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.FAST_TRACK_DISCLOSURE_REQUESTS_WITHIN_SEVEN_DAYS_SDO;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.FAST_TRACK_DISCLOSURE_STANDARD_SDO;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.FAST_TRACK_DISCLOSURE_UPLOAD_DEADLINE;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.FAST_TRACK_DISCLOSURE_UPLOAD_PREFIX;

/**
 * Encapsulates the standard disclosure paragraph so multiple stages can reuse the same
 * deadlines/wording without duplicating the 4+ date offsets.
 */
@Service
@RequiredArgsConstructor
public class SdoDisclosureOfDocumentsFieldsService {

    private final SdoDeadlineService sdoDeadlineService;

    public void populateFastTrackDisclosureOfDocuments(CaseData.CaseDataBuilder<?, ?> updatedData) {
        FastTrackDisclosureOfDocuments disclosure = FastTrackDisclosureOfDocuments.builder()
            .input1(FAST_TRACK_DISCLOSURE_STANDARD_SDO)
            .date1(sdoDeadlineService.nextWorkingDayFromNowWeeks(4))
            .input2(FAST_TRACK_DISCLOSURE_INSPECTION)
            .date2(sdoDeadlineService.nextWorkingDayFromNowWeeks(5))
            .input3(FAST_TRACK_DISCLOSURE_REQUESTS_WITHIN_SEVEN_DAYS_SDO)
            .input4(FAST_TRACK_DISCLOSURE_UPLOAD_PREFIX + " " + FAST_TRACK_DISCLOSURE_UPLOAD_DEADLINE)
            .date3(sdoDeadlineService.nextWorkingDayFromNowWeeks(8))
            .build();

        updatedData.fastTrackDisclosureOfDocuments(disclosure).build();
    }
}
