package uk.gov.hmcts.reform.civil.prd.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DxAddressTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void builder_AllFields_ReturnsDxAddress() {
        // Act
        DxAddress dxAddress = DxAddress.builder()
            .dxExchange("London DX Exchange")
            .dxNumber("DX 123456")
            .build();

        // Assert
        assertThat(dxAddress).isNotNull();
        assertThat(dxAddress.getDxExchange()).isEqualTo("London DX Exchange");
        assertThat(dxAddress.getDxNumber()).isEqualTo("DX 123456");
    }

    @Test
    void noArgsConstructor_CreatesEmptyInstance() {
        // Act
        DxAddress dxAddress = new DxAddress();

        // Assert
        assertThat(dxAddress).isNotNull();
        assertThat(dxAddress.getDxExchange()).isNull();
        assertThat(dxAddress.getDxNumber()).isNull();
    }

    @Test
    void allArgsConstructor_AllFields_CreatesInstance() {
        // Act
        DxAddress dxAddress = new DxAddress("Manchester DX Exchange", "DX 789012");

        // Assert
        assertThat(dxAddress).isNotNull();
        assertThat(dxAddress.getDxExchange()).isEqualTo("Manchester DX Exchange");
        assertThat(dxAddress.getDxNumber()).isEqualTo("DX 789012");
    }

    @Test
    void setters_UpdateFields() {
        // Arrange
        DxAddress dxAddress = new DxAddress();

        // Act
        dxAddress.setDxExchange("Birmingham DX Exchange");
        dxAddress.setDxNumber("DX 345678");

        // Assert
        assertThat(dxAddress.getDxExchange()).isEqualTo("Birmingham DX Exchange");
        assertThat(dxAddress.getDxNumber()).isEqualTo("DX 345678");
    }

    @Test
    void equals_SameValues_ReturnsTrue() {
        // Arrange
        DxAddress dxAddress1 = DxAddress.builder()
            .dxExchange("Cardiff DX Exchange")
            .dxNumber("DX 111222")
            .build();

        DxAddress dxAddress2 = DxAddress.builder()
            .dxExchange("Cardiff DX Exchange")
            .dxNumber("DX 111222")
            .build();

        // Act & Assert
        assertThat(dxAddress1).isEqualTo(dxAddress2);
        assertThat(dxAddress1.hashCode()).hasSameHashCodeAs(dxAddress2.hashCode());
    }

    @Test
    void equals_DifferentValues_ReturnsFalse() {
        // Arrange
        DxAddress dxAddress1 = DxAddress.builder()
            .dxExchange("Edinburgh DX Exchange")
            .dxNumber("DX 333444")
            .build();

        DxAddress dxAddress2 = DxAddress.builder()
            .dxExchange("Glasgow DX Exchange")
            .dxNumber("DX 555666")
            .build();

        // Act & Assert
        assertThat(dxAddress1).isNotEqualTo(dxAddress2);
    }

    @Test
    void equals_DifferentExchangeSameNumber_ReturnsFalse() {
        // Arrange
        DxAddress dxAddress1 = DxAddress.builder()
            .dxExchange("Leeds DX Exchange")
            .dxNumber("DX 777888")
            .build();

        DxAddress dxAddress2 = DxAddress.builder()
            .dxExchange("Sheffield DX Exchange")
            .dxNumber("DX 777888")
            .build();

        // Act & Assert
        assertThat(dxAddress1).isNotEqualTo(dxAddress2);
    }

    @Test
    void equals_SameExchangeDifferentNumber_ReturnsFalse() {
        // Arrange
        DxAddress dxAddress1 = DxAddress.builder()
            .dxExchange("Newcastle DX Exchange")
            .dxNumber("DX 999000")
            .build();

        DxAddress dxAddress2 = DxAddress.builder()
            .dxExchange("Newcastle DX Exchange")
            .dxNumber("DX 111000")
            .build();

        // Act & Assert
        assertThat(dxAddress1).isNotEqualTo(dxAddress2);
    }

    @Test
    void toString_ReturnsFormattedString() {
        // Arrange
        DxAddress dxAddress = DxAddress.builder()
            .dxExchange("Bristol DX Exchange")
            .dxNumber("DX 222333")
            .build();

        // Act
        String toString = dxAddress.toString();

        // Assert
        assertThat(toString).contains("DxAddress")
            .contains("dxExchange=Bristol DX Exchange")
            .contains("dxNumber=DX 222333");
    }

    @Test
    void builder_PartialFields_ReturnsDxAddress() {
        // Act
        DxAddress dxAddressWithExchangeOnly = DxAddress.builder()
            .dxExchange("Liverpool DX Exchange")
            .build();

        DxAddress dxAddressWithNumberOnly = DxAddress.builder()
            .dxNumber("DX 444555")
            .build();

        // Assert
        assertThat(dxAddressWithExchangeOnly).isNotNull();
        assertThat(dxAddressWithExchangeOnly.getDxExchange()).isEqualTo("Liverpool DX Exchange");
        assertThat(dxAddressWithExchangeOnly.getDxNumber()).isNull();

        assertThat(dxAddressWithNumberOnly).isNotNull();
        assertThat(dxAddressWithNumberOnly.getDxExchange()).isNull();
        assertThat(dxAddressWithNumberOnly.getDxNumber()).isEqualTo("DX 444555");
    }

    @Test
    void builder_NullFields_ReturnsDxAddress() {
        // Act
        DxAddress dxAddress = DxAddress.builder()
            .dxExchange(null)
            .dxNumber(null)
            .build();

        // Assert
        assertThat(dxAddress).isNotNull();
        assertThat(dxAddress.getDxExchange()).isNull();
        assertThat(dxAddress.getDxNumber()).isNull();
    }

    @Test
    void jsonSerialization_AllFields_SerializesCorrectly() throws Exception {
        // Arrange
        DxAddress dxAddress = DxAddress.builder()
            .dxExchange("Oxford DX Exchange")
            .dxNumber("DX 666777")
            .build();

        // Act
        String json = objectMapper.writeValueAsString(dxAddress);

        // Assert
        assertThat(json).contains("\"dxExchange\":\"Oxford DX Exchange\"")
            .contains("\"dxNumber\":\"DX 666777\"");
    }

    @Test
    void jsonDeserialization_ValidJson_DeserializesCorrectly() throws Exception {
        // Arrange
        String json = "{\"dxExchange\":\"Cambridge DX Exchange\",\"dxNumber\":\"DX 888999\"}";

        // Act
        DxAddress dxAddress = objectMapper.readValue(json, DxAddress.class);

        // Assert
        assertThat(dxAddress).isNotNull();
        assertThat(dxAddress.getDxExchange()).isEqualTo("Cambridge DX Exchange");
        assertThat(dxAddress.getDxNumber()).isEqualTo("DX 888999");
    }

    @Test
    void jsonDeserialization_NullFields_DeserializesCorrectly() throws Exception {
        // Arrange
        String json = "{\"dxExchange\":null,\"dxNumber\":null}";

        // Act
        DxAddress dxAddress = objectMapper.readValue(json, DxAddress.class);

        // Assert
        assertThat(dxAddress).isNotNull();
        assertThat(dxAddress.getDxExchange()).isNull();
        assertThat(dxAddress.getDxNumber()).isNull();
    }

    @Test
    void jsonDeserialization_MissingFields_DeserializesCorrectly() throws Exception {
        // Arrange
        String json = "{}";

        // Act
        DxAddress dxAddress = objectMapper.readValue(json, DxAddress.class);

        // Assert
        assertThat(dxAddress).isNotNull();
        assertThat(dxAddress.getDxExchange()).isNull();
        assertThat(dxAddress.getDxNumber()).isNull();
    }

    @Test
    void jsonDeserialization_PartialFields_DeserializesCorrectly() throws Exception {
        // Arrange
        String jsonWithExchangeOnly = "{\"dxExchange\":\"Norwich DX Exchange\"}";
        String jsonWithNumberOnly = "{\"dxNumber\":\"DX 123321\"}";

        // Act
        DxAddress dxAddress1 = objectMapper.readValue(jsonWithExchangeOnly, DxAddress.class);
        DxAddress dxAddress2 = objectMapper.readValue(jsonWithNumberOnly, DxAddress.class);

        // Assert
        assertThat(dxAddress1).isNotNull();
        assertThat(dxAddress1.getDxExchange()).isEqualTo("Norwich DX Exchange");
        assertThat(dxAddress1.getDxNumber()).isNull();

        assertThat(dxAddress2).isNotNull();
        assertThat(dxAddress2.getDxExchange()).isNull();
        assertThat(dxAddress2.getDxNumber()).isEqualTo("DX 123321");
    }
}
