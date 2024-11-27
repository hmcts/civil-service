package uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandlertests.disposalhearingtests;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.bankholidays.WorkingDayIndicator;
import uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.disposalhearing.WitnessOfFactFieldBuilder;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingWitnessOfFact;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WitnessOfFactFieldBuilderTest {

    @Mock
    private WorkingDayIndicator workingDayIndicator;

    @InjectMocks
    private WitnessOfFactFieldBuilder witnessOfFactFieldBuilder;

    @Test
    void shouldSetWitnessOfFact() {
        LocalDate date2 = LocalDate.now().plusWeeks(4).plusDays(1);
        LocalDate date3 = LocalDate.now().plusWeeks(6).plusDays(1);

        when(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusWeeks(4))).thenReturn(date2);
        when(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusWeeks(6))).thenReturn(date3);

        CaseData.CaseDataBuilder<?, ?> updatedData = CaseData.builder();

        witnessOfFactFieldBuilder.build(updatedData);

        CaseData result = updatedData.build();
        DisposalHearingWitnessOfFact witnessOfFact = result.getDisposalHearingWitnessOfFact();
        assertThat(witnessOfFact).isNotNull();
        assertThat(witnessOfFact.getInput3())
                .isEqualTo("The claimant must upload to the Digital Portal copies of the witness statements "
                        + "of all witnesses of fact on whose evidence reliance is to be placed by 4pm on");
        assertThat(witnessOfFact.getDate2()).isEqualTo(date2);
        assertThat(witnessOfFact.getInput4()).isEqualTo("The provisions of CPR 32.6 apply to such evidence.");
        assertThat(witnessOfFact.getInput5())
                .isEqualTo("Any application by the defendant in relation to CPR 32.7 must be made by 4pm on");
        assertThat(witnessOfFact.getDate3()).isEqualTo(date3);
        assertThat(witnessOfFact.getInput6())
                .isEqualTo("and must be accompanied by proposed directions for allocation and listing for "
                        + "trial on quantum. This is because cross-examination will cause the hearing to "
                        + "exceed the 30-minute maximum time estimate for a disposal hearing.");
    }
}