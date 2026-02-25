package uk.gov.hmcts.reform.civil.validation;

import org.hibernate.validator.messageinterpolation.ParameterMessageInterpolator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.model.RespondToClaim;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class PaymentDateValidatorTest {

    private PaymentDateValidator paymentDateValidator;

    @BeforeEach
    void setUp() {
        ValidatorFactory validatorFactory = Validation.byDefaultProvider()
            .configure()
            .messageInterpolator(new ParameterMessageInterpolator())
            .buildValidatorFactory();

        Validator validator = validatorFactory.getValidator();
        paymentDateValidator = new PaymentDateValidator(validator);
    }

    @Test
    void shouldBeValidDate_whenToday() {
        RespondToClaim respondToClaim = new RespondToClaim()
            .setWhenWasThisAmountPaid(LocalDate.now().minusDays(1))
            ;

        assertTrue(paymentDateValidator.validate(respondToClaim).isEmpty());
    }

    @Test
    void shouldBeValidDate_whenPastDate() {
        RespondToClaim respondToClaim = new RespondToClaim()
            .setWhenWasThisAmountPaid(LocalDate.now())
            ;

        assertTrue(paymentDateValidator.validate(respondToClaim).isEmpty());
    }

    @Test
    void shouldBeValidDate_whenFutureDate() {
        RespondToClaim respondToClaim = new RespondToClaim()
            .setWhenWasThisAmountPaid(LocalDate.now().plusDays(1))
            ;

        assertEquals(1, paymentDateValidator.validate(respondToClaim).size());
        assertEquals("Date for when amount was paid must be today or in the past",
                     paymentDateValidator.validate(respondToClaim).get(0));
    }

}
