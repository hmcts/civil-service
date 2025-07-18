package uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.fasttrack;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.bankholidays.WorkingDayIndicator;
import uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.SdoCaseFieldBuilder;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackDisclosureOfDocuments;

import java.time.LocalDate;

@Slf4j
@Component
@RequiredArgsConstructor
public class FastTrackDisclosureOfDocumentsFieldBuilder implements SdoCaseFieldBuilder {

    private final WorkingDayIndicator workingDayIndicator;

    @Override
    public void build(CaseData.CaseDataBuilder<?, ?> updatedData) {
        updatedData.fastTrackDisclosureOfDocuments(FastTrackDisclosureOfDocuments.builder()
                .input1(
                        "Standard disclosure shall be provided by the parties by uploading to the Digital Portal their list of documents by 4pm on")
                .date1(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusWeeks(
                        4)))
                .input2(
                        "Any request to inspect a document, or for a copy of a document, shall be made directly to the other party by 4pm on")
                .date2(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusWeeks(
                        6)))
                .input3(
                        "Requests will be complied with within 7 days of the receipt of the request.")
                .input4(
                        "Each party must upload to the Digital Portal copies of those documents on which they wish to rely at trial by 4pm on")
                .date3(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusWeeks(
                        8)))
                .build());
    }
}
