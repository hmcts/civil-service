package uk.gov.hmcts.reform.civil.ras.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GrantTypeTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void values_ReturnsAllEnumValues() {
        // Act
        GrantType[] values = GrantType.values();

        // Assert
        assertThat(values).hasSize(5).containsExactly(
            GrantType.BASIC,
            GrantType.SPECIFIC,
            GrantType.STANDARD,
            GrantType.CHALLENGED,
            GrantType.EXCLUDED
        );
    }

    @Test
    void valueOf_ValidName_ReturnsEnumValue() {
        // Act & Assert
        assertThat(GrantType.valueOf("BASIC")).isEqualTo(GrantType.BASIC);
        assertThat(GrantType.valueOf("SPECIFIC")).isEqualTo(GrantType.SPECIFIC);
        assertThat(GrantType.valueOf("STANDARD")).isEqualTo(GrantType.STANDARD);
        assertThat(GrantType.valueOf("CHALLENGED")).isEqualTo(GrantType.CHALLENGED);
        assertThat(GrantType.valueOf("EXCLUDED")).isEqualTo(GrantType.EXCLUDED);
    }

    @Test
    void valueOf_InvalidName_ThrowsException() {
        // Act & Assert
        assertThatThrownBy(() -> GrantType.valueOf("INVALID")).isInstanceOf(IllegalArgumentException.class).hasMessageContaining(
            "No enum constant").hasMessageContaining("INVALID");
    }

    @Test
    void valueOf_NullName_ThrowsException() {
        // Act & Assert
        assertThatThrownBy(() -> GrantType.valueOf(null)).isInstanceOf(NullPointerException.class);
    }

    @ParameterizedTest
    @EnumSource(GrantType.class)
    void name_ReturnsCorrectValue(GrantType grantType) {
        // Act
        String name = grantType.name();

        // Assert
        assertThat(name).isNotNull().isIn("BASIC", "SPECIFIC", "STANDARD", "CHALLENGED", "EXCLUDED");
    }

    @ParameterizedTest
    @EnumSource(GrantType.class)
    void ordinal_ReturnsCorrectValue(GrantType grantType) {
        // Act
        int ordinal = grantType.ordinal();

        // Assert
        assertThat(ordinal).isGreaterThanOrEqualTo(0).isLessThan(5);
    }

    @Test
    void ordinal_SpecificValues_ReturnsExpectedOrdinal() {
        // Act & Assert
        assertThat(GrantType.BASIC.ordinal()).isZero();
        assertThat(GrantType.SPECIFIC.ordinal()).isOne();
        assertThat(GrantType.STANDARD.ordinal()).isEqualTo(2);
        assertThat(GrantType.CHALLENGED.ordinal()).isEqualTo(3);
        assertThat(GrantType.EXCLUDED.ordinal()).isEqualTo(4);
    }

    @ParameterizedTest
    @EnumSource(GrantType.class)
    void toString_ReturnsName(GrantType grantType) {
        // Act
        String toString = grantType.toString();

        // Assert
        assertThat(toString).isEqualTo(grantType.name());
    }

    @Test
    void compareTo_ReturnsExpectedOrder() {
        // Act & Assert
        assertThat(GrantType.BASIC).isLessThan(GrantType.SPECIFIC);
        assertThat(GrantType.SPECIFIC).isLessThan(GrantType.STANDARD);
        assertThat(GrantType.STANDARD).isLessThan(GrantType.CHALLENGED);
        assertThat(GrantType.CHALLENGED).isLessThan(GrantType.EXCLUDED);
        assertThat(GrantType.EXCLUDED).isGreaterThan(GrantType.BASIC);
        assertThat(GrantType.STANDARD).isEqualByComparingTo(GrantType.STANDARD);
    }

    @Test
    void equals_DifferentEnum_ReturnsFalse() {
        // Act & Assert
        assertThat(GrantType.EXCLUDED).isNotEqualTo(GrantType.BASIC);
    }

    @Test
    void hashCode_ConsistentWithEquals() {
        // Act & Assert
        assertThat(GrantType.BASIC.hashCode()).hasSameHashCodeAs(GrantType.BASIC.hashCode())
            .isNotEqualTo(GrantType.SPECIFIC.hashCode());
    }

    @ParameterizedTest
    @EnumSource(GrantType.class)
    void jsonSerialization_SerializesAsString(GrantType grantType) throws Exception {
        // Act
        String json = objectMapper.writeValueAsString(grantType);

        // Assert
        assertThat(json).isEqualTo("\"" + grantType.name() + "\"");
    }

    @Test
    void jsonDeserialization_ValidJson_DeserializesCorrectly() throws Exception {
        // Arrange
        String jsonBasic = "\"BASIC\"";
        String jsonSpecific = "\"SPECIFIC\"";
        String jsonStandard = "\"STANDARD\"";
        String jsonChallenged = "\"CHALLENGED\"";
        String jsonExcluded = "\"EXCLUDED\"";

        // Act
        GrantType basic = objectMapper.readValue(jsonBasic, GrantType.class);
        GrantType specific = objectMapper.readValue(jsonSpecific, GrantType.class);
        GrantType standard = objectMapper.readValue(jsonStandard, GrantType.class);
        GrantType challenged = objectMapper.readValue(jsonChallenged, GrantType.class);
        GrantType excluded = objectMapper.readValue(jsonExcluded, GrantType.class);

        // Assert
        assertThat(basic).isEqualTo(GrantType.BASIC);
        assertThat(specific).isEqualTo(GrantType.SPECIFIC);
        assertThat(standard).isEqualTo(GrantType.STANDARD);
        assertThat(challenged).isEqualTo(GrantType.CHALLENGED);
        assertThat(excluded).isEqualTo(GrantType.EXCLUDED);
    }

    @Test
    void jsonDeserialization_InvalidValue_ThrowsException() {
        // Arrange
        String invalidJson = "\"INVALID_GRANT_TYPE\"";

        // Act & Assert
        assertThatThrownBy(() -> objectMapper.readValue(
            invalidJson,
            GrantType.class
        )).isInstanceOf(com.fasterxml.jackson.databind.exc.InvalidFormatException.class).hasMessageContaining(
            "not one of the values accepted for Enum class");
    }

    @Test
    void enumType_IsPublic() {
        // Act & Assert
        assertThat(GrantType.class.isEnum()).isTrue();
        assertThat(java.lang.reflect.Modifier.isPublic(GrantType.class.getModifiers())).isTrue();
    }

    @Test
    void valueOf_CaseSensitive() {
        // Act & Assert
        assertThatThrownBy(() -> GrantType.valueOf("basic")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> GrantType.valueOf("Basic")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> GrantType.valueOf("BASIC ")).isInstanceOf(IllegalArgumentException.class);
    }
}
