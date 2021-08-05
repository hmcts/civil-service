package uk.gov.hmcts.reform.civil.validation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static java.time.LocalDate.now;
import static java.util.Collections.emptyList;
import static uk.gov.hmcts.reform.civil.service.DeadlinesCalculator.END_OF_BUSINESS_DAY;

@Service
@RequiredArgsConstructor
public class DeadlineExtensionValidator {

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

    public List<String> specValidateProposedDeadline(LocalDate dateToValidate, LocalDateTime responseDeadline, Boolean isAoSApplied) {
        if (!dateToValidate.isAfter(now())) {
            return List.of("The agreed extension date must be a date in the future");
        }

        if (!dateToValidate.isAfter(responseDeadline.toLocalDate())) {
            return List.of("The agreed extension date must be after the current deadline");
        }

        if (!isAoSApplied && LocalDateTime.of(dateToValidate, END_OF_BUSINESS_DAY).isAfter(responseDeadline.plusDays(28))) {
            return List.of("Date must be from claim issue date plus a maximum of 42 days.");
        }

        if (isAoSApplied && LocalDateTime.of(dateToValidate, END_OF_BUSINESS_DAY).isAfter(responseDeadline.plusDays(28))) {
            return List.of("Date must be from claim issue date plus a maximum of between 29 and 56 days.");
        }

        return emptyList();
    }
}
