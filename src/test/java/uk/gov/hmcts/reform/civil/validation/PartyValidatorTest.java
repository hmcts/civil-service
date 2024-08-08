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
            Address primaryAddress = Address.builder()
                .addressLine1("Proper address")
                .county(" ¨Proper address")
                .build();

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
            Address primaryAddress = Address.builder()
                .addressLine1("Line 1 test again for more than 35 characters")
                .addressLine2("Line 1 test again for more than 35 characters")
                .addressLine3("Line 1 test again for more than 35 characters")
                .county("Line 1 test again for more than 35 characters")
                .postCode("Line 1 test again for more than 35 characters")
                .postTown("Line 1 test again for more than 35 characters")
                .build();

            List<String> errors = new ArrayList<>();
            validator.validateAddress(primaryAddress, errors);

            assertThat(errors).isNotEmpty();
            assertThat(errors).hasSize(6);
            assertThat(errors).contains("Building and Street exceeds maximum length 35");
        }

        @Test
        void should_not_return_max_length_error() {
            Address primaryAddress = Address.builder()
                .addressLine1("Proper address")
                .addressLine2("Proper address")
                .addressLine3("Proper address")
                .county("Proper address")
                .postCode("Proper")
                .postTown("Proper address")
                .build();

            List<String> errors = new ArrayList<>();
            validator.validateAddress(primaryAddress, errors);

            assertThat(errors).isEmpty();
        }

        @Test
        void should_return_Special_character_error_address_line1() {
            Address primaryAddress = Address.builder()
                .addressLine1("ˆProper address")
                .build();

            List<String> errors = new ArrayList<>();
            validator.validateAddress(primaryAddress, errors);
            assertThat(errors).isNotEmpty();
            assertThat(errors).contains("Special characters are not allowed");
        }

        @Test
        void should_return_Special_character_error_address_line2() {
            Address primaryAddress = Address.builder()
                .addressLine1("Proper address")
                .addressLine2("`Proper address")
                .build();

            List<String> errors = new ArrayList<>();
            validator.validateAddress(primaryAddress, errors);
            assertThat(errors).isNotEmpty();
            assertThat(errors).contains("Special characters are not allowed");
        }

        @Test
        void should_return_Special_character_error_address_line3() {
            Address primaryAddress = Address.builder()
                .addressLine1("Proper address")
                .addressLine3("´Proper address")
                .build();

            List<String> errors = new ArrayList<>();
            validator.validateAddress(primaryAddress, errors);
            assertThat(errors).isNotEmpty();
            assertThat(errors).contains("Special characters are not allowed");
        }

        @Test
        void should_return_Special_character_error_address_posttown() {
            Address primaryAddress = Address.builder()
                .addressLine1("Proper address")
                .postTown(" ¨Proper address")
                .build();

            List<String> errors = new ArrayList<>();
            validator.validateAddress(primaryAddress, errors);
            assertThat(errors).isNotEmpty();
            assertThat(errors).contains("Special characters are not allowed");
        }

        @Test
        void should_return_Special_character_error_address_postcode() {
            Address primaryAddress = Address.builder()
                .addressLine1("Proper address")
                .postCode(" ¨Proper address")
                .build();

            List<String> errors = new ArrayList<>();
            validator.validateAddress(primaryAddress, errors);
            assertThat(errors).contains("Special characters are not allowed");
        }

        @Test
        void should_return_Special_character_error_address_county() {
            Address primaryAddress = Address.builder()
                .addressLine1("Proper address")
                .county(" ¨Proper address")
                .build();

            List<String> errors = new ArrayList<>();
            validator.validateAddress(primaryAddress, errors);
            assertThat(errors).isNotEmpty();
            assertThat(errors).contains("Special characters are not allowed");
        }
    }
}
