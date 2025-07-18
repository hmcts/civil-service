package uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandlertests.fasttracktests;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.bankholidays.WorkingDayIndicator;
import uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.fasttrack.FastTrackSchedulesOfLossFieldBuilder;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackSchedulesOfLoss;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FastTrackSchedulesOfLossFieldBuilderTest {

    @Mock
    private WorkingDayIndicator workingDayIndicator;

    @InjectMocks
    private FastTrackSchedulesOfLossFieldBuilder fastTrackSchedulesOfLossFieldBuilder;

    @Test
    void shouldBuildFastTrackSchedulesOfLossFields() {
        LocalDate now = LocalDate.now();
        LocalDate date1 = now.plusWeeks(10);
        LocalDate date2 = now.plusWeeks(12);
        when(workingDayIndicator.getNextWorkingDay(date1)).thenReturn(date1);
        when(workingDayIndicator.getNextWorkingDay(date2)).thenReturn(date2);

        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = CaseData.builder();

        fastTrackSchedulesOfLossFieldBuilder.build(caseDataBuilder);

        CaseData caseData = caseDataBuilder.build();
        FastTrackSchedulesOfLoss schedulesOfLoss = caseData.getFastTrackSchedulesOfLoss();
        assertThat(schedulesOfLoss).isNotNull();
        assertThat(schedulesOfLoss.getInput1()).isEqualTo("The claimant must upload to the Digital Portal an up-to-date schedule of loss by 4pm on");
        assertThat(schedulesOfLoss.getDate1()).isEqualTo(date1);
        assertThat(schedulesOfLoss.getInput2()).isEqualTo("If the defendant wants to challenge this claim, upload to the Digital Portal counter-schedule of loss by 4pm on");
        assertThat(schedulesOfLoss.getDate2()).isEqualTo(date2);
        assertThat(schedulesOfLoss.getInput3()).isEqualTo("If there is a claim for future pecuniary loss and the parties have not already set out their case on periodical " +
                "payments, they must do so in the respective schedule and counter-schedule.");
    }
}