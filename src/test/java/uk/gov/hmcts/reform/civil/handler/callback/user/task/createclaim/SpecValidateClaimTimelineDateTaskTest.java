package uk.gov.hmcts.reform.civil.handler.callback.user.task.createclaim;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.TimelineOfEventDetails;
import uk.gov.hmcts.reform.civil.model.TimelineOfEvents;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.createclaim.SpecValidateClaimTimelineDateTask.FUTURE_DATE_ERROR;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.createclaim.SpecValidateClaimTimelineDateTask.INVALID_DATE_ERROR;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.createclaim.SpecValidateClaimTimelineDateTask.MONTH_AND_YEAR_REQUIRED_ERROR;

@ExtendWith(MockitoExtension.class)
class SpecValidateClaimTimelineDateTaskTest {

    @InjectMocks
    private SpecValidateClaimTimelineDateTask specValidateClaimTimelineDateTask;

    @Test
    void shouldReturnNoErrors_whenFullDateIsInThePast() {
        LocalDate yesterday = LocalDate.now().minusDays(1);

        List<String> errors = validate(timelineEvent(
            String.valueOf(yesterday.getDayOfMonth()),
            String.valueOf(yesterday.getMonthValue()),
            String.valueOf(yesterday.getYear())
        ));

        assertThat(errors).isEmpty();
    }

    @Test
    void shouldReturnNoErrors_whenDayIsMissingAndMonthIsCurrent() {
        YearMonth now = YearMonth.now();

        List<String> errors = validate(timelineEvent(null, String.valueOf(now.getMonthValue()), String.valueOf(now.getYear())));

        assertThat(errors).isEmpty();
    }

    @Test
    void shouldReturnFutureDateError_whenFullDateIsInTheFuture() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);

        List<String> errors = validate(timelineEvent(
            String.valueOf(tomorrow.getDayOfMonth()),
            String.valueOf(tomorrow.getMonthValue()),
            String.valueOf(tomorrow.getYear())
        ));

        assertThat(errors).containsExactly(FUTURE_DATE_ERROR);
    }

    @Test
    void shouldReturnFutureDateError_whenDayIsMissingAndMonthIsInTheFuture() {
        YearMonth nextMonth = YearMonth.now().plusMonths(1);

        List<String> errors = validate(timelineEvent(
            null,
            String.valueOf(nextMonth.getMonthValue()),
            String.valueOf(nextMonth.getYear())
        ));

        assertThat(errors).containsExactly(FUTURE_DATE_ERROR);
    }

    @ParameterizedTest
    @CsvSource(value = {
        "1, NULL, 2024",
        "1, 3, NULL",
        "NULL, NULL, NULL",
        "1, '', 2024"
    }, nullValues = "NULL")
    void shouldReturnMonthAndYearRequiredError_whenMonthOrYearIsMissing(String day, String month, String year) {
        List<String> errors = validate(timelineEvent(day, month, year));

        assertThat(errors).containsExactly(MONTH_AND_YEAR_REQUIRED_ERROR);
    }

    @ParameterizedTest
    @CsvSource({
        "31, 2, 2024",
        "1, 13, 2024",
        "0, 3, 2024",
        "1.5, 3, 2024"
    })
    void shouldReturnInvalidDateError_whenDateDoesNotExist(String day, String month, String year) {
        List<String> errors = validate(timelineEvent(day, month, year));

        assertThat(errors).containsExactly(INVALID_DATE_ERROR);
    }

    @Test
    void shouldReturnOneErrorPerInvalidEvent() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);

        List<String> errors = validate(
            timelineEvent("15", "3", "2024"),
            timelineEvent(null, null, "2024"),
            timelineEvent(
                String.valueOf(tomorrow.getDayOfMonth()),
                String.valueOf(tomorrow.getMonthValue()),
                String.valueOf(tomorrow.getYear())
            )
        );

        assertThat(errors).containsExactly(MONTH_AND_YEAR_REQUIRED_ERROR, FUTURE_DATE_ERROR);
    }

    @Test
    void shouldReturnNoErrors_whenNoTimelineEvents() {
        CaseData caseData = CaseDataBuilder.builder().build();

        var response = (AboutToStartOrSubmitCallbackResponse) specValidateClaimTimelineDateTask
            .specValidateClaimTimelineDateTask(caseData);

        assertThat(response.getErrors()).isEmpty();
    }

    private List<String> validate(TimelineOfEvents... events) {
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setTimelineOfEvents(List.of(events));

        var response = (AboutToStartOrSubmitCallbackResponse) specValidateClaimTimelineDateTask
            .specValidateClaimTimelineDateTask(caseData);

        return response.getErrors();
    }

    private TimelineOfEvents timelineEvent(String day, String month, String year) {
        TimelineOfEventDetails details = new TimelineOfEventDetails();
        details.setTimelineDay(day);
        details.setTimelineMonth(month);
        details.setTimelineYear(year);
        details.setTimelineDescription("Something happened");
        TimelineOfEvents event = new TimelineOfEvents();
        event.setValue(details);
        return event;
    }
}
