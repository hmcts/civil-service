package uk.gov.hmcts.reform.civil.validation;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.civil.service.postcode.PostcodeLookupService;

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
            List<String> errors = postcodeValidator.validatePostCodeForDefendant("BT11SS");

            assertThat(errors).containsOnly("Postcode must be in England or Wales");
        }

        @Test
        void returnError_whenInputisNull() {
            List<String> errors = postcodeValidator.validatePostCodeForDefendant(null);

            assertThat(errors).containsOnly("Please enter Postcode");
        }

        @Test
        void returnError_whenInputisNotFound() {
            when(postcodeLookupService.validatePostCodeForDefendant(any())).thenReturn(false);
            List<String> errors = postcodeValidator.validatePostCodeForDefendant("TEST");

            assertThat(errors).containsOnly("Postcode must be in England or Wales");
        }

        @Test
        void returnTrue_whenInputisFound() {
            when(postcodeLookupService.validatePostCodeForDefendant(any())).thenReturn(true);
            List<String> errors = postcodeValidator.validatePostCodeForDefendant("BA12SS");

            assertThat(errors).isEmpty();
        }
    }

}
