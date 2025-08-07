package uk.gov.hmcts.reform.civil.ras.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RoleCategoryTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void values_ReturnsAllEnumValues() {
        // Act
        RoleCategory[] values = RoleCategory.values();

        // Assert
        assertThat(values).hasSize(8).containsExactly(
            RoleCategory.JUDICIAL,
            RoleCategory.LEGAL_OPERATIONS,
            RoleCategory.ADMIN,
            RoleCategory.PROFESSIONAL,
            RoleCategory.CITIZEN,
            RoleCategory.SYSTEM,
            RoleCategory.OTHER_GOV_DEPT,
            RoleCategory.CTSC
        );
    }

    @Test
    void valueOf_ValidName_ReturnsEnumValue() {
        // Act & Assert
        assertThat(RoleCategory.valueOf("JUDICIAL")).isEqualTo(RoleCategory.JUDICIAL);
        assertThat(RoleCategory.valueOf("LEGAL_OPERATIONS")).isEqualTo(RoleCategory.LEGAL_OPERATIONS);
        assertThat(RoleCategory.valueOf("ADMIN")).isEqualTo(RoleCategory.ADMIN);
        assertThat(RoleCategory.valueOf("PROFESSIONAL")).isEqualTo(RoleCategory.PROFESSIONAL);
        assertThat(RoleCategory.valueOf("CITIZEN")).isEqualTo(RoleCategory.CITIZEN);
        assertThat(RoleCategory.valueOf("SYSTEM")).isEqualTo(RoleCategory.SYSTEM);
        assertThat(RoleCategory.valueOf("OTHER_GOV_DEPT")).isEqualTo(RoleCategory.OTHER_GOV_DEPT);
        assertThat(RoleCategory.valueOf("CTSC")).isEqualTo(RoleCategory.CTSC);
    }

    @Test
    void valueOf_InvalidName_ThrowsException() {
        // Act & Assert
        assertThatThrownBy(() -> RoleCategory.valueOf("INVALID")).isInstanceOf(IllegalArgumentException.class).hasMessageContaining(
            "No enum constant").hasMessageContaining("INVALID");
    }

    @Test
    void valueOf_NullName_ThrowsException() {
        // Act & Assert
        assertThatThrownBy(() -> RoleCategory.valueOf(null)).isInstanceOf(NullPointerException.class);
    }

    @ParameterizedTest
    @EnumSource(RoleCategory.class)
    void name_ReturnsCorrectValue(RoleCategory roleCategory) {
        // Act
        String name = roleCategory.name();

        // Assert
        assertThat(name).isNotNull().isIn(
            "JUDICIAL",
            "LEGAL_OPERATIONS",
            "ADMIN",
            "PROFESSIONAL",
            "CITIZEN",
            "SYSTEM",
            "OTHER_GOV_DEPT",
            "CTSC"
        );
    }

    @ParameterizedTest
    @EnumSource(RoleCategory.class)
    void ordinal_ReturnsCorrectValue(RoleCategory roleCategory) {
        // Act
        int ordinal = roleCategory.ordinal();

        // Assert
        assertThat(ordinal).isGreaterThanOrEqualTo(0).isLessThan(8);
    }

    @Test
    void ordinal_SpecificValues_ReturnsExpectedOrdinal() {
        // Act & Assert
        assertThat(RoleCategory.JUDICIAL.ordinal()).isZero();
        assertThat(RoleCategory.LEGAL_OPERATIONS.ordinal()).isOne();
        assertThat(RoleCategory.ADMIN.ordinal()).isEqualTo(2);
        assertThat(RoleCategory.PROFESSIONAL.ordinal()).isEqualTo(3);
        assertThat(RoleCategory.CITIZEN.ordinal()).isEqualTo(4);
        assertThat(RoleCategory.SYSTEM.ordinal()).isEqualTo(5);
        assertThat(RoleCategory.OTHER_GOV_DEPT.ordinal()).isEqualTo(6);
        assertThat(RoleCategory.CTSC.ordinal()).isEqualTo(7);
    }

    @ParameterizedTest
    @EnumSource(RoleCategory.class)
    void toString_ReturnsName(RoleCategory roleCategory) {
        // Act
        String toString = roleCategory.toString();

        // Assert
        assertThat(toString).isEqualTo(roleCategory.name());
    }

    @Test
    void compareTo_ReturnsExpectedOrder() {
        // Act & Assert
        assertThat(RoleCategory.JUDICIAL).isLessThan(RoleCategory.LEGAL_OPERATIONS);
        assertThat(RoleCategory.LEGAL_OPERATIONS).isLessThan(RoleCategory.ADMIN);
        assertThat(RoleCategory.ADMIN).isLessThan(RoleCategory.PROFESSIONAL);
        assertThat(RoleCategory.PROFESSIONAL).isLessThan(RoleCategory.CITIZEN);
        assertThat(RoleCategory.CITIZEN).isLessThan(RoleCategory.SYSTEM);
        assertThat(RoleCategory.SYSTEM).isLessThan(RoleCategory.OTHER_GOV_DEPT);
        assertThat(RoleCategory.OTHER_GOV_DEPT).isLessThan(RoleCategory.CTSC);
        assertThat(RoleCategory.CTSC).isGreaterThan(RoleCategory.JUDICIAL);
        assertThat(RoleCategory.ADMIN).isEqualByComparingTo(RoleCategory.ADMIN);
    }

    @Test
    void equals_SameEnum_ReturnsTrue() {
        // Act & Assert
        assertThat(RoleCategory.JUDICIAL).isEqualTo(RoleCategory.JUDICIAL);
    }

    @Test
    void equals_DifferentEnum_ReturnsFalse() {
        // Act & Assert
        assertThat(RoleCategory.JUDICIAL).isNotEqualTo(RoleCategory.LEGAL_OPERATIONS);
        assertThat(RoleCategory.ADMIN).isNotEqualTo(RoleCategory.PROFESSIONAL);
        assertThat(RoleCategory.CITIZEN).isNotEqualTo(RoleCategory.SYSTEM);
        assertThat(RoleCategory.OTHER_GOV_DEPT).isNotEqualTo(RoleCategory.CTSC);
    }

    @Test
    void hashCode_ConsistentWithEquals() {
        // Act & Assert
        assertThat(RoleCategory.JUDICIAL.hashCode()).hasSameHashCodeAs(RoleCategory.JUDICIAL.hashCode())
            .isNotEqualTo(RoleCategory.ADMIN.hashCode());
    }

    @ParameterizedTest
    @EnumSource(RoleCategory.class)
    void jsonSerialization_SerializesAsString(RoleCategory roleCategory) throws Exception {
        // Act
        String json = objectMapper.writeValueAsString(roleCategory);

        // Assert
        assertThat(json).isEqualTo("\"" + roleCategory.name() + "\"");
    }

    @Test
    void jsonDeserialization_ValidJson_DeserializesCorrectly() throws Exception {
        // Arrange
        String jsonJudicial = "\"JUDICIAL\"";
        String jsonLegalOps = "\"LEGAL_OPERATIONS\"";
        String jsonAdmin = "\"ADMIN\"";
        String jsonProfessional = "\"PROFESSIONAL\"";
        String jsonCitizen = "\"CITIZEN\"";
        String jsonSystem = "\"SYSTEM\"";
        String jsonOtherGov = "\"OTHER_GOV_DEPT\"";
        String jsonCtsc = "\"CTSC\"";

        // Act
        RoleCategory judicial = objectMapper.readValue(jsonJudicial, RoleCategory.class);
        RoleCategory legalOps = objectMapper.readValue(jsonLegalOps, RoleCategory.class);
        RoleCategory admin = objectMapper.readValue(jsonAdmin, RoleCategory.class);
        RoleCategory professional = objectMapper.readValue(jsonProfessional, RoleCategory.class);
        RoleCategory citizen = objectMapper.readValue(jsonCitizen, RoleCategory.class);
        RoleCategory system = objectMapper.readValue(jsonSystem, RoleCategory.class);
        RoleCategory otherGov = objectMapper.readValue(jsonOtherGov, RoleCategory.class);
        RoleCategory ctsc = objectMapper.readValue(jsonCtsc, RoleCategory.class);

        // Assert
        assertThat(judicial).isEqualTo(RoleCategory.JUDICIAL);
        assertThat(legalOps).isEqualTo(RoleCategory.LEGAL_OPERATIONS);
        assertThat(admin).isEqualTo(RoleCategory.ADMIN);
        assertThat(professional).isEqualTo(RoleCategory.PROFESSIONAL);
        assertThat(citizen).isEqualTo(RoleCategory.CITIZEN);
        assertThat(system).isEqualTo(RoleCategory.SYSTEM);
        assertThat(otherGov).isEqualTo(RoleCategory.OTHER_GOV_DEPT);
        assertThat(ctsc).isEqualTo(RoleCategory.CTSC);
    }

    @Test
    void jsonDeserialization_InvalidValue_ThrowsException() {
        // Arrange
        String invalidJson = "\"INVALID_ROLE_CATEGORY\"";

        // Act & Assert
        assertThatThrownBy(() -> objectMapper.readValue(
            invalidJson,
            RoleCategory.class
        )).isInstanceOf(com.fasterxml.jackson.databind.exc.InvalidFormatException.class).hasMessageContaining(
            "not one of the values accepted for Enum class");
    }

    @Test
    void enumType_IsPublic() {
        // Act & Assert
        assertThat(RoleCategory.class.isEnum()).isTrue();
        assertThat(java.lang.reflect.Modifier.isPublic(RoleCategory.class.getModifiers())).isTrue();
    }

    @Test
    void valueOf_CaseSensitive() {
        // Act & Assert
        assertThatThrownBy(() -> RoleCategory.valueOf("judicial")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> RoleCategory.valueOf("Judicial")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> RoleCategory.valueOf("JUDICIAL ")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> RoleCategory.valueOf("legal_operations")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void valueOf_UnderscoreHandling() {
        // Act & Assert
        assertThat(RoleCategory.valueOf("LEGAL_OPERATIONS")).isEqualTo(RoleCategory.LEGAL_OPERATIONS);
        assertThat(RoleCategory.valueOf("OTHER_GOV_DEPT")).isEqualTo(RoleCategory.OTHER_GOV_DEPT);

        // Verify that spaces don't work
        assertThatThrownBy(() -> RoleCategory.valueOf("LEGAL OPERATIONS")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> RoleCategory.valueOf("OTHER GOV DEPT")).isInstanceOf(IllegalArgumentException.class);
    }
}
