package uk.gov.hmcts.reform.civil.prd.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ContactInformationTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void builder_AllFields_ReturnsContactInformation() {
        // Arrange
        DxAddress dxAddress = DxAddress.builder().dxNumber("DX123").dxExchange("Exchange1").build();
        List<DxAddress> dxAddresses = Collections.singletonList(dxAddress);

        // Act
        ContactInformation contactInfo = ContactInformation.builder().addressLine1("123 Main Street").addressLine2(
            "Suite 100").addressLine3("Building A").country("United Kingdom").county("Greater London").dxAddress(
            dxAddresses).postCode("SW1A 1AA").townCity("London").build();

        // Assert
        assertThat(contactInfo).isNotNull();
        assertThat(contactInfo.getAddressLine1()).isEqualTo("123 Main Street");
        assertThat(contactInfo.getAddressLine2()).isEqualTo("Suite 100");
        assertThat(contactInfo.getAddressLine3()).isEqualTo("Building A");
        assertThat(contactInfo.getCountry()).isEqualTo("United Kingdom");
        assertThat(contactInfo.getCounty()).isEqualTo("Greater London");
        assertThat(contactInfo.getDxAddress()).hasSize(1)
            .containsExactly(dxAddress);
        assertThat(contactInfo.getPostCode()).isEqualTo("SW1A 1AA");
        assertThat(contactInfo.getTownCity()).isEqualTo("London");
    }

    @Test
    void noArgsConstructor_CreatesEmptyInstance() {
        // Act
        ContactInformation contactInfo = new ContactInformation();

        // Assert
        assertThat(contactInfo).isNotNull();
        assertThat(contactInfo.getAddressLine1()).isNull();
        assertThat(contactInfo.getAddressLine2()).isNull();
        assertThat(contactInfo.getAddressLine3()).isNull();
        assertThat(contactInfo.getCountry()).isNull();
        assertThat(contactInfo.getCounty()).isNull();
        assertThat(contactInfo.getDxAddress()).isNull();
        assertThat(contactInfo.getPostCode()).isNull();
        assertThat(contactInfo.getTownCity()).isNull();
    }

    @Test
    void allArgsConstructor_AllFields_CreatesInstance() {
        // Arrange
        DxAddress dxAddress = DxAddress.builder().dxNumber("DX456").dxExchange("Exchange2").build();
        List<DxAddress> dxAddresses = Collections.singletonList(dxAddress);

        // Act
        ContactInformation contactInfo = new ContactInformation(
            "456 High Street",
            "Floor 2",
            "Block B",
            "Scotland",
            "Edinburgh",
            dxAddresses,
            "EH1 1AA",
            "Edinburgh"
        );

        // Assert
        assertThat(contactInfo).isNotNull();
        assertThat(contactInfo.getAddressLine1()).isEqualTo("456 High Street");
        assertThat(contactInfo.getAddressLine2()).isEqualTo("Floor 2");
        assertThat(contactInfo.getAddressLine3()).isEqualTo("Block B");
        assertThat(contactInfo.getCountry()).isEqualTo("Scotland");
        assertThat(contactInfo.getCounty()).isEqualTo("Edinburgh");
        assertThat(contactInfo.getDxAddress()).hasSize(1)
            .containsExactly(dxAddress);
        assertThat(contactInfo.getPostCode()).isEqualTo("EH1 1AA");
        assertThat(contactInfo.getTownCity()).isEqualTo("Edinburgh");
    }

    @Test
    void setters_UpdateFields() {
        // Arrange
        ContactInformation contactInfo = new ContactInformation();
        DxAddress dxAddress = DxAddress.builder().dxNumber("DX789").dxExchange("Exchange3").build();
        List<DxAddress> dxAddresses = Collections.singletonList(dxAddress);

        // Act
        contactInfo.setAddressLine1("789 Park Avenue");
        contactInfo.setAddressLine2("Apt 5");
        contactInfo.setAddressLine3("Tower C");
        contactInfo.setCountry("Wales");
        contactInfo.setCounty("Cardiff");
        contactInfo.setDxAddress(dxAddresses);
        contactInfo.setPostCode("CF10 1AA");
        contactInfo.setTownCity("Cardiff");

        // Assert
        assertThat(contactInfo.getAddressLine1()).isEqualTo("789 Park Avenue");
        assertThat(contactInfo.getAddressLine2()).isEqualTo("Apt 5");
        assertThat(contactInfo.getAddressLine3()).isEqualTo("Tower C");
        assertThat(contactInfo.getCountry()).isEqualTo("Wales");
        assertThat(contactInfo.getCounty()).isEqualTo("Cardiff");
        assertThat(contactInfo.getDxAddress()).hasSize(1)
            .containsExactly(dxAddress);
        assertThat(contactInfo.getPostCode()).isEqualTo("CF10 1AA");
        assertThat(contactInfo.getTownCity()).isEqualTo("Cardiff");
    }

    @Test
    void equals_SameValues_ReturnsTrue() {
        // Arrange
        DxAddress dxAddress = DxAddress.builder().dxNumber("DX111").dxExchange("Exchange4").build();
        List<DxAddress> dxAddresses = Collections.singletonList(dxAddress);

        ContactInformation contactInfo1 = ContactInformation.builder().addressLine1("111 Queen Street").addressLine2(
            "Office 3").addressLine3("Building D").country("England").county("Manchester").dxAddress(dxAddresses).postCode(
            "M1 1AA").townCity("Manchester").build();

        ContactInformation contactInfo2 = ContactInformation.builder().addressLine1("111 Queen Street").addressLine2(
            "Office 3").addressLine3("Building D").country("England").county("Manchester").dxAddress(dxAddresses).postCode(
            "M1 1AA").townCity("Manchester").build();

        // Act & Assert
        assertThat(contactInfo1).isEqualTo(contactInfo2);
        assertThat(contactInfo1.hashCode()).hasSameHashCodeAs(contactInfo2.hashCode());
    }

    @Test
    void equals_DifferentValues_ReturnsFalse() {
        // Arrange
        ContactInformation contactInfo1 = ContactInformation.builder().addressLine1("111 Queen Street").postCode(
            "M1 1AA").build();

        ContactInformation contactInfo2 = ContactInformation.builder().addressLine1("222 King Street").postCode("M2 2BB").build();

        // Act & Assert
        assertThat(contactInfo1).isNotEqualTo(contactInfo2);
    }

    @Test
    void toString_ReturnsFormattedString() {
        // Arrange
        ContactInformation contactInfo = ContactInformation.builder().addressLine1("Test Street").postCode("TE1 1ST").townCity(
            "Test City").build();

        // Act
        String toString = contactInfo.toString();

        // Assert
        assertThat(toString).contains("ContactInformation")
            .contains("addressLine1=Test Street")
            .contains("postCode=TE1 1ST")
            .contains("townCity=Test City");
    }

    @Test
    void toBuilder_CreatesNewBuilderWithSameValues() {
        // Arrange
        DxAddress dxAddress = DxAddress.builder().dxNumber("DX999").dxExchange("Exchange5").build();
        List<DxAddress> dxAddresses = Collections.singletonList(dxAddress);

        ContactInformation original = ContactInformation.builder().addressLine1("999 Original Street").addressLine2(
            "Floor 9").addressLine3("Block Z").country("Northern Ireland").county("Belfast").dxAddress(dxAddresses).postCode(
            "BT1 1AA").townCity("Belfast").build();

        // Act
        ContactInformation copy = original.toBuilder().addressLine1("999 Modified Street").build();

        // Assert
        assertThat(copy).isNotNull();
        assertThat(copy.getAddressLine1()).isEqualTo("999 Modified Street");
        assertThat(copy.getAddressLine2()).isEqualTo("Floor 9");
        assertThat(copy.getAddressLine3()).isEqualTo("Block Z");
        assertThat(copy.getCountry()).isEqualTo("Northern Ireland");
        assertThat(copy.getCounty()).isEqualTo("Belfast");
        assertThat(copy.getDxAddress()).isEqualTo(dxAddresses);
        assertThat(copy.getPostCode()).isEqualTo("BT1 1AA");
        assertThat(copy.getTownCity()).isEqualTo("Belfast");
        assertThat(original.getAddressLine1()).isEqualTo("999 Original Street");
    }

    @Test
    void builder_PartialFields_ReturnsContactInformation() {
        // Act
        ContactInformation contactInfo = ContactInformation.builder().addressLine1("Partial Address").postCode("PA1 1AA").build();

        // Assert
        assertThat(contactInfo).isNotNull();
        assertThat(contactInfo.getAddressLine1()).isEqualTo("Partial Address");
        assertThat(contactInfo.getAddressLine2()).isNull();
        assertThat(contactInfo.getAddressLine3()).isNull();
        assertThat(contactInfo.getCountry()).isNull();
        assertThat(contactInfo.getCounty()).isNull();
        assertThat(contactInfo.getDxAddress()).isNull();
        assertThat(contactInfo.getPostCode()).isEqualTo("PA1 1AA");
        assertThat(contactInfo.getTownCity()).isNull();
    }

    @Test
    void builder_EmptyDxAddressList_ReturnsContactInformation() {
        // Act
        ContactInformation contactInfo = ContactInformation.builder().addressLine1("No DX Address").dxAddress(
            Collections.emptyList()).build();

        // Assert
        assertThat(contactInfo).isNotNull();
        assertThat(contactInfo.getDxAddress()).isEmpty();
    }

    @Test
    void builder_MultipleDxAddresses_ReturnsContactInformation() {
        // Arrange
        DxAddress dxAddress1 = DxAddress.builder().dxNumber("DX001").dxExchange("Exchange1").build();
        DxAddress dxAddress2 = DxAddress.builder().dxNumber("DX002").dxExchange("Exchange2").build();
        List<DxAddress> dxAddresses = Arrays.asList(dxAddress1, dxAddress2);

        // Act
        ContactInformation contactInfo = ContactInformation.builder().addressLine1("Multiple DX").dxAddress(dxAddresses).build();

        // Assert
        assertThat(contactInfo).isNotNull();
        assertThat(contactInfo.getDxAddress()).hasSize(2)
            .containsExactly(dxAddress1, dxAddress2);
    }

    @Test
    void jsonSerialization_AllFields_SerializesCorrectly() throws Exception {
        // Arrange
        DxAddress dxAddress = DxAddress.builder().dxNumber("DX123").dxExchange("Exchange1").build();
        ContactInformation contactInfo = ContactInformation.builder().addressLine1("123 Main Street").addressLine2(
            "Suite 100").addressLine3("Building A").country("United Kingdom").county("Greater London").dxAddress(
            Collections.singletonList(dxAddress)).postCode("SW1A 1AA").townCity("London").build();

        // Act
        String json = objectMapper.writeValueAsString(contactInfo);

        // Assert
        for (String s : Arrays.asList(
            "\"addressLine1\":\"123 Main Street\"",
            "\"addressLine2\":\"Suite 100\"",
            "\"addressLine3\":\"Building A\"",
            "\"country\":\"United Kingdom\"",
            "\"county\":\"Greater London\"",
            "\"dxAddress\"",
            "\"postCode\":\"SW1A 1AA\"",
            "\"townCity\":\"London\""
        )) {
            assertThat(json).contains(s);
        }
    }

    @Test
    void jsonDeserialization_ValidJson_DeserializesCorrectly() throws Exception {
        // Arrange
        String json = "{\"addressLine1\":\"123 Main Street\",\"addressLine2\":\"Suite 100\","
            + "\"addressLine3\":\"Building A\",\"country\":\"United Kingdom\","
            + "\"county\":\"Greater London\",\"dxAddress\":[{\"dxNumber\":\"DX123\","
            + "\"dxExchange\":\"Exchange1\"}],\"postCode\":\"SW1A 1AA\",\"townCity\":\"London\"}";

        // Act
        ContactInformation contactInfo = objectMapper.readValue(json, ContactInformation.class);

        // Assert
        assertThat(contactInfo).isNotNull();
        assertThat(contactInfo.getAddressLine1()).isEqualTo("123 Main Street");
        assertThat(contactInfo.getAddressLine2()).isEqualTo("Suite 100");
        assertThat(contactInfo.getAddressLine3()).isEqualTo("Building A");
        assertThat(contactInfo.getCountry()).isEqualTo("United Kingdom");
        assertThat(contactInfo.getCounty()).isEqualTo("Greater London");
        assertThat(contactInfo.getDxAddress()).hasSize(1);
        assertThat(contactInfo.getPostCode()).isEqualTo("SW1A 1AA");
        assertThat(contactInfo.getTownCity()).isEqualTo("London");
    }

    @Test
    void jsonDeserialization_NullFields_DeserializesCorrectly() throws Exception {
        // Arrange
        String json = "{\"addressLine1\":null,\"addressLine2\":null,\"addressLine3\":null,\"country\":null,\"county\":null,\"dxAddress\":null,\"postCode\":null,\"townCity\":null}";

        // Act
        ContactInformation contactInfo = objectMapper.readValue(json, ContactInformation.class);

        // Assert
        assertThat(contactInfo).isNotNull();
        assertThat(contactInfo.getAddressLine1()).isNull();
        assertThat(contactInfo.getAddressLine2()).isNull();
        assertThat(contactInfo.getAddressLine3()).isNull();
        assertThat(contactInfo.getCountry()).isNull();
        assertThat(contactInfo.getCounty()).isNull();
        assertThat(contactInfo.getDxAddress()).isNull();
        assertThat(contactInfo.getPostCode()).isNull();
        assertThat(contactInfo.getTownCity()).isNull();
    }
}
