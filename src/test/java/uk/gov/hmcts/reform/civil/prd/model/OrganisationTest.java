package uk.gov.hmcts.reform.civil.prd.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OrganisationTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void builder_AllFields_ReturnsOrganisation() {
        // Arrange
        ContactInformation contactInfo = ContactInformation.builder().addressLine1("123 Main Street").postCode(
            "SW1A 1AA").townCity("London").build();
        List<ContactInformation> contactInfoList = Collections.singletonList(contactInfo);

        SuperUser superUser = SuperUser.builder().firstName("John").lastName("Doe").email("john.doe@example.com").build();

        List<String> paymentAccounts = Arrays.asList("PBA1234567", "PBA7654321");

        // Act
        Organisation organisation = Organisation.builder().companyNumber("12345678").companyUrl("http://example.com").contactInformation(
            contactInfoList).name("Example Law Firm").organisationIdentifier("ORG-123").paymentAccount(paymentAccounts).sraId(
            "SRA123456").sraRegulated(true).status("ACTIVE").superUser(superUser).build();

        // Assert
        assertThat(organisation).isNotNull();
        assertThat(organisation.getCompanyNumber()).isEqualTo("12345678");
        assertThat(organisation.getCompanyUrl()).isEqualTo("http://example.com");
        assertThat(organisation.getContactInformation()).hasSize(1)
            .containsExactly(contactInfo);
        assertThat(organisation.getName()).isEqualTo("Example Law Firm");
        assertThat(organisation.getOrganisationIdentifier()).isEqualTo("ORG-123");
        assertThat(organisation.getPaymentAccount()).hasSize(2)
            .containsExactly("PBA1234567", "PBA7654321");
        assertThat(organisation.getSraId()).isEqualTo("SRA123456");
        assertThat(organisation.isSraRegulated()).isTrue();
        assertThat(organisation.getStatus()).isEqualTo("ACTIVE");
        assertThat(organisation.getSuperUser()).isEqualTo(superUser);
    }

    @Test
    void noArgsConstructor_CreatesEmptyInstance() {
        // Act
        Organisation organisation = new Organisation();

        // Assert
        assertThat(organisation).isNotNull();
        assertThat(organisation.getCompanyNumber()).isNull();
        assertThat(organisation.getCompanyUrl()).isNull();
        assertThat(organisation.getContactInformation()).isNull();
        assertThat(organisation.getName()).isNull();
        assertThat(organisation.getOrganisationIdentifier()).isNull();
        assertThat(organisation.getPaymentAccount()).isNull();
        assertThat(organisation.getSraId()).isNull();
        assertThat(organisation.isSraRegulated()).isFalse();
        assertThat(organisation.getStatus()).isNull();
        assertThat(organisation.getSuperUser()).isNull();
    }

    @Test
    void allArgsConstructor_AllFields_CreatesInstance() {
        // Arrange
        ContactInformation contactInfo = ContactInformation.builder().addressLine1("456 High Street").postCode("EH1 1AA").townCity(
            "Edinburgh").build();
        List<ContactInformation> contactInfoList = Collections.singletonList(contactInfo);

        SuperUser superUser = SuperUser.builder().firstName("Jane").lastName("Smith").email("jane.smith@example.com").build();

        List<String> paymentAccounts = Collections.singletonList("PBA9876543");

        // Act
        Organisation organisation = new Organisation(
            "87654321",
            "http://lawfirm.com",
            contactInfoList,
            "Scottish Law Firm",
            "ORG-456",
            paymentAccounts,
            "SRA654321",
            false,
            "PENDING",
            superUser
        );

        // Assert
        assertThat(organisation).isNotNull();
        assertThat(organisation.getCompanyNumber()).isEqualTo("87654321");
        assertThat(organisation.getCompanyUrl()).isEqualTo("http://lawfirm.com");
        assertThat(organisation.getContactInformation()).hasSize(1);
        assertThat(organisation.getName()).isEqualTo("Scottish Law Firm");
        assertThat(organisation.getOrganisationIdentifier()).isEqualTo("ORG-456");
        assertThat(organisation.getPaymentAccount()).hasSize(1);
        assertThat(organisation.getSraId()).isEqualTo("SRA654321");
        assertThat(organisation.isSraRegulated()).isFalse();
        assertThat(organisation.getStatus()).isEqualTo("PENDING");
        assertThat(organisation.getSuperUser()).isEqualTo(superUser);
    }

    @Test
    void setters_UpdateFields() {
        // Arrange
        Organisation organisation = new Organisation();
        ContactInformation contactInfo = ContactInformation.builder().addressLine1("789 Park Lane").build();
        SuperUser superUser = SuperUser.builder().firstName("Bob").lastName("Johnson").build();
        List<String> paymentAccounts = Arrays.asList("PBA1111111", "PBA2222222");

        // Act
        organisation.setCompanyNumber("11111111");
        organisation.setCompanyUrl("http://newlawfirm.com");
        organisation.setContactInformation(Collections.singletonList(contactInfo));
        organisation.setName("New Law Firm");
        organisation.setOrganisationIdentifier("ORG-789");
        organisation.setPaymentAccount(paymentAccounts);
        organisation.setSraId("SRA111111");
        organisation.setSraRegulated(true);
        organisation.setStatus("SUSPENDED");
        organisation.setSuperUser(superUser);

        // Assert
        assertThat(organisation.getCompanyNumber()).isEqualTo("11111111");
        assertThat(organisation.getCompanyUrl()).isEqualTo("http://newlawfirm.com");
        assertThat(organisation.getContactInformation()).hasSize(1);
        assertThat(organisation.getName()).isEqualTo("New Law Firm");
        assertThat(organisation.getOrganisationIdentifier()).isEqualTo("ORG-789");
        assertThat(organisation.getPaymentAccount()).hasSize(2);
        assertThat(organisation.getSraId()).isEqualTo("SRA111111");
        assertThat(organisation.isSraRegulated()).isTrue();
        assertThat(organisation.getStatus()).isEqualTo("SUSPENDED");
        assertThat(organisation.getSuperUser()).isEqualTo(superUser);
    }

    @Test
    void equals_SameValues_ReturnsTrue() {
        // Arrange
        SuperUser superUser = SuperUser.builder().firstName("Test").lastName("User").build();

        Organisation org1 = Organisation.builder().companyNumber("22222222").name("Test Firm").organisationIdentifier(
            "ORG-222").sraRegulated(true).status("ACTIVE").superUser(superUser).build();

        Organisation org2 = Organisation.builder().companyNumber("22222222").name("Test Firm").organisationIdentifier(
            "ORG-222").sraRegulated(true).status("ACTIVE").superUser(superUser).build();

        // Act & Assert
        assertThat(org1).isEqualTo(org2);
        assertThat(org1.hashCode()).hasSameHashCodeAs(org2.hashCode());
    }

    @Test
    void equals_DifferentValues_ReturnsFalse() {
        // Arrange
        Organisation org1 = Organisation.builder().companyNumber("33333333").name("Firm One").organisationIdentifier(
            "ORG-333").build();

        Organisation org2 = Organisation.builder().companyNumber("44444444").name("Firm Two").organisationIdentifier(
            "ORG-444").build();

        // Act & Assert
        assertThat(org1).isNotEqualTo(org2);
    }

    @Test
    void equals_DifferentSraRegulated_ReturnsFalse() {
        // Arrange
        Organisation org1 = Organisation.builder().name("Test Firm").sraRegulated(true).build();

        Organisation org2 = Organisation.builder().name("Test Firm").sraRegulated(false).build();

        // Act & Assert
        assertThat(org1).isNotEqualTo(org2);
    }

    @Test
    void toString_ReturnsFormattedString() {
        // Arrange
        Organisation organisation = Organisation.builder().companyNumber("55555555").name("Test Law Firm").organisationIdentifier(
            "ORG-555").status("ACTIVE").build();

        // Act
        String toString = organisation.toString();

        // Assert
        for (String s : Arrays.asList(
            "Organisation",
            "companyNumber=55555555",
            "name=Test Law Firm",
            "organisationIdentifier=ORG-555",
            "status=ACTIVE"
        )) {
            assertThat(toString).contains(s);
        }
    }

    @Test
    void builder_PartialFields_ReturnsOrganisation() {
        // Act
        Organisation organisation = Organisation.builder().name("Partial Law Firm").organisationIdentifier("ORG-PARTIAL").status(
            "ACTIVE").build();

        // Assert
        assertThat(organisation).isNotNull();
        assertThat(organisation.getName()).isEqualTo("Partial Law Firm");
        assertThat(organisation.getOrganisationIdentifier()).isEqualTo("ORG-PARTIAL");
        assertThat(organisation.getStatus()).isEqualTo("ACTIVE");
        assertThat(organisation.getCompanyNumber()).isNull();
        assertThat(organisation.getCompanyUrl()).isNull();
        assertThat(organisation.getContactInformation()).isNull();
        assertThat(organisation.getPaymentAccount()).isNull();
        assertThat(organisation.getSraId()).isNull();
        assertThat(organisation.isSraRegulated()).isFalse();
        assertThat(organisation.getSuperUser()).isNull();
    }

    @Test
    void builder_EmptyCollections_ReturnsOrganisation() {
        // Act
        Organisation organisation = Organisation.builder().name("Empty Collections Firm").contactInformation(Collections.emptyList()).paymentAccount(
            Collections.emptyList()).build();

        // Assert
        assertThat(organisation).isNotNull();
        assertThat(organisation.getContactInformation()).isEmpty();
        assertThat(organisation.getPaymentAccount()).isEmpty();
    }

    @Test
    void builder_MultipleContactInformation_ReturnsOrganisation() {
        // Arrange
        ContactInformation contactInfo1 = ContactInformation.builder().addressLine1("Head Office").postCode("HO1 1AA").build();
        ContactInformation contactInfo2 = ContactInformation.builder().addressLine1("Branch Office").postCode("BO1 1AA").build();
        List<ContactInformation> contactInfoList = Arrays.asList(contactInfo1, contactInfo2);

        // Act
        Organisation organisation = Organisation.builder().name("Multi-Office Firm").contactInformation(contactInfoList).build();

        // Assert
        assertThat(organisation).isNotNull();
        assertThat(organisation.getContactInformation()).hasSize(2)
            .containsExactly(contactInfo1, contactInfo2);
    }

    @Test
    void jsonSerialization_AllFields_SerializesCorrectly() throws Exception {
        // Arrange
        ContactInformation contactInfo = ContactInformation.builder().addressLine1("JSON Street").postCode("JS1 1ON").build();
        SuperUser superUser = SuperUser.builder().firstName("JSON").lastName("User").email("json@example.com").build();
        Organisation organisation = Organisation.builder()
            .companyNumber("JSON12345")
            .companyUrl("http://json.example.com")
            .contactInformation(Collections.singletonList(contactInfo))
            .name("JSON Law Firm")
            .organisationIdentifier("ORG-JSON")
            .paymentAccount(Arrays.asList("PBA-JSON1", "PBA-JSON2"))
            .sraId("SRA-JSON")
            .sraRegulated(true)
            .status("ACTIVE")
            .superUser(superUser)
            .build();

        // Act
        String json = objectMapper.writeValueAsString(organisation);

        // Assert
        for (String s : Arrays.asList(
            "\"companyNumber\":\"JSON12345\"",
            "\"companyUrl\":\"http://json.example.com\"",
            "\"name\":\"JSON Law Firm\"",
            "\"organisationIdentifier\":\"ORG-JSON\"",
            "\"sraId\":\"SRA-JSON\"",
            "\"sraRegulated\":true",
            "\"status\":\"ACTIVE\"",
            "\"contactInformation\"",
            "\"paymentAccount\"",
            "\"superUser\""
        )) {
            assertThat(json).contains(s);
        }
    }

    @Test
    void jsonDeserialization_ValidJson_DeserializesCorrectly() throws Exception {
        // Arrange
        String json = "{\"companyNumber\":\"12345678\",\"companyUrl\":\"http://example.com\","
            + "\"contactInformation\":[{\"addressLine1\":\"123 Main\",\"postCode\":\"AB1 2CD\"}],"
            + "\"name\":\"Test Firm\",\"organisationIdentifier\":\"ORG-123\",\"paymentAccount\":[\"PBA123\"],"
            + "\"sraId\":\"SRA123\",\"sraRegulated\":true,\"status\":\"ACTIVE\","
            + "\"superUser\":{\"firstName\":\"John\",\"lastName\":\"Doe\",\"email\":\"john@example.com\"}}";

        // Act
        Organisation organisation = objectMapper.readValue(json, Organisation.class);

        // Assert
        assertThat(organisation).isNotNull();
        assertThat(organisation.getCompanyNumber()).isEqualTo("12345678");
        assertThat(organisation.getCompanyUrl()).isEqualTo("http://example.com");
        assertThat(organisation.getName()).isEqualTo("Test Firm");
        assertThat(organisation.getOrganisationIdentifier()).isEqualTo("ORG-123");
        assertThat(organisation.getSraId()).isEqualTo("SRA123");
        assertThat(organisation.isSraRegulated()).isTrue();
        assertThat(organisation.getStatus()).isEqualTo("ACTIVE");
        assertThat(organisation.getContactInformation()).hasSize(1);
        assertThat(organisation.getPaymentAccount()).hasSize(1);
        assertThat(organisation.getSuperUser()).isNotNull();
    }

    @Test
    void jsonDeserialization_NullFields_DeserializesCorrectly() throws Exception {
        // Arrange
        String json = "{\"companyNumber\":null,\"companyUrl\":null,\"contactInformation\":null,"
            + "\"name\":\"Null Fields Firm\",\"organisationIdentifier\":null,\"paymentAccount\":null,"
            + "\"sraId\":null,\"sraRegulated\":false,\"status\":null,\"superUser\":null}";
        // Act
        Organisation organisation = objectMapper.readValue(json, Organisation.class);

        // Assert
        assertThat(organisation).isNotNull();
        assertThat(organisation.getName()).isEqualTo("Null Fields Firm");
        assertThat(organisation.getCompanyNumber()).isNull();
        assertThat(organisation.getCompanyUrl()).isNull();
        assertThat(organisation.getContactInformation()).isNull();
        assertThat(organisation.getOrganisationIdentifier()).isNull();
        assertThat(organisation.getPaymentAccount()).isNull();
        assertThat(organisation.getSraId()).isNull();
        assertThat(organisation.isSraRegulated()).isFalse();
        assertThat(organisation.getStatus()).isNull();
        assertThat(organisation.getSuperUser()).isNull();
    }
}
