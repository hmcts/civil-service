package uk.gov.hmcts.reform.civil.validation;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.model.Address;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static java.time.LocalDate.now;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class PartyValidatorTest {

    private static final LocalDate NOW = now();

    @InjectMocks
    private PartyValidator validator;

    @Nested
    class ValidateName {

        @Test
        void shouldReturn_error_when_length_exceeds() {
            String name = "Mr Testing for maxlength 70 error First name and last name this name should throw error";
            List<String> errors = new ArrayList<>();

            validator.validateName(name, errors);
            assertThat(errors).isNotEmpty();
            assertThat(errors).contains("Name exceeds maximum length 70");
        }

        @Test
        void shouldNotReturn_error_when_length_is_in_limit() {
            String name = "Mr ABC TEST";
            List<String> errors = new ArrayList<>();

            validator.validateName(name, errors);
            assertThat(errors).isEmpty();
        }

        @Test
        public void should_return_Special_character_error_name() {
            Address primaryAddress = new Address();
            primaryAddress.setAddressLine1("Proper address");
            primaryAddress.setCounty(" ¨Proper address");

            List<String> errors = new ArrayList<>();
            validator.validateAddress(primaryAddress, errors);
            assertThat(errors).isNotEmpty();
            assertThat(errors).contains("Special characters are not allowed");
        }
    }

    @Nested
    class ValidateAddress {
        @Test
        void should_return_max_length_error() {
            Address primaryAddress = new Address();
            primaryAddress.setAddressLine1("Line 1 test again for more than 35 characters");
            primaryAddress.setAddressLine2("Line 1 test again for more than 35 characters");
            primaryAddress.setAddressLine3("Line 1 test again for more than 35 characters");
            primaryAddress.setCounty("Line 1 test again for more than 35 characters");
            primaryAddress.setPostCode("Line 1 test again for more than 35 characters");
            primaryAddress.setPostTown("Line 1 test again for more than 35 characters");

            List<String> errors = new ArrayList<>();
            validator.validateAddress(primaryAddress, errors);

            assertThat(errors).isNotEmpty();
            assertThat(errors).hasSize(6);
            assertThat(errors).contains("Building and Street exceeds maximum length 35");
        }

        @Test
        void should_not_return_max_length_error() {
            Address primaryAddress = new Address();
            primaryAddress.setAddressLine1("Proper address");
            primaryAddress.setAddressLine2("Proper address");
            primaryAddress.setAddressLine3("Proper address");
            primaryAddress.setCounty("Proper address");
            primaryAddress.setPostCode("Proper");
            primaryAddress.setPostTown("Proper address");

            List<String> errors = new ArrayList<>();
            validator.validateAddress(primaryAddress, errors);

            assertThat(errors).isEmpty();
        }

        @Test
        void should_return_Special_character_error_address_line1() {
            Address primaryAddress = new Address();
            primaryAddress.setAddressLine1("ˆProper address");

            List<String> errors = new ArrayList<>();
            validator.validateAddress(primaryAddress, errors);
            assertThat(errors).isNotEmpty();
            assertThat(errors).contains("Special characters are not allowed");
        }

        @Test
        void should_return_Special_character_error_address_line2() {
            Address primaryAddress = new Address();
            primaryAddress.setAddressLine1("Proper address");
            primaryAddress.setAddressLine2("`Proper address");

            List<String> errors = new ArrayList<>();
            validator.validateAddress(primaryAddress, errors);
            assertThat(errors).isNotEmpty();
            assertThat(errors).contains("Special characters are not allowed");
        }

        @Test
        void should_return_Special_character_error_address_line3() {
            Address primaryAddress = new Address();
            primaryAddress.setAddressLine1("Proper address");
            primaryAddress.setAddressLine3("´Proper address");

            List<String> errors = new ArrayList<>();
            validator.validateAddress(primaryAddress, errors);
            assertThat(errors).isNotEmpty();
            assertThat(errors).contains("Special characters are not allowed");
        }

        @Test
        void should_return_Special_character_error_address_posttown() {
            Address primaryAddress = new Address();
            primaryAddress.setAddressLine1("Proper address");
            primaryAddress.setPostTown(" ¨Proper address");

            List<String> errors = new ArrayList<>();
            validator.validateAddress(primaryAddress, errors);
            assertThat(errors).isNotEmpty();
            assertThat(errors).contains("Special characters are not allowed");
        }

        @Test
        void should_return_Special_character_error_address_postcode() {
            Address primaryAddress = new Address();
            primaryAddress.setAddressLine1("Proper address");
            primaryAddress.setPostCode(" ¨Proper address");

            List<String> errors = new ArrayList<>();
            validator.validateAddress(primaryAddress, errors);
            assertThat(errors).contains("Special characters are not allowed");
        }

        @Test
        void should_return_Special_character_error_address_county() {
            Address primaryAddress = new Address();
            primaryAddress.setAddressLine1("Proper address");
            primaryAddress.setCounty(" ¨Proper address");

            List<String> errors = new ArrayList<>();
            validator.validateAddress(primaryAddress, errors);
            assertThat(errors).isNotEmpty();
            assertThat(errors).contains("Special characters are not allowed");
        }
    }
}
