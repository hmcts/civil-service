package uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandlertests.disposalhearingtests;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.bankholidays.WorkingDayIndicator;
import uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.disposalhearing.SchedulesOfLossFieldBuilder;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingSchedulesOfLoss;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SchedulesOfLossFieldBuilderTest {

    @Mock
    private WorkingDayIndicator workingDayIndicator;

    @InjectMocks
    private SchedulesOfLossFieldBuilder schedulesOfLossFieldBuilder;

    @Test
    void shouldSetSchedulesOfLoss() {
        LocalDate date2 = LocalDate.now().plusWeeks(10).plusDays(1);
        LocalDate date3 = LocalDate.now().plusWeeks(12).plusDays(1);
        LocalDate date4 = LocalDate.now().plusWeeks(12).plusDays(1);

        when(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusWeeks(10))).thenReturn(date2);
        when(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusWeeks(12))).thenReturn(date3);
        when(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusWeeks(12))).thenReturn(date4);

        CaseData.CaseDataBuilder<?, ?> updatedData = CaseData.builder();

        schedulesOfLossFieldBuilder.build(updatedData);

        CaseData result = updatedData.build();
        DisposalHearingSchedulesOfLoss schedulesOfLoss = result.getDisposalHearingSchedulesOfLoss();
        assertThat(schedulesOfLoss).isNotNull();
        assertThat(schedulesOfLoss.getInput2())
                .isEqualTo("If there is a claim for ongoing or future loss in the original schedule of losses, "
                        + "the claimant must upload to the Digital Portal an up-to-date schedule of loss by 4pm on");
        assertThat(schedulesOfLoss.getDate2()).isEqualTo(date2);
        assertThat(schedulesOfLoss.getInput3())
                .isEqualTo("If the defendant wants to challenge this claim, they must send an up-to-date "
                        + "counter-schedule of loss to the claimant by 4pm on");
        assertThat(schedulesOfLoss.getDate3()).isEqualTo(date3);
        assertThat(schedulesOfLoss.getInput4())
                .isEqualTo("If the defendant want to challenge the sums claimed in the schedule of loss they "
                        + "must upload to the Digital Portal an updated counter schedule of loss by 4pm on");
        assertThat(schedulesOfLoss.getDate4()).isEqualTo(date4);
    }
}