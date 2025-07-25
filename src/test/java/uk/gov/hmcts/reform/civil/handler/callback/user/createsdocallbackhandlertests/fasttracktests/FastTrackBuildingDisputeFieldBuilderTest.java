package uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandlertests.fasttracktests;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.bankholidays.WorkingDayIndicator;
import uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.fasttrack.FastTrackBuildingDisputeFieldBuilder;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FastTrackBuildingDisputeFieldBuilderTest {

    @Mock
    private WorkingDayIndicator workingDayIndicator;

    @InjectMocks
    private FastTrackBuildingDisputeFieldBuilder fastTrackBuildingDisputeFieldBuilder;

    @Test
    void shouldBuildFastTrackBuildingDisputeFields() {
        LocalDate now = LocalDate.now();
        LocalDate date1 = now.plusWeeks(10);
        LocalDate date2 = now.plusWeeks(12);
        when(workingDayIndicator.getNextWorkingDay(date1)).thenReturn(date1);
        when(workingDayIndicator.getNextWorkingDay(date2)).thenReturn(date2);

        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = CaseData.builder();

        fastTrackBuildingDisputeFieldBuilder.build(caseDataBuilder);

        CaseData caseData = caseDataBuilder.build();
        assertThat(caseData.getFastTrackBuildingDispute()).isNotNull();
        assertThat(caseData.getFastTrackBuildingDispute().getInput1()).isEqualTo("The claimant must prepare a Scott Schedule of the defects, items of damage, or any other " +
                "relevant matters");
        assertThat(caseData.getFastTrackBuildingDispute().getInput2()).isEqualTo("""
                The columns should be headed:
                  •  Item
                  •  Alleged defect
                  •  Claimant’s costing
                  •  Defendant’s response
                  •  Defendant’s costing
                  •  Reserved for Judge’s use""");
        assertThat(caseData.getFastTrackBuildingDispute().getInput3()).isEqualTo("The claimant must upload to the Digital Portal the Scott Schedule with the relevant columns " +
                "completed by 4pm on");
        assertThat(caseData.getFastTrackBuildingDispute().getDate1()).isEqualTo(date1);
        assertThat(caseData.getFastTrackBuildingDispute().getInput4()).isEqualTo("The defendant must upload to the Digital Portal an amended version of the Scott Schedule with " +
                "the relevant columns in response completed by 4pm on");
        assertThat(caseData.getFastTrackBuildingDispute().getDate2()).isEqualTo(date2);
    }
}