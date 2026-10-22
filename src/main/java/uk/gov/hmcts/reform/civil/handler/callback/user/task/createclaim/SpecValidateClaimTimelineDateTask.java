package uk.gov.hmcts.reform.civil.handler.callback.user.task.createclaim;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.TimelineOfEventDetails;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.apache.commons.lang3.StringUtils.isBlank;

@Component
public class SpecValidateClaimTimelineDateTask {

    static final String FUTURE_DATE_ERROR = "Correct the date. You can’t use a future date.";
    static final String MONTH_AND_YEAR_REQUIRED_ERROR = "Correct the date. You must enter at least month and year.";
    static final String INVALID_DATE_ERROR = "Correct the date. Enter a real date.";

    public CallbackResponse specValidateClaimTimelineDateTask(CaseData caseData) {
        List<String> errors = validateTimelineDates(caseData);
        return buildCallbackResponse(errors);
    }

    private List<String> validateTimelineDates(CaseData caseData) {
        List<String> errors = new ArrayList<>();
        if (caseDataHasTimelineOfEvents(caseData)) {
            caseData.getTimelineOfEvents().forEach(timelineEvent ->
                validateTimelineDate(timelineEvent.getValue()).ifPresent(errors::add));
        }
        return errors;
    }

    private Optional<String> validateTimelineDate(TimelineOfEventDetails details) {
        if (isBlank(details.getTimelineMonth()) || isBlank(details.getTimelineYear())) {
            return Optional.of(MONTH_AND_YEAR_REQUIRED_ERROR);
        }
        try {
            YearMonth yearMonth = YearMonth.of(
                Integer.parseInt(details.getTimelineYear()),
                Integer.parseInt(details.getTimelineMonth())
            );
            boolean isFuture = isBlank(details.getTimelineDay())
                ? yearMonth.isAfter(YearMonth.now())
                : yearMonth.atDay(Integer.parseInt(details.getTimelineDay())).isAfter(LocalDate.now());
            return isFuture ? Optional.of(FUTURE_DATE_ERROR) : Optional.empty();
        } catch (NumberFormatException | DateTimeException e) {
            return Optional.of(INVALID_DATE_ERROR);
        }
    }

    private boolean caseDataHasTimelineOfEvents(CaseData caseData) {
        return caseData.getTimelineOfEvents() != null && !caseData.getTimelineOfEvents().isEmpty();
    }

    private CallbackResponse buildCallbackResponse(List<String> errors) {
        return AboutToStartOrSubmitCallbackResponse.builder()
            .errors(errors)
            .build();
    }
}
