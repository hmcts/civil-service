package uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandlertests.fasttracktests;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.bankholidays.WorkingDayIndicator;
import uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.fasttrack.FastTrackNotesFieldBuilder;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackNotes;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FastTrackNotesFieldBuilderTest {

    @Mock
    private WorkingDayIndicator workingDayIndicator;

    @InjectMocks
    private FastTrackNotesFieldBuilder fastTrackNotesFieldBuilder;

    @Test
    void shouldBuildFastTrackNotesFields() {
        LocalDate now = LocalDate.now();
        LocalDate nextWorkingDay = now.plusWeeks(1);
        when(workingDayIndicator.getNextWorkingDay(nextWorkingDay)).thenReturn(nextWorkingDay);

        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = CaseData.builder();

        fastTrackNotesFieldBuilder.build(caseDataBuilder);

        CaseData caseData = caseDataBuilder.build();
        FastTrackNotes notes = caseData.getFastTrackNotes();
        assertThat(notes).isNotNull();
        assertThat(notes.getInput()).isEqualTo("This Order has been made without a hearing. Each party has the right to apply to have this Order set aside or varied. Any " +
                "application must be received by the Court, together with the appropriate fee by 4pm on");
        assertThat(notes.getDate()).isEqualTo(nextWorkingDay);
    }
}