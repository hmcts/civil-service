package uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandlertests.fieldsnihltests;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.fieldsnihl.FieldsNihlUtils;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class FieldsNihlUtilsTest {

    @InjectMocks
    private FieldsNihlUtils fieldsNihlUtils;

    @Test
    void shouldReturnErrorWhenDateIsInThePast() {
        LocalDate pastDate = LocalDate.now().minusDays(1);
        Optional<String> result = fieldsNihlUtils.validateFutureDate(pastDate);
        assertEquals(Optional.of("Date must be in the future"), result);
    }

    @Test
    void shouldReturnEmptyWhenDateIsInTheFuture() {
        LocalDate futureDate = LocalDate.now().plusDays(1);
        Optional<String> result = fieldsNihlUtils.validateFutureDate(futureDate);
        assertEquals(Optional.empty(), result);
    }

    @Test
    void shouldReturnEmptyWhenQuantityIsGreaterThanOrEqualToZero() {
        Integer zeroQuantity = 0;
        Optional<String> result = fieldsNihlUtils.validateGreaterOrEqualZero(zeroQuantity);
        assertEquals(Optional.empty(), result);

        Integer positiveQuantity = 1;
        result = fieldsNihlUtils.validateGreaterOrEqualZero(positiveQuantity);
        assertEquals(Optional.empty(), result);
    }
}