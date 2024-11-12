package uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.disposalhearing;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.bankholidays.WorkingDayIndicator;
import uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.SdoCaseFieldBuilder;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingWitnessOfFact;

import java.time.LocalDate;

@Slf4j
@Component
@RequiredArgsConstructor
public class WitnessOfFactFieldBuilder implements SdoCaseFieldBuilder {

    private final WorkingDayIndicator workingDayIndicator;

    @Override
    public void build(CaseData.CaseDataBuilder<?, ?> updatedData) {
        log.info("Setting witness of fact");
        updatedData.disposalHearingWitnessOfFact(DisposalHearingWitnessOfFact.builder()
                .input3(
                        "The claimant must upload to the Digital Portal copies of the witness statements "
                                + "of all witnesses of fact on whose evidence reliance is to be placed by 4pm on")
                .date2(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusWeeks(
                        4)))
                .input4("The provisions of CPR 32.6 apply to such evidence.")
                .input5(
                        "Any application by the defendant in relation to CPR 32.7 must be made by 4pm on")
                .date3(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusWeeks(
                        6)))
                .input6(
                        "and must be accompanied by proposed directions for allocation and listing for "
                                + "trial on quantum. This is because cross-examination will cause the hearing to "
                                + "exceed the 30-minute maximum time estimate for a disposal hearing.")
                .build());
    }
}
