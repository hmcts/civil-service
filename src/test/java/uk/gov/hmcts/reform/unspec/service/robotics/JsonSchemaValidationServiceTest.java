package uk.gov.hmcts.reform.unspec.service.robotics;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import uk.gov.hmcts.reform.unspec.service.robotics.exception.JsonSchemaValidationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JsonSchemaValidationServiceTest {

    JsonSchemaValidationService validationService = new JsonSchemaValidationService("sample-json-schema.json");

    @Nested
    class IsValid {

        @ParameterizedTest
        @CsvSource("null,true,false")
        void shouldReturnFalse_whenPayloadIsNotValidJson(String payload) {
            assertFalse(validationService.isValid(payload));
        }

        @ParameterizedTest
        @CsvSource("¯\\_(ツ)_/¯,not a json,1234")
        void shouldThrowJsonSchemaValidationException_whenPayloadHasInvalidJsonToken(String payload) {
            assertThrows(
                JsonSchemaValidationException.class,
                () -> validationService.isValid(payload)
            );
        }

        @Test
        void shouldReturnTrue_whenValidJsonPayload() {
            String payload = "{\"name\": \"Joe\",\"age\": 77}";
            assertTrue(validationService.isValid(payload));
        }
    }

    @Nested
    class Validate {

        @ParameterizedTest
        @CsvSource("true,false")
        void shouldReturnValidationErrors_whenPayloadIsBoolean(String payload) {
            var errors = validationService.validate(payload);

            assertThat(errors).hasSize(1);
            assertThat(errors)
                .isNotEmpty()
                .element(0)
                .extracting("message")
                .isEqualTo("$: boolean found, object expected");
        }

        @Test
        void shouldReturnValidationErrors_whenPayloadIsNotValidJson() {
            String payload = "{\"name\": \"Joe\",\"age\": -1}";
            var errors = validationService.validate(payload);

            assertThat(errors).hasSize(1);
            assertThat(errors)
                .isNotEmpty()
                .element(0)
                .extracting("message")
                .isEqualTo("$.age: must have a minimum value of 0");
        }

        @Test
        void shouldReturnNoErrors_whenPayloadIsValidJson() {
            String payload = "{\"name\": \"Joe\",\"age\": 10}";
            var errors = validationService.validate(payload);

            assertThat(errors).isEmpty();
        }

        @Test
        void shouldThrowJsonSchemaValidationException_whenJsonSchemaFileDoesNotExist() {
            Exception exception = assertThrows(
                JsonSchemaValidationException.class,
                () -> validationService
                    .validate("{}", "not-a-file")
            );
            String expectedMessage = "no file found with the link 'not-a-file'";
            String actualMessage = exception.getMessage();

            assertEquals(expectedMessage, actualMessage);
        }
    }

    @Nested
    class GetJsonSchemaFile {

        @Test
        void shouldReturnDefaultSchemaFile_whenInvoked() {
            assertThat(validationService.getJsonSchemaFile())
                .isEqualTo("sample-json-schema.json");
        }

        @Test
        void shouldReturnSchemaFile_whenInvoked() {
            assertThat(new JsonSchemaValidationService("/another-schema-file.json").getJsonSchemaFile())
                .isEqualTo("/another-schema-file.json");
        }
    }
}
