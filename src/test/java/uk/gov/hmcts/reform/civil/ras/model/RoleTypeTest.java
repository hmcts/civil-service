package uk.gov.hmcts.reform.civil.ras.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RoleTypeTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void values_ReturnsAllEnumValues() {
        // Act
        RoleType[] values = RoleType.values();

        // Assert
        assertThat(values).hasSize(2)
            .containsExactly(RoleType.CASE, RoleType.ORGANISATION);
    }

    @Test
    void valueOf_ValidName_ReturnsEnumValue() {
        // Act & Assert
        assertThat(RoleType.valueOf("CASE")).isEqualTo(RoleType.CASE);
        assertThat(RoleType.valueOf("ORGANISATION")).isEqualTo(RoleType.ORGANISATION);
    }

    @Test
    void valueOf_InvalidName_ThrowsException() {
        // Act & Assert
        assertThatThrownBy(() -> RoleType.valueOf("INVALID"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("No enum constant")
            .hasMessageContaining("INVALID");
    }

    @Test
    void valueOf_NullName_ThrowsException() {
        // Act & Assert
        assertThatThrownBy(() -> RoleType.valueOf(null))
            .isInstanceOf(NullPointerException.class);
    }

    @ParameterizedTest
    @EnumSource(RoleType.class)
    void name_ReturnsCorrectValue(RoleType roleType) {
        // Act
        String name = roleType.name();

        // Assert
        assertThat(name).isNotNull()
            .isIn("CASE", "ORGANISATION");
    }

    @ParameterizedTest
    @EnumSource(RoleType.class)
    void ordinal_ReturnsCorrectValue(RoleType roleType) {
        // Act
        int ordinal = roleType.ordinal();

        // Assert
        assertThat(ordinal).isGreaterThanOrEqualTo(0)
            .isLessThan(2);
    }

    @Test
    void ordinal_SpecificValues_ReturnsExpectedOrdinal() {
        // Act & Assert
        assertThat(RoleType.CASE.ordinal()).isZero();
        assertThat(RoleType.ORGANISATION.ordinal()).isOne();
    }

    @ParameterizedTest
    @EnumSource(RoleType.class)
    void toString_ReturnsName(RoleType roleType) {
        // Act
        String toString = roleType.toString();

        // Assert
        assertThat(toString).isEqualTo(roleType.name());
    }

    @Test
    void compareTo_ReturnsExpectedOrder() {
        // Act & Assert
        assertThat(RoleType.CASE).isLessThan(RoleType.ORGANISATION);
        assertThat(RoleType.ORGANISATION).isGreaterThan(RoleType.CASE);
        assertThat(RoleType.CASE).isEqualByComparingTo(RoleType.CASE);
        assertThat(RoleType.ORGANISATION).isEqualByComparingTo(RoleType.ORGANISATION);
    }

    @Test
    void equals_SameEnum_ReturnsTrue() {
        // Act & Assert
        assertThat(RoleType.CASE).isEqualTo(RoleType.CASE);
    }

    @Test
    void equals_DifferentEnum_ReturnsFalse() {
        // Act & Assert
        assertThat(RoleType.CASE).isNotEqualTo(RoleType.ORGANISATION);
        assertThat(RoleType.ORGANISATION).isNotEqualTo(RoleType.CASE);
    }

    @Test
    void equals_Null_ReturnsFalse() {
        // Act & Assert
        assertThat(RoleType.CASE).isNotNull();
        assertThat(RoleType.ORGANISATION).isNotNull();
    }

    @Test
    void hashCode_ConsistentWithEquals() {
        // Act & Assert
        assertThat(RoleType.CASE.hashCode()).hasSameHashCodeAs(RoleType.CASE.hashCode());
        assertThat(RoleType.ORGANISATION.hashCode()).hasSameHashCodeAs(RoleType.ORGANISATION.hashCode());
        assertThat(RoleType.CASE.hashCode()).isNotEqualTo(RoleType.ORGANISATION.hashCode());
    }

    @ParameterizedTest
    @EnumSource(RoleType.class)
    void jsonSerialization_SerializesAsString(RoleType roleType) throws Exception {
        // Act
        String json = objectMapper.writeValueAsString(roleType);

        // Assert
        assertThat(json).isEqualTo("\"" + roleType.name() + "\"");
    }

    @Test
    void jsonSerialization_AllValues_SerializesCorrectly() throws Exception {
        // Act
        String jsonCase = objectMapper.writeValueAsString(RoleType.CASE);
        String jsonOrganisation = objectMapper.writeValueAsString(RoleType.ORGANISATION);

        // Assert
        assertThat(jsonCase).isEqualTo("\"CASE\"");
        assertThat(jsonOrganisation).isEqualTo("\"ORGANISATION\"");
    }

    @Test
    void jsonDeserialization_ValidJson_DeserializesCorrectly() throws Exception {
        // Arrange
        String jsonCase = "\"CASE\"";
        String jsonOrganisation = "\"ORGANISATION\"";

        // Act
        RoleType caseType = objectMapper.readValue(jsonCase, RoleType.class);
        RoleType organisationType = objectMapper.readValue(jsonOrganisation, RoleType.class);

        // Assert
        assertThat(caseType).isEqualTo(RoleType.CASE);
        assertThat(organisationType).isEqualTo(RoleType.ORGANISATION);
    }

    @Test
    void jsonDeserialization_InvalidValue_ThrowsException() {
        // Arrange
        String invalidJson = "\"INVALID_ROLE_TYPE\"";

        // Act & Assert
        assertThatThrownBy(() -> objectMapper.readValue(invalidJson, RoleType.class))
            .isInstanceOf(com.fasterxml.jackson.databind.exc.InvalidFormatException.class)
            .hasMessageContaining("not one of the values accepted for Enum class");
    }

    @Test
    void jsonDeserialization_NullValue_ReturnsNull() throws Exception {
        // Arrange
        String nullJson = "null";

        // Act
        RoleType result = objectMapper.readValue(nullJson, RoleType.class);

        // Assert
        assertThat(result).isNull();
    }

    @Test
    void enumType_IsPublic() {
        // Act & Assert
        assertThat(RoleType.class.isEnum()).isTrue();
        assertThat(java.lang.reflect.Modifier.isPublic(RoleType.class.getModifiers())).isTrue();
    }

    @Test
    void valueOf_CaseSensitive() {
        // Act & Assert
        assertThatThrownBy(() -> RoleType.valueOf("case"))
            .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> RoleType.valueOf("Case"))
            .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> RoleType.valueOf("CASE "))
            .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> RoleType.valueOf("organisation"))
            .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> RoleType.valueOf("Organisation"))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void valueOf_AlternativeSpellings_ThrowsException() {
        // Test British vs American spelling
        assertThatThrownBy(() -> RoleType.valueOf("ORGANIZATION"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("No enum constant");
    }

    @Test
    void enumDeclarationOrder_MatchesOrdinals() {
        // This test verifies that the enum constants are declared in the expected order
        RoleType[] values = RoleType.values();

        // Assert
        assertThat(values[0]).isEqualTo(RoleType.CASE);
        assertThat(values[1]).isEqualTo(RoleType.ORGANISATION);
    }
}
