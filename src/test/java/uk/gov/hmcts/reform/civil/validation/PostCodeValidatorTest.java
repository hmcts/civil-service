package uk.gov.hmcts.reform.civil.validation;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.civil.postcode.PostcodeLookupService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = {
    PostcodeValidator.class
})
public class PostCodeValidatorTest {

    @MockBean
    private PostcodeLookupService postcodeLookupService;

    @Autowired
    private PostcodeValidator postcodeValidator;

    @Nested
    class ValidatePostCode {

        @Test
        void returnError_whenStartswithBT() {
            List<String> errors = postcodeValidator.validate("BT11SS");

            assertThat(errors).containsOnly("Postcode must be in England or Wales");
        }

        @Test
        void returnError_whenInputisNull() {
            List<String> errors = postcodeValidator.validate(null);

            assertThat(errors).containsOnly("Please enter Postcode");
        }

        @Test
        void returnError_whenInputisNotFound() {
            when(postcodeLookupService.validatePostCodeForDefendant(any())).thenReturn(false);
            List<String> errors = postcodeValidator.validate("TEST");

            assertThat(errors).contains("Postcode format is invalid");
        }

        @Test
        void returnTrue_whenInputisFound() {
            when(postcodeLookupService.validatePostCodeForDefendant(any())).thenReturn(true);
            List<String> errors = postcodeValidator.validate("BA1 2SS");

            assertThat(errors).isEmpty();
        }

        @Test
        void returnError_whenInputContainsMaliciousCharacters() {
            List<String> errors = postcodeValidator.validate("BA1<script>alert('xss')</script>2SS");

            assertThat(errors).containsOnly("Postcode format is invalid");
        }

        @Test
        void returnError_whenInputTooLong() {
            List<String> errors = postcodeValidator.validate("VERYLONGPOSTCODEINPUT");

            assertThat(errors).containsOnly("Postcode format is invalid");
        }

        @Test
        void returnError_whenInputInvalidFormat() {
            List<String> errors = postcodeValidator.validate("INVALID");

            assertThat(errors).containsOnly("Postcode format is invalid");
        }

        @Test
        void normalizeSpacesInPostcode() {
            when(postcodeLookupService.validatePostCodeForDefendant("BA1 2SS")).thenReturn(true);
            List<String> errors = postcodeValidator.validate("BA1    2SS");

            assertThat(errors).isEmpty();
        }

        @Test
        void handleEmptyStringInput() {
            List<String> errors = postcodeValidator.validate("");

            assertThat(errors).containsOnly("Please enter Postcode");
        }

        @Test
        void handleWhitespaceOnlyInput() {
            List<String> errors = postcodeValidator.validate("   ");

            assertThat(errors).containsOnly("Please enter Postcode");
        }
    }
}
