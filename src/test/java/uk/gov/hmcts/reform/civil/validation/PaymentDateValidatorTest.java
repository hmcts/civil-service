package uk.gov.hmcts.reform.civil.validation;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.hmcts.reform.civil.model.RespondToClaim;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = {PaymentDateValidator.class, ValidationAutoConfiguration.class})
class PaymentDateValidatorTest {

    @Autowired
    PaymentDateValidator paymentDateValidator;

    @Test
    void shouldBeValidDate_whenToday() {
        RespondToClaim respondToClaim = RespondToClaim.builder()
            .whenWasThisAmountPaid(LocalDate.now().minusDays(1))
            .build();

        assertTrue(paymentDateValidator.validate(respondToClaim).isEmpty());
    }

    @Test
    void shouldBeValidDate_whenPastDate() {
        RespondToClaim respondToClaim = RespondToClaim.builder()
            .whenWasThisAmountPaid(LocalDate.now())
            .build();

        assertTrue(paymentDateValidator.validate(respondToClaim).isEmpty());
    }

    @Test
    void shouldBeValidDate_whenFutureDate() throws NoSuchFieldException {
        RespondToClaim respondToClaim = RespondToClaim.builder()
            .whenWasThisAmountPaid(LocalDate.now().plusDays(1))
            .build();

        assertEquals(1, paymentDateValidator.validate(respondToClaim).size());
        assertEquals("Date for when amount was paid must be today or in the past",
                     paymentDateValidator.validate(respondToClaim).get(0));
    }

}
