package uk.gov.hmcts.reform.civil.service.sdo;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackDisclosureOfDocuments;

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
            .input1("Standard disclosure shall be provided by the parties by uploading to the Digital Portal their "
                        + "list of documents by 4pm on")
            .date1(sdoDeadlineService.nextWorkingDayFromNowWeeks(4))
            .input2("Any request to inspect a document, or for a copy of a document, shall be made directly to "
                        + "the other party by 4pm on")
            .date2(sdoDeadlineService.nextWorkingDayFromNowWeeks(5))
            .input3("Requests will be complied with within 7 days of the receipt of the request.")
            .input4("Each party must upload to the Digital Portal copies of those documents on which they wish to"
                        + " rely at trial by 4pm on")
            .date3(sdoDeadlineService.nextWorkingDayFromNowWeeks(8))
            .build();

        updatedData.fastTrackDisclosureOfDocuments(disclosure).build();
    }
}
