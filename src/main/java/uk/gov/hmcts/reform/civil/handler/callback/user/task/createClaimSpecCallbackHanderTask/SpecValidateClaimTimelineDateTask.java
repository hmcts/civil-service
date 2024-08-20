package uk.gov.hmcts.reform.civil.handler.callback.user.task.createClaimSpecCallbackHanderTask;

import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.TimelineOfEvents;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class SpecValidateClaimTimelineDateTask {

    public CallbackResponse specValidateClaimTimelineDateTask(CaseData caseData) {
        List<String> errors = new ArrayList<>();
        if (caseData.getTimelineOfEvents() != null) {
            List<TimelineOfEvents> timelineOfEvent = caseData.getTimelineOfEvents();
            timelineOfEvent.forEach(timelineOfEvents -> {
                if (timelineOfEvents.getValue().getTimelineDate().isAfter(LocalDate.now())) {
                    errors.add("Correct the date. You can’t use a future date.");
                }
            });
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .errors(errors)
            .build();
    }
}
