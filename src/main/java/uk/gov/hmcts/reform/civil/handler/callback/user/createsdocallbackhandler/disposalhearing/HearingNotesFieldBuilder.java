package uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.disposalhearing;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.bankholidays.WorkingDayIndicator;
import uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.SdoCaseFieldBuilder;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingNotes;

import java.time.LocalDate;

@Slf4j
@Component
@RequiredArgsConstructor
public class HearingNotesFieldBuilder implements SdoCaseFieldBuilder {

    private final WorkingDayIndicator workingDayIndicator;

    @Override
    public void build(CaseData.CaseDataBuilder<?, ?> updatedData) {
        log.info("Setting hearing notes");
        updatedData.disposalHearingNotes(DisposalHearingNotes.builder()
                                             .input(
                                                 "This Order has been made without a hearing. Each party has the right to apply to "
                                                     + "have this Order set aside or varied. Any such application must be uploaded to the "
                                                     + "Digital Portal together with the appropriate fee, by 4pm on")
                                             .date(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusWeeks(1)))
                                             .build());
    }
}
