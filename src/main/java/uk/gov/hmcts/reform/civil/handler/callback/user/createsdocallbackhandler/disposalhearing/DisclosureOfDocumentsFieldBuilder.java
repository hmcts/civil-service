package uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.disposalhearing;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.bankholidays.WorkingDayIndicator;
import uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.SdoCaseFieldBuilder;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingDisclosureOfDocuments;

import java.time.LocalDate;

@Slf4j
@Component
@RequiredArgsConstructor
public class DisclosureOfDocumentsFieldBuilder implements SdoCaseFieldBuilder {

    private final WorkingDayIndicator workingDayIndicator;

    @Override
    public void build(CaseData.CaseDataBuilder<?, ?> updatedData) {
        log.info("Setting disclosure of documents");
        updatedData.disposalHearingDisclosureOfDocuments(
                DisposalHearingDisclosureOfDocuments.builder()
                        .input1("The parties shall serve on each other copies of the documents upon "
                                + "which reliance is to be placed at the disposal hearing by 4pm on")
                        .date1(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusWeeks(10)))
                        .input2("The parties must upload to the Digital Portal copies of those documents "
                                + "which they wish the court to consider when deciding the amount of damages, "
                                + "by 4pm on")
                        .date2(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusWeeks(10)))
                        .build());
    }
}
