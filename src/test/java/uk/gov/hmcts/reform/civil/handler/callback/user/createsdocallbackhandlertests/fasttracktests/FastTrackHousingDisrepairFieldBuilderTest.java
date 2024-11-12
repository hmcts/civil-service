package uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandlertests.fasttracktests;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.bankholidays.WorkingDayIndicator;
import uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.fasttrack.FastTrackHousingDisrepairFieldBuilder;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackHousingDisrepair;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FastTrackHousingDisrepairFieldBuilderTest {

    @Mock
    private WorkingDayIndicator workingDayIndicator;

    @InjectMocks
    private FastTrackHousingDisrepairFieldBuilder fastTrackHousingDisrepairFieldBuilder;

    @Test
    void shouldBuildFastTrackHousingDisrepairFields() {
        LocalDate now = LocalDate.now();
        LocalDate date1 = now.plusWeeks(10);
        LocalDate date2 = now.plusWeeks(12);
        when(workingDayIndicator.getNextWorkingDay(date1)).thenReturn(date1);
        when(workingDayIndicator.getNextWorkingDay(date2)).thenReturn(date2);

        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = CaseData.builder();

        fastTrackHousingDisrepairFieldBuilder.build(caseDataBuilder);

        CaseData caseData = caseDataBuilder.build();
        FastTrackHousingDisrepair housingDisrepair = caseData.getFastTrackHousingDisrepair();
        assertThat(housingDisrepair).isNotNull();
        assertThat(housingDisrepair.getInput1()).isEqualTo("The claimant must prepare a Scott Schedule of the items in disrepair.");
        assertThat(housingDisrepair.getInput2()).isEqualTo("""
                The columns should be headed:
                  •  Item
                  •  Alleged disrepair
                  •  Defendant’s response
                  •  Reserved for Judge’s use""");
        assertThat(housingDisrepair.getInput3()).isEqualTo("The claimant must upload to the Digital Portal the Scott Schedule with the relevant columns completed by 4pm on");
        assertThat(housingDisrepair.getDate1()).isEqualTo(date1);
        assertThat(housingDisrepair.getInput4()).isEqualTo("The defendant must upload to the Digital Portal the amended Scott Schedule with the relevant columns in response " +
                "completed by 4pm on");
        assertThat(housingDisrepair.getDate2()).isEqualTo(date2);
    }
}