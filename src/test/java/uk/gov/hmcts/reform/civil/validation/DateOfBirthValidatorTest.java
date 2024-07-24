package uk.gov.hmcts.reform.civil.validation;

import org.hibernate.validator.messageinterpolation.ParameterMessageInterpolator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.model.Party;

import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class DateOfBirthValidatorTest {

    private DateOfBirthValidator dateOfBirthValidator;

    @BeforeEach
    void setUp() {
        ValidatorFactory validatorFactory = Validation.byDefaultProvider()
            .configure()
            .messageInterpolator(new ParameterMessageInterpolator())
            .buildValidatorFactory();

        Validator validator = validatorFactory.getValidator();
        dateOfBirthValidator = new DateOfBirthValidator(validator);
    }

    @Test
    void shouldReturnError_whenDateOfBirthIsInTheFuture() {
        Party party = Party.builder().individualDateOfBirth(LocalDate.now().plusDays(1)).build();

        var errors = dateOfBirthValidator.validate(party);

        assertThat(errors).containsExactly("The date entered cannot be in the future");
    }

    @Test
    void shouldReturnNoError_whenDateOfBirthIsInThePast() {
        Party party = Party.builder().individualDateOfBirth(LocalDate.now().minusYears(19)).build();

        var errors = dateOfBirthValidator.validate(party);

        assertThat(errors).isEmpty();
    }

    @Test
    void shouldReturnNoError_whenDateOfBirthIsNotProvided() {
        Party party = Party.builder().build();

        var errors = dateOfBirthValidator.validate(party);

        assertThat(errors).isEmpty();
    }
}
