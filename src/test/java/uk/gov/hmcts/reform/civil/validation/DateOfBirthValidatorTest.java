package uk.gov.hmcts.reform.civil.validation;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.hmcts.reform.civil.model.Party;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = {DateOfBirthValidator.class, ValidationAutoConfiguration.class})
class DateOfBirthValidatorTest {

    @Autowired
    DateOfBirthValidator validator;

    @Test
    void shouldReturnError_whenDateOfBirthIsInTheFuture() {
        Party party = Party.builder().individualDateOfBirth(LocalDate.now().plusDays(1)).build();

        var errors = validator.validate(party);

        assertThat(errors).containsExactly("The date entered cannot be in the future");
    }

    @Test
    void shouldReturnNoError_whenDateOfBirthIsInThePast() {
        Party party = Party.builder().individualDateOfBirth(LocalDate.now().minusYears(19)).build();

        var errors = validator.validate(party);

        assertThat(errors).isEmpty();
    }

    @Test
    void shouldReturnNoError_whenDateOfBirthIsNotProvided() {
        Party party = Party.builder().build();

        var errors = validator.validate(party);

        assertThat(errors).isEmpty();
    }
}
