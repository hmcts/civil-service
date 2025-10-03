package uk.gov.hmcts.reform.civil.ras.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

class AttributesTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void builder_AllFields_ReturnsAttributes() {
        // Act
        Attributes attributes = Attributes.builder().substantive("Y").caseId("12345678").jurisdiction("PRIVATELAW").caseType(
            "CARE_SUPERVISION_EPO").primaryLocation("12345").region("North West").contractType("SALARIED").build();

        // Assert
        assertThat(attributes).isNotNull();
        assertThat(attributes.getSubstantive()).isEqualTo("Y");
        assertThat(attributes.getCaseId()).isEqualTo("12345678");
        assertThat(attributes.getJurisdiction()).isEqualTo("PRIVATELAW");
        assertThat(attributes.getCaseType()).isEqualTo("CARE_SUPERVISION_EPO");
        assertThat(attributes.getPrimaryLocation()).isEqualTo("12345");
        assertThat(attributes.getRegion()).isEqualTo("North West");
        assertThat(attributes.getContractType()).isEqualTo("SALARIED");
    }

    @Test
    void noArgsConstructor_CreatesEmptyInstance() {
        // Act
        Attributes attributes = new Attributes();

        // Assert
        assertThat(attributes).isNotNull();
        assertThat(attributes.getSubstantive()).isNull();
        assertThat(attributes.getCaseId()).isNull();
        assertThat(attributes.getJurisdiction()).isNull();
        assertThat(attributes.getCaseType()).isNull();
        assertThat(attributes.getPrimaryLocation()).isNull();
        assertThat(attributes.getRegion()).isNull();
        assertThat(attributes.getContractType()).isNull();
    }

    @Test
    void allArgsConstructor_AllFields_CreatesInstance() {
        // Act
        Attributes attributes = new Attributes(
            "N",
                                               "87654321",
                                               "PUBLICLAW",
                                               "ADOPTION",
                                               "67890",
                                               "South East",
                                               "FEE_PAID"
        );

        // Assert
        assertThat(attributes).isNotNull();
        assertThat(attributes.getSubstantive()).isEqualTo("N");
        assertThat(attributes.getCaseId()).isEqualTo("87654321");
        assertThat(attributes.getJurisdiction()).isEqualTo("PUBLICLAW");
        assertThat(attributes.getCaseType()).isEqualTo("ADOPTION");
        assertThat(attributes.getPrimaryLocation()).isEqualTo("67890");
        assertThat(attributes.getRegion()).isEqualTo("South East");
        assertThat(attributes.getContractType()).isEqualTo("FEE_PAID");
    }

    @Test
    void setters_UpdateFields() {
        // Arrange
        Attributes attributes = new Attributes();

        // Act
        attributes.setSubstantive("Y");
        attributes.setCaseId("11111111");
        attributes.setJurisdiction("CIVIL");
        attributes.setCaseType("FAST_TRACK");
        attributes.setPrimaryLocation("99999");
        attributes.setRegion("London");
        attributes.setContractType("VOLUNTARY");

        // Assert
        assertThat(attributes.getSubstantive()).isEqualTo("Y");
        assertThat(attributes.getCaseId()).isEqualTo("11111111");
        assertThat(attributes.getJurisdiction()).isEqualTo("CIVIL");
        assertThat(attributes.getCaseType()).isEqualTo("FAST_TRACK");
        assertThat(attributes.getPrimaryLocation()).isEqualTo("99999");
        assertThat(attributes.getRegion()).isEqualTo("London");
        assertThat(attributes.getContractType()).isEqualTo("VOLUNTARY");
    }

    @Test
    void equals_SameValues_ReturnsTrue() {
        // Arrange
        Attributes attributes1 = Attributes.builder().substantive("Y").caseId("12345").jurisdiction("CRIMINAL").caseType(
            "MURDER").primaryLocation("LOC001").region("Midlands").contractType("PERMANENT").build();

        Attributes attributes2 = Attributes.builder().substantive("Y").caseId("12345").jurisdiction("CRIMINAL").caseType(
            "MURDER").primaryLocation("LOC001").region("Midlands").contractType("PERMANENT").build();

        // Act & Assert
        assertThat(attributes1).isEqualTo(attributes2);
        assertThat(attributes1.hashCode()).hasSameHashCodeAs(attributes2.hashCode());
    }

    @Test
    void equals_DifferentValues_ReturnsFalse() {
        // Arrange
        Attributes attributes1 = Attributes.builder().substantive("Y").caseId("12345").build();

        Attributes attributes2 = Attributes.builder().substantive("N").caseId("67890").build();

        // Act & Assert
        assertThat(attributes1).isNotEqualTo(attributes2);
    }

    @Test
    void equals_DifferentCaseId_ReturnsFalse() {
        // Arrange
        Attributes attributes1 = Attributes.builder().caseId("12345").jurisdiction("FAMILY").build();

        Attributes attributes2 = Attributes.builder().caseId("67890").jurisdiction("FAMILY").build();

        // Act & Assert
        assertThat(attributes1).isNotEqualTo(attributes2);
    }

    @Test
    void toString_ReturnsFormattedString() {
        // Arrange
        Attributes attributes = Attributes.builder().substantive("Y").caseId("99999").jurisdiction("TRIBUNAL").caseType(
            "EMPLOYMENT").primaryLocation("TRIB001").region("Scotland").contractType("CONSULTANT").build();

        // Act
        String toString = attributes.toString();

        // Assert
        for (String s : Arrays.asList(
            "Attributes",
            "substantive=Y",
            "caseId=99999",
            "jurisdiction=TRIBUNAL",
            "caseType=EMPLOYMENT",
            "primaryLocation=TRIB001",
            "region=Scotland",
            "contractType=CONSULTANT"
        )) {
            assertThat(toString).contains(s);
        }
    }

    @Test
    void builder_PartialFields_ReturnsAttributes() {
        // Act
        Attributes attributesJurisdictionOnly = Attributes.builder().jurisdiction("IMMIGRATION").build();

        Attributes attributesCaseOnly = Attributes.builder().caseId("55555").caseType("ASYLUM").build();

        Attributes attributesLocationOnly = Attributes.builder().primaryLocation("IMM001").region("Wales").build();

        // Assert
        assertThat(attributesJurisdictionOnly).isNotNull();
        assertThat(attributesJurisdictionOnly.getJurisdiction()).isEqualTo("IMMIGRATION");
        assertThat(attributesJurisdictionOnly.getCaseId()).isNull();
        assertThat(attributesJurisdictionOnly.getRegion()).isNull();

        assertThat(attributesCaseOnly).isNotNull();
        assertThat(attributesCaseOnly.getCaseId()).isEqualTo("55555");
        assertThat(attributesCaseOnly.getCaseType()).isEqualTo("ASYLUM");
        assertThat(attributesCaseOnly.getJurisdiction()).isNull();

        assertThat(attributesLocationOnly).isNotNull();
        assertThat(attributesLocationOnly.getPrimaryLocation()).isEqualTo("IMM001");
        assertThat(attributesLocationOnly.getRegion()).isEqualTo("Wales");
        assertThat(attributesLocationOnly.getCaseId()).isNull();
    }

    @Test
    void builder_NullFields_ReturnsAttributes() {
        // Act
        Attributes attributes = Attributes.builder().substantive(null).caseId(null).jurisdiction(null).caseType(null).primaryLocation(
            null).region(null).contractType(null).build();

        // Assert
        assertThat(attributes).isNotNull();
        assertThat(attributes.getSubstantive()).isNull();
        assertThat(attributes.getCaseId()).isNull();
        assertThat(attributes.getJurisdiction()).isNull();
        assertThat(attributes.getCaseType()).isNull();
        assertThat(attributes.getPrimaryLocation()).isNull();
        assertThat(attributes.getRegion()).isNull();
        assertThat(attributes.getContractType()).isNull();
    }

    @Test
    void jsonSerialization_AllFields_SerializesCorrectly() throws Exception {
        // Arrange
        Attributes attributes = Attributes.builder().substantive("Y").caseId("JSON123").jurisdiction("JSON_JURISDICTION").caseType(
            "JSON_TYPE").primaryLocation("JSON_LOC").region("JSON Region").contractType("JSON_CONTRACT").build();

        // Act
        String json = objectMapper.writeValueAsString(attributes);

        // Assert
        for (String s : Arrays.asList(
            "\"substantive\":\"Y\"",
            "\"caseId\":\"JSON123\"",
            "\"jurisdiction\":\"JSON_JURISDICTION\"",
            "\"caseType\":\"JSON_TYPE\"",
            "\"primaryLocation\":\"JSON_LOC\"",
            "\"region\":\"JSON Region\"",
            "\"contractType\":\"JSON_CONTRACT\""
        )) {
            assertThat(json).contains(s);
        }
    }

    @Test
    void jsonDeserialization_ValidJson_DeserializesCorrectly() throws Exception {
        // Arrange
        String json = "{\"substantive\":\"N\",\"caseId\":\"DESER123\",\"jurisdiction\":\"DESER_JUR\","
            + "\"caseType\":\"DESER_TYPE\",\"primaryLocation\":\"DESER_LOC\","
            + "\"region\":\"Deserialize Region\",\"contractType\":\"DESER_CONTRACT\"}";

        // Act
        Attributes attributes = objectMapper.readValue(json, Attributes.class);

        // Assert
        assertThat(attributes).isNotNull();
        assertThat(attributes.getSubstantive()).isEqualTo("N");
        assertThat(attributes.getCaseId()).isEqualTo("DESER123");
        assertThat(attributes.getJurisdiction()).isEqualTo("DESER_JUR");
        assertThat(attributes.getCaseType()).isEqualTo("DESER_TYPE");
        assertThat(attributes.getPrimaryLocation()).isEqualTo("DESER_LOC");
        assertThat(attributes.getRegion()).isEqualTo("Deserialize Region");
        assertThat(attributes.getContractType()).isEqualTo("DESER_CONTRACT");
    }

    @Test
    void jsonDeserialization_NullFields_DeserializesCorrectly() throws Exception {
        // Arrange
        String json = "{\"substantive\":null,\"caseId\":null,\"jurisdiction\":null,\"caseType\":null,\"primaryLocation\":null,\"region\":null,\"contractType\":null}";

        // Act
        Attributes attributes = objectMapper.readValue(json, Attributes.class);

        // Assert
        assertThat(attributes).isNotNull();
        assertThat(attributes.getSubstantive()).isNull();
        assertThat(attributes.getCaseId()).isNull();
        assertThat(attributes.getJurisdiction()).isNull();
        assertThat(attributes.getCaseType()).isNull();
        assertThat(attributes.getPrimaryLocation()).isNull();
        assertThat(attributes.getRegion()).isNull();
        assertThat(attributes.getContractType()).isNull();
    }

    @Test
    void jsonDeserialization_EmptyJson_DeserializesCorrectly() throws Exception {
        // Arrange
        String json = "{}";

        // Act
        Attributes attributes = objectMapper.readValue(json, Attributes.class);

        // Assert
        assertThat(attributes).isNotNull();
        assertThat(attributes.getSubstantive()).isNull();
        assertThat(attributes.getCaseId()).isNull();
        assertThat(attributes.getJurisdiction()).isNull();
        assertThat(attributes.getCaseType()).isNull();
        assertThat(attributes.getPrimaryLocation()).isNull();
        assertThat(attributes.getRegion()).isNull();
        assertThat(attributes.getContractType()).isNull();
    }

    @Test
    void jsonDeserialization_PartialFields_DeserializesCorrectly() throws Exception {
        // Arrange
        String jsonOnlyCase = "{\"caseId\":\"PARTIAL123\",\"caseType\":\"PARTIAL_TYPE\"}";
        String jsonOnlyLocation = "{\"primaryLocation\":\"PARTIAL_LOC\",\"region\":\"Partial Region\"}";

        // Act
        Attributes attributes1 = objectMapper.readValue(jsonOnlyCase, Attributes.class);
        Attributes attributes2 = objectMapper.readValue(jsonOnlyLocation, Attributes.class);

        // Assert
        assertThat(attributes1).isNotNull();
        assertThat(attributes1.getCaseId()).isEqualTo("PARTIAL123");
        assertThat(attributes1.getCaseType()).isEqualTo("PARTIAL_TYPE");
        assertThat(attributes1.getSubstantive()).isNull();
        assertThat(attributes1.getJurisdiction()).isNull();
        assertThat(attributes1.getPrimaryLocation()).isNull();
        assertThat(attributes1.getRegion()).isNull();
        assertThat(attributes1.getContractType()).isNull();

        assertThat(attributes2).isNotNull();
        assertThat(attributes2.getPrimaryLocation()).isEqualTo("PARTIAL_LOC");
        assertThat(attributes2.getRegion()).isEqualTo("Partial Region");
        assertThat(attributes2.getSubstantive()).isNull();
        assertThat(attributes2.getCaseId()).isNull();
        assertThat(attributes2.getJurisdiction()).isNull();
        assertThat(attributes2.getCaseType()).isNull();
        assertThat(attributes2.getContractType()).isNull();
    }

    @Test
    void builder_EmptyStrings_ReturnsAttributes() {
        // Act
        Attributes attributes = Attributes.builder().substantive("").caseId("").jurisdiction("").caseType("").primaryLocation(
            "").region("").contractType("").build();

        // Assert
        assertThat(attributes).isNotNull();
        assertThat(attributes.getSubstantive()).isEmpty();
        assertThat(attributes.getCaseId()).isEmpty();
        assertThat(attributes.getJurisdiction()).isEmpty();
        assertThat(attributes.getCaseType()).isEmpty();
        assertThat(attributes.getPrimaryLocation()).isEmpty();
        assertThat(attributes.getRegion()).isEmpty();
        assertThat(attributes.getContractType()).isEmpty();
    }

    @Test
    void builder_MixedValues_ReturnsAttributes() {
        // Act
        Attributes attributes = Attributes.builder()
            .substantive("true")
            .caseId("1234-5678-90AB-CDEF")
            .jurisdiction("SSCS")
            .caseType("benefit")
            .primaryLocation("Birmingham")
            .region("West Midlands")
            .contractType("TEMP")
            .build();

        // Assert
        assertThat(attributes).isNotNull();
        assertThat(attributes.getSubstantive()).isEqualTo("true");
        assertThat(attributes.getCaseId()).isEqualTo("1234-5678-90AB-CDEF");
        assertThat(attributes.getJurisdiction()).isEqualTo("SSCS");
        assertThat(attributes.getCaseType()).isEqualTo("benefit");
        assertThat(attributes.getPrimaryLocation()).isEqualTo("Birmingham");
        assertThat(attributes.getRegion()).isEqualTo("West Midlands");
        assertThat(attributes.getContractType()).isEqualTo("TEMP");
    }
}
