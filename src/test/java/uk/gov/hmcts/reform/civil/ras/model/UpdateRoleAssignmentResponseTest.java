package uk.gov.hmcts.reform.civil.ras.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

class UpdateRoleAssignmentResponseTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
    }

    @Test
    void builder_AllFields_ReturnsUpdateRoleAssignmentResponse() {
        // Arrange
        RoleAssignmentRequest request = RoleAssignmentRequest.builder()
            .requestedRoles(Collections.singletonList(
                RoleAssignment.builder()
                    .id("role123")
                    .actorId("actor123")
                    .roleName("judge")
                    .build()
            ))
            .roleRequest(RoleRequest.builder()
                .assignerId("assigner123")
                .process("process123")
                .build())
            .build();

        // Act
        UpdateRoleAssignmentResponse response = UpdateRoleAssignmentResponse.builder()
            .roleAssignmentResponse(request)
            .build();

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getRoleAssignmentResponse()).isEqualTo(request);
    }

    @Test
    void noArgsConstructor_CreatesEmptyInstance() {
        // Act
        UpdateRoleAssignmentResponse response = new UpdateRoleAssignmentResponse();

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getRoleAssignmentResponse()).isNull();
    }

    @Test
    void allArgsConstructor_AllFields_CreatesInstance() {
        // Arrange
        RoleAssignmentRequest request = RoleAssignmentRequest.builder()
            .requestedRoles(Collections.emptyList())
            .build();

        // Act
        UpdateRoleAssignmentResponse response = new UpdateRoleAssignmentResponse(request);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getRoleAssignmentResponse()).isEqualTo(request);
    }

    @Test
    void setters_UpdateFields() {
        // Arrange
        UpdateRoleAssignmentResponse response = new UpdateRoleAssignmentResponse();
        RoleAssignmentRequest request = RoleAssignmentRequest.builder()
            .roleRequest(RoleRequest.builder()
                .assignerId("updated")
                .build())
            .build();

        // Act
        response.setRoleAssignmentResponse(request);

        // Assert
        assertThat(response.getRoleAssignmentResponse()).isEqualTo(request);
    }

    @Test
    void equals_SameValues_ReturnsTrue() {
        // Arrange
        RoleAssignmentRequest request = RoleAssignmentRequest.builder()
            .roleRequest(RoleRequest.builder()
                             .assignerId("same")
                             .build())
            .build();

        UpdateRoleAssignmentResponse response1 = UpdateRoleAssignmentResponse.builder()
            .roleAssignmentResponse(request)
            .build();

        UpdateRoleAssignmentResponse response2 = UpdateRoleAssignmentResponse.builder()
            .roleAssignmentResponse(request)
            .build();

        // Act & Assert
        assertThat(response1).isEqualTo(response2)
            .hasSameHashCodeAs(response2);
    }

    @Test
    void equals_DifferentValues_ReturnsFalse() {
        // Arrange
        UpdateRoleAssignmentResponse response1 = UpdateRoleAssignmentResponse.builder()
            .roleAssignmentResponse(RoleAssignmentRequest.builder()
                .roleRequest(RoleRequest.builder().assignerId("id1").build())
                .build())
            .build();

        UpdateRoleAssignmentResponse response2 = UpdateRoleAssignmentResponse.builder()
            .roleAssignmentResponse(RoleAssignmentRequest.builder()
                .roleRequest(RoleRequest.builder().assignerId("id2").build())
                .build())
            .build();

        // Act & Assert
        assertThat(response1).isNotEqualTo(response2);
    }

    @Test
    void equals_OneNull_ReturnsFalse() {
        // Arrange
        UpdateRoleAssignmentResponse response1 = UpdateRoleAssignmentResponse.builder()
            .roleAssignmentResponse(RoleAssignmentRequest.builder().build())
            .build();

        UpdateRoleAssignmentResponse response2 = UpdateRoleAssignmentResponse.builder()
            .roleAssignmentResponse(null)
            .build();

        // Act & Assert
        assertThat(response1).isNotEqualTo(response2);
    }

    @Test
    void equals_BothNull_ReturnsTrue() {
        // Arrange
        UpdateRoleAssignmentResponse response1 = UpdateRoleAssignmentResponse.builder()
            .roleAssignmentResponse(null)
            .build();

        UpdateRoleAssignmentResponse response2 = UpdateRoleAssignmentResponse.builder()
            .roleAssignmentResponse(null)
            .build();

        // Act & Assert
        assertThat(response1).isEqualTo(response2)
            .hasSameHashCodeAs(response2);
    }

    @Test
    void toString_ReturnsFormattedString() {
        // Arrange
        RoleAssignmentRequest request = RoleAssignmentRequest.builder()
            .roleRequest(RoleRequest.builder()
                .assignerId("toString123")
                .process("toStringProcess")
                .build())
            .build();

        UpdateRoleAssignmentResponse response = UpdateRoleAssignmentResponse.builder()
            .roleAssignmentResponse(request)
            .build();

        // Act
        String toString = response.toString();

        // Assert
        for (String s : Arrays.asList("UpdateRoleAssignmentResponse", "roleAssignmentResponse")) {
            assertThat(toString).contains(s);
        }
    }

    @Test
    void toString_NullField_HandlesGracefully() {
        // Arrange
        UpdateRoleAssignmentResponse response = UpdateRoleAssignmentResponse.builder()
            .roleAssignmentResponse(null)
            .build();

        // Act
        String toString = response.toString();

        // Assert
        for (String s : Arrays.asList("UpdateRoleAssignmentResponse", "roleAssignmentResponse=null")) {
            assertThat(toString).contains(s);
        }
    }

    @Test
    void builder_ComplexNestedStructure_ReturnsUpdateRoleAssignmentResponse() {
        // Arrange
        RoleAssignment role1 = RoleAssignment.builder()
            .id("complex1")
            .actorId("complexActor1")
            .roleName("complexRole1")
            .roleType(RoleType.CASE)
            .roleCategory(RoleCategory.JUDICIAL)
            .grantType(GrantType.STANDARD)
            .readOnly(false)
            .build();

        RoleAssignment role2 = RoleAssignment.builder()
            .id("complex2")
            .actorId("complexActor2")
            .roleName("complexRole2")
            .roleType(RoleType.ORGANISATION)
            .roleCategory(RoleCategory.LEGAL_OPERATIONS)
            .grantType(GrantType.SPECIFIC)
            .readOnly(true)
            .build();

        RoleRequest roleRequest = RoleRequest.builder()
            .assignerId("complexAssigner")
            .process("complexProcess")
            .reference("complexReference")
            .replaceExisting(true)
            .build();

        RoleAssignmentRequest request = RoleAssignmentRequest.builder()
            .requestedRoles(java.util.Arrays.asList(role1, role2))
            .roleRequest(roleRequest)
            .build();

        // Act
        UpdateRoleAssignmentResponse response = UpdateRoleAssignmentResponse.builder()
            .roleAssignmentResponse(request)
            .build();

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getRoleAssignmentResponse()).isNotNull();
        assertThat(response.getRoleAssignmentResponse().getRequestedRoles()).hasSize(2);
        assertThat(response.getRoleAssignmentResponse().getRoleRequest()).isNotNull();
        assertThat(response.getRoleAssignmentResponse().getRoleRequest().getAssignerId()).isEqualTo("complexAssigner");
    }

    @Test
    void jsonSerialization_AllFields_SerializesCorrectly() throws Exception {
        // Arrange
        RoleAssignmentRequest request = RoleAssignmentRequest.builder()
            .requestedRoles(Collections.singletonList(
                RoleAssignment.builder()
                    .id("json123")
                    .actorId("jsonActor")
                    .roleName("jsonRole")
                    .build()
            ))
            .roleRequest(RoleRequest.builder()
                .assignerId("jsonAssigner")
                .process("jsonProcess")
                .reference("jsonReference")
                .build())
            .build();

        UpdateRoleAssignmentResponse response = UpdateRoleAssignmentResponse.builder()
            .roleAssignmentResponse(request)
            .build();

        // Act
        String json = objectMapper.writeValueAsString(response);

        // Assert
        for (String s : Arrays.asList(
            "\"roleAssignmentResponse\"",
            "\"requestedRoles\"",
            "\"roleRequest\"",
            "\"id\":\"json123\"",
            "\"actorId\":\"jsonActor\"",
            "\"roleName\":\"jsonRole\"",
            "\"assignerId\":\"jsonAssigner\"",
            "\"process\":\"jsonProcess\"",
            "\"reference\":\"jsonReference\""
        )) {
            assertThat(json).contains(s);
        }
    }

    @Test
    void jsonDeserialization_SimplifiedJson_DeserializesCorrectly() throws Exception {
        // Arrange
        // Since RoleAssignment and RoleRequest don't have no-args constructors,
        // we can only test deserialization of the wrapper itself
        String json = "{\"roleAssignmentResponse\":{\"requestedRoles\":null,\"roleRequest\":null}}";

        // Act
        UpdateRoleAssignmentResponse response = objectMapper.readValue(json, UpdateRoleAssignmentResponse.class);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getRoleAssignmentResponse()).isNotNull();
        assertThat(response.getRoleAssignmentResponse().getRequestedRoles()).isNull();
        assertThat(response.getRoleAssignmentResponse().getRoleRequest()).isNull();
    }

    @Test
    void jsonDeserialization_EmptyJson_DeserializesCorrectly() throws Exception {
        // Arrange
        String json = "{}";

        // Act
        UpdateRoleAssignmentResponse response = objectMapper.readValue(json, UpdateRoleAssignmentResponse.class);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getRoleAssignmentResponse()).isNull();
    }

    @Test
    void jsonDeserialization_NullField_DeserializesCorrectly() throws Exception {
        // Arrange
        String json = "{\"roleAssignmentResponse\":null}";

        // Act
        UpdateRoleAssignmentResponse response = objectMapper.readValue(json, UpdateRoleAssignmentResponse.class);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getRoleAssignmentResponse()).isNull();
    }

    @Test
    void jsonSerialization_NullField_SerializesCorrectly() throws Exception {
        // Arrange
        UpdateRoleAssignmentResponse response = UpdateRoleAssignmentResponse.builder()
            .roleAssignmentResponse(null)
            .build();

        // Act
        String json = objectMapper.writeValueAsString(response);

        // Assert
        assertThat(json).contains("\"roleAssignmentResponse\":null");
    }

    @Test
    void jsonDeserialization_EmptyRequest_DeserializesCorrectly() throws Exception {
        // Arrange
        String json = "{\"roleAssignmentResponse\":{\"requestedRoles\":[],\"roleRequest\":null}}";

        // Act
        UpdateRoleAssignmentResponse response = objectMapper.readValue(json, UpdateRoleAssignmentResponse.class);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getRoleAssignmentResponse()).isNotNull();
        assertThat(response.getRoleAssignmentResponse().getRequestedRoles()).isEmpty();
        assertThat(response.getRoleAssignmentResponse().getRoleRequest()).isNull();
    }
}
