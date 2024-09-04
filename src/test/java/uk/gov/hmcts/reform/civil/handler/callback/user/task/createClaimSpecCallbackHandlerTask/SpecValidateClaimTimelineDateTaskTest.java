package uk.gov.hmcts.reform.civil.handler.callback.user.task.createclaimspeccallbackhandlertask;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.createclaimspeccallbackhandertask.SpecValidateClaimTimelineDateTask;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.TimelineOfEventDetails;
import uk.gov.hmcts.reform.civil.model.TimelineOfEvents;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class SpecValidateClaimTimelineDateTaskTest {

    @InjectMocks
    private SpecValidateClaimTimelineDateTask specValidateClaimTimelineDateTask;

    @Test
    void shouldValidateClaimTimelineDate_whenPopulated() {
        // Given
        List<TimelineOfEvents> timelineOfEvents = new ArrayList<>();
        timelineOfEvents.add(
            TimelineOfEvents.builder().value(TimelineOfEventDetails.builder().timelineDate(LocalDate.now().minusDays(1)).build()).build());
        CaseData caseData = CaseData.builder().timelineOfEvents(timelineOfEvents)
            .build();

        // When
        var response = (AboutToStartOrSubmitCallbackResponse) specValidateClaimTimelineDateTask.specValidateClaimTimelineDateTask(caseData);

        // Then
        assertThat(response.getErrors()).isEmpty();
    }

    @Test
    void shouldReturnErrorWhenTimelineDatePopulatedWithFutureDate() {
        // Given
        List<TimelineOfEvents> timelineOfEvents = new ArrayList<>();
        timelineOfEvents.add(
            TimelineOfEvents.builder().value(TimelineOfEventDetails.builder().timelineDate(LocalDate.now().plusDays(1)).build()).build());
        CaseData caseData = CaseData.builder().timelineOfEvents(timelineOfEvents)
            .build();

        // When
        var response = (AboutToStartOrSubmitCallbackResponse) specValidateClaimTimelineDateTask.specValidateClaimTimelineDateTask(caseData);

        // Then
        assertThat(response.getErrors()).contains("Correct the date. You can’t use a future date.");
    }
}
