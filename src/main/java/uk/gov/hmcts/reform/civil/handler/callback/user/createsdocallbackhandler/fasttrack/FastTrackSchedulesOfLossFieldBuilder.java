package uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.fasttrack;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.bankholidays.WorkingDayIndicator;
import uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.SdoCaseFieldBuilder;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackSchedulesOfLoss;

import java.time.LocalDate;

@Slf4j
@Component
@RequiredArgsConstructor
public class FastTrackSchedulesOfLossFieldBuilder implements SdoCaseFieldBuilder {

    private final WorkingDayIndicator workingDayIndicator;

    @Override
    public void build(CaseData.CaseDataBuilder<?, ?> updatedData) {
        updatedData.fastTrackSchedulesOfLoss(FastTrackSchedulesOfLoss.builder()
                .input1(
                        "The claimant must upload to the Digital Portal an up-to-date schedule of loss by 4pm on")
                .date1(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusWeeks(
                        10)))
                .input2(
                        "If the defendant wants to challenge this claim, upload to the Digital Portal counter-schedule of loss by 4pm on")
                .date2(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusWeeks(
                        12)))
                .input3(
                        "If there is a claim for future pecuniary loss and the parties have not already set out their case on periodical payments," +
                                " they must do so in the respective schedule and counter-schedule.")
                .build());
    }
}
