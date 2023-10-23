package uk.gov.hmcts.reform.civil.validation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.bankholidays.WorkingDayIndicator;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static java.time.LocalDate.now;
import static java.util.Collections.emptyList;
import static uk.gov.hmcts.reform.civil.service.DeadlinesCalculator.END_OF_BUSINESS_DAY;

@Service
@RequiredArgsConstructor
public class DeadlineExtensionValidator {

    /**
     * initial deadline for a claim.
     */
    public static final long INITIAL_DEADLINE = 14;
    /**
     * deadline extension when respondent acks the claim.
     */
    public static final long ACK_EXTENSION = 14;
    /**
     * deadline extension when there is an agreed extension.
     */
    public static final long AGREED_EXTENSION = 28;

    private final WorkingDayIndicator workingDayIndicator;

    public List<String> validateProposedDeadline(LocalDate dateToValidate, LocalDateTime responseDeadline) {
        if (!dateToValidate.isAfter(now())) {
            return List.of("The agreed extension date must be a date in the future");
        }

        if (!dateToValidate.isAfter(responseDeadline.toLocalDate())) {
            return List.of("The agreed extension date must be after the current deadline");
        }

        if (LocalDateTime.of(dateToValidate, END_OF_BUSINESS_DAY).isAfter(responseDeadline.plusDays(28))) {
            return List.of("The agreed extension date cannot be more than 28 days after the current deadline");
        }

        return emptyList();
    }

    public List<String> specValidateProposedDeadline(LocalDate dateToValidate,
                                                     LocalDateTime responseDeadline,
                                                     Boolean isAoSApplied) {
        if (!dateToValidate.isAfter(now())) {
            return List.of("The agreed extension date must be a date in the future");
        }

        if (!dateToValidate.isAfter(responseDeadline.toLocalDate())) {
            return List.of("The agreed extension date must be after the current deadline");
        }

        if (LocalDateTime.of(dateToValidate, END_OF_BUSINESS_DAY).isAfter(responseDeadline.plusDays(28))) {
            return List.of("The agreed extension date cannot be more than 28 days after the current deadline");
        }

        LocalDateTime newResponseDeadline = workingDayIndicator.isWorkingDay(responseDeadline.plusDays(28)
                                                                                 .toLocalDate()) ? responseDeadline.plusDays(
            28) : LocalDateTime.of(
            workingDayIndicator.getNextWorkingDay(responseDeadline.plusDays(28).toLocalDate()),
            LocalDateTime.now().toLocalTime()
        );

        if (!isAoSApplied && LocalDateTime.of(
            dateToValidate,
            END_OF_BUSINESS_DAY
        ).isAfter(newResponseDeadline)) {
            return List.of("Date must be from claim issue date plus a maximum of 56 days.");
        }

        if (isAoSApplied && LocalDateTime.of(
            dateToValidate,
            END_OF_BUSINESS_DAY
        ).isAfter(newResponseDeadline)) {
            return List.of("Date must be from claim issue date plus a maximum of between 29 and 56 days.");
        }

        if (!workingDayIndicator.isWorkingDay(dateToValidate)) {
            return List.of("Date must be next working weekday");
        }
        return emptyList();
    }

    public LocalDate getMaxDate(LocalDateTime notificationDetailsDate, LocalDateTime ackNotificationDate) {
        LocalDate date = notificationDetailsDate
            .plusDays(
                AGREED_EXTENSION
                    + INITIAL_DEADLINE
                    + (ackNotificationDate == null ? 0L : ACK_EXTENSION))
            .toLocalDate();
        while (!workingDayIndicator.isWorkingDay(date)) {
            date = date.plusDays(1L);
        }
        return date;
    }
}
