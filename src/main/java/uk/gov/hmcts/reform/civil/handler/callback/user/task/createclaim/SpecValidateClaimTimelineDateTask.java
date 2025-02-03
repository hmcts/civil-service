package uk.gov.hmcts.reform.civil.handler.callback.user.task.createclaim;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.TimelineOfEvents;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
public class SpecValidateClaimTimelineDateTask {

    public CallbackResponse specValidateClaimTimelineDateTask(CaseData caseData) {
        List<String> errors = validateTimelineDates(caseData);
        return buildCallbackResponse(errors);
    }

    private List<String> validateTimelineDates(CaseData caseData) {
        List<String> errors = new ArrayList<>();
        if (caseDataHasTimelineOfEvents(caseData)) {
            caseData.getTimelineOfEvents().forEach(timelineEvent -> {
                if (isFutureTimelineDate(timelineEvent)) {
                    errors.add("Correct the date. You can’t use a future date.");
                }
            });
        }
        return errors;
    }

    private boolean caseDataHasTimelineOfEvents(CaseData caseData) {
        return caseData.getTimelineOfEvents() != null && !caseData.getTimelineOfEvents().isEmpty();
    }

    private boolean isFutureTimelineDate(TimelineOfEvents timelineEvent) {
        return timelineEvent.getValue().getTimelineDate().isAfter(LocalDate.now());
    }

    private CallbackResponse buildCallbackResponse(List<String> errors) {
        return AboutToStartOrSubmitCallbackResponse.builder()
            .errors(errors)
            .build();
    }
}
