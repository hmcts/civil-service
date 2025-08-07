package uk.gov.hmcts.reform.civil.ras.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

class RoleRequestTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
    }

    @Test
    void builder_AllFields_ReturnsRoleRequest() {
        // Act
        RoleRequest request = RoleRequest.builder()
            .assignerId("assigner123")
            .process("process123")
            .reference("reference123")
            .replaceExisting(true)
            .build();

        // Assert
        assertThat(request).isNotNull();
        assertThat(request.getAssignerId()).isEqualTo("assigner123");
        assertThat(request.getProcess()).isEqualTo("process123");
        assertThat(request.getReference()).isEqualTo("reference123");
        assertThat(request.isReplaceExisting()).isTrue();
    }

    @Test
    void builder_WithoutReplaceExisting_UsesDefaultFalse() {
        // Act
        RoleRequest request = RoleRequest.builder()
            .assignerId("defaultTest")
            .process("testProcess")
            .reference("testRef")
            .build();

        // Assert
        assertThat(request).isNotNull();
        assertThat(request.getAssignerId()).isEqualTo("defaultTest");
        assertThat(request.getProcess()).isEqualTo("testProcess");
        assertThat(request.getReference()).isEqualTo("testRef");
        assertThat(request.isReplaceExisting()).isFalse();
    }

    @Test
    void builder_MinimalFields_ReturnsRoleRequest() {
        // Act
        RoleRequest request = RoleRequest.builder()
            .assignerId("minimal123")
            .build();

        // Assert
        assertThat(request).isNotNull();
        assertThat(request.getAssignerId()).isEqualTo("minimal123");
        assertThat(request.getProcess()).isNull();
        assertThat(request.getReference()).isNull();
        assertThat(request.isReplaceExisting()).isFalse();
    }

    @Test
    void builder_NullFields_ReturnsRoleRequest() {
        // Act
        RoleRequest request = RoleRequest.builder()
            .assignerId(null)
            .process(null)
            .reference(null)
            .replaceExisting(false)
            .build();

        // Assert
        assertThat(request).isNotNull();
        assertThat(request.getAssignerId()).isNull();
        assertThat(request.getProcess()).isNull();
        assertThat(request.getReference()).isNull();
        assertThat(request.isReplaceExisting()).isFalse();
    }

    @Test
    void builder_EmptyFields_ReturnsRoleRequest() {
        // Act
        RoleRequest request = RoleRequest.builder()
            .assignerId("")
            .process("")
            .reference("")
            .build();

        // Assert
        assertThat(request).isNotNull();
        assertThat(request.getAssignerId()).isEmpty();
        assertThat(request.getProcess()).isEmpty();
        assertThat(request.getReference()).isEmpty();
        assertThat(request.isReplaceExisting()).isFalse();
    }

    @Test
    void equals_SameValues_ReturnsTrue() {
        // Arrange
        RoleRequest request1 = RoleRequest.builder()
            .assignerId("same123")
            .process("sameProcess")
            .reference("sameRef")
            .replaceExisting(true)
            .build();

        RoleRequest request2 = RoleRequest.builder()
            .assignerId("same123")
            .process("sameProcess")
            .reference("sameRef")
            .replaceExisting(true)
            .build();

        // Act & Assert
        assertThat(request1).isEqualTo(request2);
        assertThat(request1.hashCode()).hasSameHashCodeAs(request2.hashCode());
    }

    @Test
    void equals_DifferentAssignerId_ReturnsFalse() {
        // Arrange
        RoleRequest request1 = RoleRequest.builder()
            .assignerId("id1")
            .build();

        RoleRequest request2 = RoleRequest.builder()
            .assignerId("id2")
            .build();

        // Act & Assert
        assertThat(request1).isNotEqualTo(request2);
    }

    @Test
    void equals_DifferentProcess_ReturnsFalse() {
        // Arrange
        RoleRequest request1 = RoleRequest.builder()
            .assignerId("same")
            .process("process1")
            .build();

        RoleRequest request2 = RoleRequest.builder()
            .assignerId("same")
            .process("process2")
            .build();

        // Act & Assert
        assertThat(request1).isNotEqualTo(request2);
    }

    @Test
    void equals_DifferentReference_ReturnsFalse() {
        // Arrange
        RoleRequest request1 = RoleRequest.builder()
            .assignerId("same")
            .reference("ref1")
            .build();

        RoleRequest request2 = RoleRequest.builder()
            .assignerId("same")
            .reference("ref2")
            .build();

        // Act & Assert
        assertThat(request1).isNotEqualTo(request2);
    }

    @Test
    void equals_DifferentReplaceExisting_ReturnsFalse() {
        // Arrange
        RoleRequest request1 = RoleRequest.builder()
            .assignerId("same")
            .replaceExisting(true)
            .build();

        RoleRequest request2 = RoleRequest.builder()
            .assignerId("same")
            .replaceExisting(false)
            .build();

        // Act & Assert
        assertThat(request1).isNotEqualTo(request2);
    }

    @Test
    void equals_NullValues_HandlesProperly() {
        // Arrange
        RoleRequest request1 = RoleRequest.builder()
            .assignerId(null)
            .process(null)
            .reference(null)
            .build();

        RoleRequest request2 = RoleRequest.builder()
            .assignerId(null)
            .process(null)
            .reference(null)
            .build();

        // Act & Assert
        assertThat(request1).isEqualTo(request2);
        assertThat(request1.hashCode()).hasSameHashCodeAs(request2.hashCode());
    }

    @Test
    void toString_ReturnsFormattedString() {
        // Arrange
        RoleRequest request = RoleRequest.builder()
            .assignerId("toString123")
            .process("toStringProcess")
            .reference("toStringRef")
            .replaceExisting(true)
            .build();

        // Act
        String toString = request.toString();

        // Assert
        for (String s : Arrays.asList(
            "RoleRequest",
            "assignerId=toString123",
            "process=toStringProcess",
            "reference=toStringRef",
            "replaceExisting=true"
        )) {
            assertThat(toString).contains(s);
        }
    }

    @Test
    void toString_NullFields_HandlesGracefully() {
        // Arrange
        RoleRequest request = RoleRequest.builder()
            .assignerId(null)
            .process(null)
            .reference(null)
            .build();

        // Act
        String toString = request.toString();

        // Assert
        for (String s : Arrays.asList(
            "RoleRequest",
            "assignerId=null",
            "process=null",
            "reference=null",
            "replaceExisting=false"
        )) {
            assertThat(toString).contains(s);
        }
    }

    @Test
    void builder_SpecialCharacters_ReturnsRoleRequest() {
        // Act
        RoleRequest request = RoleRequest.builder()
            .assignerId("special!@#$%^&*()")
            .process("process with spaces")
            .reference("ref/with/slashes")
            .build();

        // Assert
        assertThat(request).isNotNull();
        assertThat(request.getAssignerId()).isEqualTo("special!@#$%^&*()");
        assertThat(request.getProcess()).isEqualTo("process with spaces");
        assertThat(request.getReference()).isEqualTo("ref/with/slashes");
    }

    @Test
    void builder_LongStrings_ReturnsRoleRequest() {
        // Arrange
        String longString = "a".repeat(1000);

        // Act
        RoleRequest request = RoleRequest.builder()
            .assignerId(longString)
            .process(longString)
            .reference(longString)
            .build();

        // Assert
        assertThat(request).isNotNull();
        assertThat(request.getAssignerId()).hasSize(1000);
        assertThat(request.getProcess()).hasSize(1000);
        assertThat(request.getReference()).hasSize(1000);
    }

    @Test
    void jsonSerialization_AllFields_SerializesCorrectly() throws Exception {
        // Arrange
        RoleRequest request = RoleRequest.builder()
            .assignerId("jsonAssigner")
            .process("jsonProcess")
            .reference("jsonReference")
            .replaceExisting(true)
            .build();

        // Act
        String json = objectMapper.writeValueAsString(request);

        // Assert
        for (String s : Arrays.asList(
            "\"assignerId\":\"jsonAssigner\"",
            "\"process\":\"jsonProcess\"",
            "\"reference\":\"jsonReference\"",
            "\"replaceExisting\":true"
        )) {
            assertThat(json).contains(s);
        }
    }

    @Test
    void jsonSerialization_DefaultReplaceExisting_SerializesAsFalse() throws Exception {
        // Arrange
        RoleRequest request = RoleRequest.builder()
            .assignerId("defaultTest")
            .build();

        // Act
        String json = objectMapper.writeValueAsString(request);

        // Assert
        for (String s : Arrays.asList(
            "\"assignerId\":\"defaultTest\"",
            "\"replaceExisting\":false",
            "\"process\":null",
            "\"reference\":null"
        )) {
            assertThat(json).contains(s);
        }
    }

    @Test
    void jsonSerialization_NullFields_SerializesCorrectly() throws Exception {
        // Arrange
        RoleRequest request = RoleRequest.builder()
            .assignerId(null)
            .process(null)
            .reference(null)
            .replaceExisting(false)
            .build();

        // Act
        String json = objectMapper.writeValueAsString(request);

        // Assert
        for (String s : Arrays.asList(
            "\"assignerId\":null",
            "\"process\":null",
            "\"reference\":null",
            "\"replaceExisting\":false"
        )) {
            assertThat(json).contains(s);
        }
    }

    @Test
    void jsonSerialization_EmptyStrings_SerializesCorrectly() throws Exception {
        // Arrange
        RoleRequest request = RoleRequest.builder()
            .assignerId("")
            .process("")
            .reference("")
            .build();

        // Act
        String json = objectMapper.writeValueAsString(request);

        // Assert
        for (String s : Arrays.asList(
            "\"assignerId\":\"\"",
            "\"process\":\"\"",
            "\"reference\":\"\"",
            "\"replaceExisting\":false"
        )) {
            assertThat(json).contains(s);
        }
    }

    @Test
    void builder_MixedCase_ReturnsRoleRequest() {
        // Act
        RoleRequest request = RoleRequest.builder()
            .assignerId("MixedCase123")
            .process("UPPERCASE_PROCESS")
            .reference("lowercase-reference")
            .replaceExisting(true)
            .build();

        // Assert
        assertThat(request).isNotNull();
        assertThat(request.getAssignerId()).isEqualTo("MixedCase123");
        assertThat(request.getProcess()).isEqualTo("UPPERCASE_PROCESS");
        assertThat(request.getReference()).isEqualTo("lowercase-reference");
        assertThat(request.isReplaceExisting()).isTrue();
    }
}
