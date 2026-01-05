package uk.gov.hmcts.reform.civil.handler.callback.user.task.createclaim;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.TimelineOfEventDetails;
import uk.gov.hmcts.reform.civil.model.TimelineOfEvents;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class SpecValidateClaimTimelineDateTaskTest {

    @InjectMocks
    private SpecValidateClaimTimelineDateTask specValidateClaimTimelineDateTask;

    @Test
    void shouldValidateClaimTimelineDate_whenPopulated() {
        List<TimelineOfEvents> timelineOfEvents = new ArrayList<>();
        TimelineOfEventDetails timelineOfEventDetails = new TimelineOfEventDetails();
        timelineOfEventDetails.setTimelineDate(LocalDate.now().minusDays(1));
        TimelineOfEvents timelineOfEvents1 = new TimelineOfEvents();
        timelineOfEvents1.setValue(timelineOfEventDetails);
        timelineOfEvents.add(timelineOfEvents1);
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setTimelineOfEvents(timelineOfEvents);

        var response = (AboutToStartOrSubmitCallbackResponse) specValidateClaimTimelineDateTask.specValidateClaimTimelineDateTask(caseData);

        assertThat(response.getErrors()).isEmpty();
    }

    @Test
    void shouldReturnErrorWhenTimelineDatePopulatedWithFutureDate() {
        List<TimelineOfEvents> timelineOfEvents = new ArrayList<>();
        TimelineOfEventDetails timelineOfEventDetails = new TimelineOfEventDetails();
        timelineOfEventDetails.setTimelineDate(LocalDate.now().plusDays(1));
        TimelineOfEvents timelineOfEvents1 = new TimelineOfEvents();
        timelineOfEvents1.setValue(timelineOfEventDetails);
        timelineOfEvents.add(timelineOfEvents1);
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setTimelineOfEvents(timelineOfEvents);

        var response = (AboutToStartOrSubmitCallbackResponse) specValidateClaimTimelineDateTask.specValidateClaimTimelineDateTask(caseData);

        assertThat(response.getErrors()).contains("Correct the date. You can’t use a future date.");
    }
}
