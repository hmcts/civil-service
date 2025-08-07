package uk.gov.hmcts.reform.civil.prd.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

class SuperUserTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void builder_AllFields_ReturnsSuperUser() {
        // Act
        SuperUser superUser = SuperUser.builder()
            .email("john.doe@example.com")
            .firstName("John")
            .lastName("Doe")
            .build();

        // Assert
        assertThat(superUser).isNotNull();
        assertThat(superUser.getEmail()).isEqualTo("john.doe@example.com");
        assertThat(superUser.getFirstName()).isEqualTo("John");
        assertThat(superUser.getLastName()).isEqualTo("Doe");
    }

    @Test
    void noArgsConstructor_CreatesEmptyInstance() {
        // Act
        SuperUser superUser = new SuperUser();

        // Assert
        assertThat(superUser).isNotNull();
        assertThat(superUser.getEmail()).isNull();
        assertThat(superUser.getFirstName()).isNull();
        assertThat(superUser.getLastName()).isNull();
    }

    @Test
    void allArgsConstructor_AllFields_CreatesInstance() {
        // Act
        SuperUser superUser = new SuperUser("jane.smith@example.com", "Jane", "Smith");

        // Assert
        assertThat(superUser).isNotNull();
        assertThat(superUser.getEmail()).isEqualTo("jane.smith@example.com");
        assertThat(superUser.getFirstName()).isEqualTo("Jane");
        assertThat(superUser.getLastName()).isEqualTo("Smith");
    }

    @Test
    void setters_UpdateFields() {
        // Arrange
        SuperUser superUser = new SuperUser();

        // Act
        superUser.setEmail("bob.johnson@example.com");
        superUser.setFirstName("Bob");
        superUser.setLastName("Johnson");

        // Assert
        assertThat(superUser.getEmail()).isEqualTo("bob.johnson@example.com");
        assertThat(superUser.getFirstName()).isEqualTo("Bob");
        assertThat(superUser.getLastName()).isEqualTo("Johnson");
    }

    @Test
    void equals_SameValues_ReturnsTrue() {
        // Arrange
        SuperUser superUser1 = SuperUser.builder()
            .email("test@example.com")
            .firstName("Test")
            .lastName("User")
            .build();

        SuperUser superUser2 = SuperUser.builder()
            .email("test@example.com")
            .firstName("Test")
            .lastName("User")
            .build();

        // Act & Assert
        assertThat(superUser1).isEqualTo(superUser2);
        assertThat(superUser1.hashCode()).hasSameHashCodeAs(superUser2.hashCode());
    }

    @Test
    void equals_DifferentEmail_ReturnsFalse() {
        // Arrange
        SuperUser superUser1 = SuperUser.builder()
            .email("user1@example.com")
            .firstName("Same")
            .lastName("Name")
            .build();

        SuperUser superUser2 = SuperUser.builder()
            .email("user2@example.com")
            .firstName("Same")
            .lastName("Name")
            .build();

        // Act & Assert
        assertThat(superUser1).isNotEqualTo(superUser2);
    }

    @Test
    void equals_DifferentFirstName_ReturnsFalse() {
        // Arrange
        SuperUser superUser1 = SuperUser.builder()
            .email("same@example.com")
            .firstName("First1")
            .lastName("Same")
            .build();

        SuperUser superUser2 = SuperUser.builder()
            .email("same@example.com")
            .firstName("First2")
            .lastName("Same")
            .build();

        // Act & Assert
        assertThat(superUser1).isNotEqualTo(superUser2);
    }

    @Test
    void equals_DifferentLastName_ReturnsFalse() {
        // Arrange
        SuperUser superUser1 = SuperUser.builder()
            .email("same@example.com")
            .firstName("Same")
            .lastName("Last1")
            .build();

        SuperUser superUser2 = SuperUser.builder()
            .email("same@example.com")
            .firstName("Same")
            .lastName("Last2")
            .build();

        // Act & Assert
        assertThat(superUser1).isNotEqualTo(superUser2);
    }

    @Test
    void toString_ReturnsFormattedString() {
        // Arrange
        SuperUser superUser = SuperUser.builder()
            .email("toString@example.com")
            .firstName("ToString")
            .lastName("Test")
            .build();

        // Act
        String toString = superUser.toString();

        // Assert
        for (String s : Arrays.asList(
            "SuperUser",
            "email=toString@example.com",
            "firstName=ToString",
            "lastName=Test"
        )) {
            assertThat(toString).contains(s);
        }
    }

    @Test
    void builder_PartialFields_ReturnsSuperUser() {
        // Act
        SuperUser superUserEmailOnly = SuperUser.builder()
            .email("partial@example.com")
            .build();

        SuperUser superUserNameOnly = SuperUser.builder()
            .firstName("Partial")
            .lastName("User")
            .build();

        // Assert
        assertThat(superUserEmailOnly).isNotNull();
        assertThat(superUserEmailOnly.getEmail()).isEqualTo("partial@example.com");
        assertThat(superUserEmailOnly.getFirstName()).isNull();
        assertThat(superUserEmailOnly.getLastName()).isNull();

        assertThat(superUserNameOnly).isNotNull();
        assertThat(superUserNameOnly.getEmail()).isNull();
        assertThat(superUserNameOnly.getFirstName()).isEqualTo("Partial");
        assertThat(superUserNameOnly.getLastName()).isEqualTo("User");
    }

    @Test
    void builder_NullFields_ReturnsSuperUser() {
        // Act
        SuperUser superUser = SuperUser.builder()
            .email(null)
            .firstName(null)
            .lastName(null)
            .build();

        // Assert
        assertThat(superUser).isNotNull();
        assertThat(superUser.getEmail()).isNull();
        assertThat(superUser.getFirstName()).isNull();
        assertThat(superUser.getLastName()).isNull();
    }

    @Test
    void jsonSerialization_AllFields_SerializesCorrectly() throws Exception {
        // Arrange
        SuperUser superUser = SuperUser.builder()
            .email("json.test@example.com")
            .firstName("Json")
            .lastName("Test")
            .build();

        // Act
        String json = objectMapper.writeValueAsString(superUser);

        // Assert
        for (String s : Arrays.asList(
            "\"email\":\"json.test@example.com\"",
            "\"firstName\":\"Json\"",
            "\"lastName\":\"Test\""
        )) {
            assertThat(json).contains(s);
        }
    }

    @Test
    void jsonDeserialization_ValidJson_DeserializesCorrectly() throws Exception {
        // Arrange
        String json = "{\"email\":\"deserialize@example.com\",\"firstName\":\"Deserialize\",\"lastName\":\"Test\"}";

        // Act
        SuperUser superUser = objectMapper.readValue(json, SuperUser.class);

        // Assert
        assertThat(superUser).isNotNull();
        assertThat(superUser.getEmail()).isEqualTo("deserialize@example.com");
        assertThat(superUser.getFirstName()).isEqualTo("Deserialize");
        assertThat(superUser.getLastName()).isEqualTo("Test");
    }

    @Test
    void jsonDeserialization_NullFields_DeserializesCorrectly() throws Exception {
        // Arrange
        String json = "{\"email\":null,\"firstName\":null,\"lastName\":null}";

        // Act
        SuperUser superUser = objectMapper.readValue(json, SuperUser.class);

        // Assert
        assertThat(superUser).isNotNull();
        assertThat(superUser.getEmail()).isNull();
        assertThat(superUser.getFirstName()).isNull();
        assertThat(superUser.getLastName()).isNull();
    }

    @Test
    void jsonDeserialization_MissingFields_DeserializesCorrectly() throws Exception {
        // Arrange
        String json = "{}";

        // Act
        SuperUser superUser = objectMapper.readValue(json, SuperUser.class);

        // Assert
        assertThat(superUser).isNotNull();
        assertThat(superUser.getEmail()).isNull();
        assertThat(superUser.getFirstName()).isNull();
        assertThat(superUser.getLastName()).isNull();
    }

    @Test
    void jsonDeserialization_PartialFields_DeserializesCorrectly() throws Exception {
        // Arrange
        String jsonEmailOnly = "{\"email\":\"emailonly@example.com\"}";
        String jsonNameOnly = "{\"firstName\":\"FirstOnly\",\"lastName\":\"LastOnly\"}";

        // Act
        SuperUser superUser1 = objectMapper.readValue(jsonEmailOnly, SuperUser.class);
        SuperUser superUser2 = objectMapper.readValue(jsonNameOnly, SuperUser.class);

        // Assert
        assertThat(superUser1).isNotNull();
        assertThat(superUser1.getEmail()).isEqualTo("emailonly@example.com");
        assertThat(superUser1.getFirstName()).isNull();
        assertThat(superUser1.getLastName()).isNull();

        assertThat(superUser2).isNotNull();
        assertThat(superUser2.getEmail()).isNull();
        assertThat(superUser2.getFirstName()).isEqualTo("FirstOnly");
        assertThat(superUser2.getLastName()).isEqualTo("LastOnly");
    }

    @Test
    void builder_EmptyStrings_ReturnsSuperUser() {
        // Act
        SuperUser superUser = SuperUser.builder()
            .email("")
            .firstName("")
            .lastName("")
            .build();

        // Assert
        assertThat(superUser).isNotNull();
        assertThat(superUser.getEmail()).isEmpty();
        assertThat(superUser.getFirstName()).isEmpty();
        assertThat(superUser.getLastName()).isEmpty();
    }

    @Test
    void builder_SpecialCharacters_ReturnsSuperUser() {
        // Act
        SuperUser superUser = SuperUser.builder()
            .email("special+chars@example-domain.com")
            .firstName("Jean-Pierre")
            .lastName("O'Connor")
            .build();

        // Assert
        assertThat(superUser).isNotNull();
        assertThat(superUser.getEmail()).isEqualTo("special+chars@example-domain.com");
        assertThat(superUser.getFirstName()).isEqualTo("Jean-Pierre");
        assertThat(superUser.getLastName()).isEqualTo("O'Connor");
    }
}
