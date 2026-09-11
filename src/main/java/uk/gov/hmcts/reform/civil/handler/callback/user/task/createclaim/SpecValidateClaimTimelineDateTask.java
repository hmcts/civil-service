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
                if (isFullDate(timelineEvent) && isFutureTimelineDate(timelineEvent)) {
                    errors.add("Correct the date. You can’t use a future date.");
                } else if (!isMonthAndYearOnly(timelineEvent)) {
                    errors.add("Correct the date. You must enter atleast month and year.");
                }
            });
        }
        return errors;
    }

    private boolean caseDataHasTimelineOfEvents(CaseData caseData) {
        return caseData.getTimelineOfEvents() != null && !caseData.getTimelineOfEvents().isEmpty();
    }

    private boolean isFullDate(TimelineOfEvents timelineEvent) {
        return timelineEvent.getValue().getTimelineDay() != null && timelineEvent.getValue().getTimelineMonth() != null && timelineEvent.getValue().getTimelineYear() != null;
    }

    private boolean isMonthAndYearOnly(TimelineOfEvents timelineEvent) {
        return timelineEvent.getValue().getTimelineDay() == null && timelineEvent.getValue().getTimelineMonth() != null && timelineEvent.getValue().getTimelineYear() != null;
    }

    private boolean isFutureTimelineDate(TimelineOfEvents timelineEvent) {
        int year = Integer.parseInt(timelineEvent.getValue().getTimelineYear());
        int month = Integer.parseInt(timelineEvent.getValue().getTimelineMonth());
        int dayOfMonth = Integer.parseInt(timelineEvent.getValue().getTimelineDay());
        LocalDate timeLineDate = LocalDate.of(year, month, dayOfMonth);
        return timeLineDate.isAfter(LocalDate.now());
    }

    private CallbackResponse buildCallbackResponse(List<String> errors) {
        return AboutToStartOrSubmitCallbackResponse.builder()
            .errors(errors)
            .build();
    }
}
