package uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.disposalhearing;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.bankholidays.WorkingDayIndicator;
import uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.SdoCaseFieldBuilder;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingSchedulesOfLoss;

import java.time.LocalDate;

@Slf4j
@Component
@RequiredArgsConstructor
public class SchedulesOfLossFieldBuilder implements SdoCaseFieldBuilder {

    private final WorkingDayIndicator workingDayIndicator;

    @Override
    public void build(CaseData.CaseDataBuilder<?, ?> updatedData) {
        log.info("Setting schedules of loss");
        updatedData.disposalHearingSchedulesOfLoss(DisposalHearingSchedulesOfLoss.builder()
                                                       .input2(
                                                           "If there is a claim for ongoing or future loss in the original schedule of losses, "
                                                               + "the claimant must upload to the Digital Portal an up-to-date schedule of loss by "
                                                               + "4pm on")
                                                       .date2(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusWeeks(
                                                           10)))
                                                       .input3(
                                                           "If the defendant wants to challenge this claim, they must send an up-to-date "
                                                               + "counter-schedule of loss to the claimant by 4pm on")
                                                       .date3(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusWeeks(
                                                           12)))
                                                       .input4(
                                                           "If the defendant want to challenge the sums claimed in the schedule of loss they "
                                                               + "must upload to the Digital Portal an updated counter schedule of loss by 4pm on")
                                                       .date4(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusWeeks(
                                                           12)))
                                                       .build());
    }
}
