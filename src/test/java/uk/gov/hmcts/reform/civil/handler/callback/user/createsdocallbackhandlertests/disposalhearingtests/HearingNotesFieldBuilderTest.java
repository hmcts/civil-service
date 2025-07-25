package uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandlertests.disposalhearingtests;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.bankholidays.WorkingDayIndicator;
import uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.disposalhearing.HearingNotesFieldBuilder;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingNotes;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HearingNotesFieldBuilderTest {

    @Mock
    private WorkingDayIndicator workingDayIndicator;

    @InjectMocks
    private HearingNotesFieldBuilder hearingNotesFieldBuilder;

    @Test
    void shouldSetHearingNotes() {
        LocalDate nextWorkingDay = LocalDate.now().plusWeeks(1).plusDays(1); // Assuming the next working day is one day after a week
        when(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusWeeks(1))).thenReturn(nextWorkingDay);

        CaseData.CaseDataBuilder<?, ?> updatedData = CaseData.builder();

        hearingNotesFieldBuilder.build(updatedData);

        CaseData result = updatedData.build();
        DisposalHearingNotes notes = result.getDisposalHearingNotes();
        assertThat(notes).isNotNull();
        assertThat(notes.getInput())
                .isEqualTo("This Order has been made without a hearing. Each party has the right to apply to "
                        + "have this Order set aside or varied. Any such application must be uploaded to the "
                        + "Digital Portal together with the appropriate fee, by 4pm on");
        assertThat(notes.getDate()).isEqualTo(nextWorkingDay);
    }
}