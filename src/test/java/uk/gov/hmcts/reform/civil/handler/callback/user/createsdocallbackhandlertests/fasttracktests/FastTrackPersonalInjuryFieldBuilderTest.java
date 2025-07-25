package uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandlertests.fasttracktests;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.bankholidays.WorkingDayIndicator;
import uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.fasttrack.FastTrackPersonalInjuryFieldBuilder;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackPersonalInjury;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FastTrackPersonalInjuryFieldBuilderTest {

    @Mock
    private WorkingDayIndicator workingDayIndicator;

    @InjectMocks
    private FastTrackPersonalInjuryFieldBuilder fastTrackPersonalInjuryFieldBuilder;

    @Test
    void shouldBuildFastTrackPersonalInjuryFields() {
        LocalDate now = LocalDate.now();
        LocalDate date1 = now.plusWeeks(4);
        LocalDate date2 = now.plusWeeks(4);
        LocalDate date3 = now.plusWeeks(8);
        LocalDate date4 = now.plusWeeks(8);
        when(workingDayIndicator.getNextWorkingDay(date1)).thenReturn(date1);
        when(workingDayIndicator.getNextWorkingDay(date2)).thenReturn(date2);
        when(workingDayIndicator.getNextWorkingDay(date3)).thenReturn(date3);
        when(workingDayIndicator.getNextWorkingDay(date4)).thenReturn(date4);

        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = CaseData.builder();

        fastTrackPersonalInjuryFieldBuilder.build(caseDataBuilder);

        CaseData caseData = caseDataBuilder.build();
        FastTrackPersonalInjury personalInjury = caseData.getFastTrackPersonalInjury();
        assertThat(personalInjury).isNotNull();
        assertThat(personalInjury.getInput1()).isEqualTo("The claimant has permission to rely upon the written expert evidence already uploaded to the Digital Portal with the " +
                "particulars of claim and in addition has permission to rely upon any associated correspondence or updating report which is uploaded to the Digital Portal by 4pm" +
                " on");
        assertThat(personalInjury.getDate1()).isEqualTo(date1);
        assertThat(personalInjury.getInput2()).isEqualTo("Any questions which are to be addressed to an expert must be sent to the expert directly and uploaded to the Digital " +
                "Portal by 4pm on");
        assertThat(personalInjury.getDate2()).isEqualTo(date2);
        assertThat(personalInjury.getInput3()).isEqualTo("The answers to the questions shall be answered by the Expert by");
        assertThat(personalInjury.getDate3()).isEqualTo(date3);
        assertThat(personalInjury.getInput4()).isEqualTo("and uploaded to the Digital Portal by");
        assertThat(personalInjury.getDate4()).isEqualTo(date4);
    }
}