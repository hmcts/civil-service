package uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandlertests.disposalhearingtests;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.bankholidays.WorkingDayIndicator;
import uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.disposalhearing.QuestionsToExpertsFieldBuilder;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingQuestionsToExperts;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QuestionsToExpertsFieldBuilderTest {

    @Mock
    private WorkingDayIndicator workingDayIndicator;

    @InjectMocks
    private QuestionsToExpertsFieldBuilder questionsToExpertsFieldBuilder;

    @Test
    void shouldSetQuestionsToExperts() {
        LocalDate nextWorkingDay = LocalDate.now().plusWeeks(6).plusDays(1); // Assuming the next working day is one day after 6 weeks
        when(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusWeeks(6))).thenReturn(nextWorkingDay);

        CaseData.CaseDataBuilder<?, ?> updatedData = CaseData.builder();

        questionsToExpertsFieldBuilder.build(updatedData);

        CaseData result = updatedData.build();
        DisposalHearingQuestionsToExperts questionsToExperts = result.getDisposalHearingQuestionsToExperts();
        assertThat(questionsToExperts).isNotNull();
        assertThat(questionsToExperts.getDate()).isEqualTo(nextWorkingDay);
    }
}