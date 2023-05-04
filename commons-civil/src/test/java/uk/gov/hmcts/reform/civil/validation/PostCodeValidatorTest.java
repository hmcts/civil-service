package uk.gov.hmcts.reform.civil.validation;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestExecutionListeners;
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

            assertThat(errors).containsOnly("Postcode must be in England or Wales");
        }

        @Test
        void returnTrue_whenInputisFound() {
            when(postcodeLookupService.validatePostCodeForDefendant(any())).thenReturn(true);
            List<String> errors = postcodeValidator.validate("BA12SS");

            assertThat(errors).isEmpty();
        }
    }

    @Nested
    class ValidatePostCodeUk {

        @Test
        void returnError_whenEmptyInput() {
            Assertions.assertFalse(postcodeValidator.validateUk("").isEmpty());
            Assertions.assertFalse(postcodeValidator.validateUk(null).isEmpty());
        }

        @Test
        void returnError_whenInputNoUk() {
            String postcode = "postcode";
            when(postcodeLookupService.validatePostCodeUk(postcode))
                .thenReturn(false);
            Assertions.assertFalse(postcodeValidator.validateUk(postcode).isEmpty());
        }

        @Test
        void returnEmpty_whenInputUk() {
            String postcode = "postcode";
            when(postcodeLookupService.validatePostCodeUk(postcode))
                .thenReturn(true);
            Assertions.assertTrue(postcodeValidator.validateUk(postcode).isEmpty());
        }
    }
}
