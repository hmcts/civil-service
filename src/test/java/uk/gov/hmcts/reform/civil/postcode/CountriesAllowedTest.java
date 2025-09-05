package uk.gov.hmcts.reform.civil.postcode;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CountriesAllowedTest {

    private final ObjectMapper objectMapper = new ObjectMapper()
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    @Nested
    class EnumValueTests {

        @Test
        void values_ReturnsAllEnumConstants() {
            // Act
            CountriesAllowed[] values = CountriesAllowed.values();

            // Assert
            assertThat(values)
                .hasSize(4)
                .containsExactly(CountriesAllowed.ENGLAND, CountriesAllowed.SCOTLAND,
                                 CountriesAllowed.WALES, CountriesAllowed.NOT_FOUND);
        }

        @ParameterizedTest
        @EnumSource(CountriesAllowed.class)
        void valueOf_ValidName_ReturnsCorrectEnum(CountriesAllowed expected) {
            // Arrange
            String name = expected.name();

            // Act
            CountriesAllowed result = CountriesAllowed.valueOf(name);

            // Assert
            assertThat(result)
                .isNotNull()
                .isEqualTo(expected);
        }

        @Test
        void valueOf_InvalidName_ThrowsIllegalArgumentException() {
            // Act & Assert
            assertThatThrownBy(() -> CountriesAllowed.valueOf("INVALID_COUNTRY"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("No enum constant");
        }

        @ParameterizedTest
        @EnumSource(CountriesAllowed.class)
        void toString_ReturnsEnumName(CountriesAllowed country) {
            // Act
            String result = country.toString();

            // Assert
            assertThat(result)
                .isNotNull()
                .isEqualTo(country.name());
        }
    }

    @Nested
    class JsonSerializationTests {

        @ParameterizedTest
        @EnumSource(CountriesAllowed.class)
        void serializeToJson_ValidEnum_SerializesAsString(CountriesAllowed country) throws Exception {
            // Act
            String json = objectMapper.writeValueAsString(country);

            // Assert
            assertThat(json)
                .isNotNull()
                .isEqualTo("\"" + country.name() + "\"");
        }

        @ParameterizedTest
        @EnumSource(CountriesAllowed.class)
        void deserializeFromJson_ValidJson_ReturnsCorrectEnum(CountriesAllowed expected) throws Exception {
            // Arrange
            String json = "\"" + expected.name() + "\"";

            // Act
            CountriesAllowed result = objectMapper.readValue(json, CountriesAllowed.class);

            // Assert
            assertThat(result)
                .isNotNull()
                .isEqualTo(expected);
        }

        @ParameterizedTest
        @ValueSource(strings = {"INVALID_COUNTRY", "", "null"})
        void deserializeFromJson_InvalidJson_ThrowsException(String invalidJson) {
            // Act & Assert
            assertThatThrownBy(() -> objectMapper.readValue("\"" + invalidJson + "\"", CountriesAllowed.class))
                .isInstanceOf(Exception.class); // Jackson throws various exceptions (e.g., InvalidFormatException)
        }
    }
}
