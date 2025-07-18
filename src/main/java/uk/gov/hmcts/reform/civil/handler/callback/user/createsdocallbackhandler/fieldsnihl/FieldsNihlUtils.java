package uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.fieldsnihl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Optional;

import static uk.gov.hmcts.reform.civil.constants.CreateSDOText.ERROR_MESSAGE_DATE_MUST_BE_IN_THE_FUTURE;
import static uk.gov.hmcts.reform.civil.constants.CreateSDOText.ERROR_MESSAGE_NUMBER_CANNOT_BE_LESS_THAN_ZERO;

@Slf4j
@Component
public class FieldsNihlUtils {

    public Optional<String> validateFutureDate(LocalDate date) {
        LocalDate today = LocalDate.now();
        log.debug("Checking if date {} is after today {}", date, today);
        if (date.isAfter(today)) {
            return Optional.empty();
        }
        log.warn("Date {} is not in the future", date);
        return Optional.of(ERROR_MESSAGE_DATE_MUST_BE_IN_THE_FUTURE);
    }

    public Optional<String> validateGreaterOrEqualZero(Integer quantity) {
        log.debug("Checking if quantity {} is greater than or equal to zero", quantity);
        if (quantity < 0) {
            log.warn("Quantity {} is less than zero", quantity);
            return Optional.of(ERROR_MESSAGE_NUMBER_CANNOT_BE_LESS_THAN_ZERO);
        }
        return Optional.empty();
    }
}
