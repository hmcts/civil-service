package uk.gov.hmcts.reform.civil.ras.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RoleAssignmentRequestTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Test
    void builder_AllFields_ReturnsRoleAssignmentRequest() {
        // Arrange
        RoleAssignment roleAssignment1 = RoleAssignment.builder()
            .id("role1")
            .actorId("actor1")
            .roleName("judge")
            .build();

        RoleAssignment roleAssignment2 = RoleAssignment.builder()
            .id("role2")
            .actorId("actor2")
            .roleName("solicitor")
            .build();

        List<RoleAssignment> requestedRoles = Arrays.asList(roleAssignment1, roleAssignment2);

        RoleRequest roleRequest = RoleRequest.builder()
            .assignerId("assigner123")
            .process("process")
            .reference("reference")
            .build();

        // Act
        RoleAssignmentRequest request = RoleAssignmentRequest.builder()
            .requestedRoles(requestedRoles)
            .roleRequest(roleRequest)
            .build();

        // Assert
        assertThat(request).isNotNull();
        assertThat(request.getRequestedRoles()).hasSize(2)
            .containsExactly(roleAssignment1, roleAssignment2);
        assertThat(request.getRoleRequest()).isEqualTo(roleRequest);
    }

    @Test
    void noArgsConstructor_CreatesEmptyInstance() {
        // Act
        RoleAssignmentRequest request = new RoleAssignmentRequest();

        // Assert
        assertThat(request).isNotNull();
        assertThat(request.getRequestedRoles()).isNull();
        assertThat(request.getRoleRequest()).isNull();
    }

    @Test
    void allArgsConstructor_AllFields_CreatesInstance() {
        // Arrange
        RoleAssignment roleAssignment = RoleAssignment.builder()
            .id("role123")
            .actorId("actor123")
            .build();
        List<RoleAssignment> requestedRoles = Collections.singletonList(roleAssignment);

        RoleRequest roleRequest = RoleRequest.builder()
            .assignerId("assigner456")
            .build();

        // Act
        RoleAssignmentRequest request = new RoleAssignmentRequest(requestedRoles, roleRequest);

        // Assert
        assertThat(request).isNotNull();
        assertThat(request.getRequestedRoles()).hasSize(1)
            .containsExactly(roleAssignment);
        assertThat(request.getRoleRequest()).isEqualTo(roleRequest);
    }

    @Test
    void setters_UpdateFields() {
        // Arrange
        RoleAssignmentRequest request = new RoleAssignmentRequest();
        RoleAssignment roleAssignment = RoleAssignment.builder()
            .id("updated")
            .build();
        List<RoleAssignment> requestedRoles = Collections.singletonList(roleAssignment);

        RoleRequest roleRequest = RoleRequest.builder()
            .assignerId("updatedAssigner")
            .build();

        // Act
        request.setRequestedRoles(requestedRoles);
        request.setRoleRequest(roleRequest);

        // Assert
        assertThat(request.getRequestedRoles()).hasSize(1)
            .containsExactly(roleAssignment);
        assertThat(request.getRoleRequest()).isEqualTo(roleRequest);
    }

    @Test
    void equals_SameValues_ReturnsTrue() {
        // Arrange
        RoleAssignment roleAssignment = RoleAssignment.builder()
            .id("same")
            .build();
        List<RoleAssignment> requestedRoles = Collections.singletonList(roleAssignment);

        RoleRequest roleRequest = RoleRequest.builder()
            .assignerId("sameAssigner")
            .build();

        RoleAssignmentRequest request1 = RoleAssignmentRequest.builder()
            .requestedRoles(requestedRoles)
            .roleRequest(roleRequest)
            .build();

        RoleAssignmentRequest request2 = RoleAssignmentRequest.builder()
            .requestedRoles(requestedRoles)
            .roleRequest(roleRequest)
            .build();

        // Act & Assert
        assertThat(request1).isEqualTo(request2);
        assertThat(request1.hashCode()).hasSameHashCodeAs(request2.hashCode());
    }

    @Test
    void equals_DifferentValues_ReturnsFalse() {
        // Arrange
        RoleAssignmentRequest request1 = RoleAssignmentRequest.builder()
            .requestedRoles(Collections.singletonList(RoleAssignment.builder().id("id1").build()))
            .build();

        RoleAssignmentRequest request2 = RoleAssignmentRequest.builder()
            .requestedRoles(Collections.singletonList(RoleAssignment.builder().id("id2").build()))
            .build();

        // Act & Assert
        assertThat(request1).isNotEqualTo(request2);
    }

    @Test
    void toString_ReturnsFormattedString() {
        // Arrange
        RoleAssignment roleAssignment = RoleAssignment.builder()
            .id("toString123")
            .actorId("toStringActor")
            .build();
        RoleAssignmentRequest request = RoleAssignmentRequest.builder()
            .requestedRoles(Collections.singletonList(roleAssignment))
            .build();

        // Act
        String toString = request.toString();

        // Assert
        assertThat(toString).contains("RoleAssignmentRequest")
            .contains("requestedRoles")
            .contains("roleRequest");
    }

    @Test
    void builder_EmptyList_ReturnsRoleAssignmentRequest() {
        // Act
        RoleAssignmentRequest request = RoleAssignmentRequest.builder()
            .requestedRoles(Collections.emptyList())
            .build();

        // Assert
        assertThat(request).isNotNull();
        assertThat(request.getRequestedRoles()).isEmpty();
        assertThat(request.getRoleRequest()).isNull();
    }

    @Test
    void builder_NullValues_ReturnsRoleAssignmentRequest() {
        // Act
        RoleAssignmentRequest request = RoleAssignmentRequest.builder()
            .requestedRoles(null)
            .roleRequest(null)
            .build();

        // Assert
        assertThat(request).isNotNull();
        assertThat(request.getRequestedRoles()).isNull();
        assertThat(request.getRoleRequest()).isNull();
    }

    @Test
    void builder_ComplexScenario_ReturnsRoleAssignmentRequest() {
        // Arrange
        ZonedDateTime now = ZonedDateTime.now();

        RoleAssignment role1 = RoleAssignment.builder()
            .id("complex1")
            .actorId("complexActor1")
            .roleName("complexRole1")
            .roleType(RoleType.CASE)
            .roleCategory(RoleCategory.JUDICIAL)
            .grantType(GrantType.STANDARD)
            .beginTime(now)
            .endTime(now.plusDays(30))
            .readOnly(false)
            .build();

        RoleAssignment role2 = RoleAssignment.builder()
            .id("complex2")
            .actorId("complexActor2")
            .roleName("complexRole2")
            .roleType(RoleType.ORGANISATION)
            .roleCategory(RoleCategory.LEGAL_OPERATIONS)
            .grantType(GrantType.SPECIFIC)
            .beginTime(now.minusDays(10))
            .endTime(now.plusDays(20))
            .readOnly(true)
            .build();

        List<RoleAssignment> requestedRoles = Arrays.asList(role1, role2);

        RoleRequest roleRequest = RoleRequest.builder()
            .assignerId("complexAssigner")
            .process("complexProcess")
            .reference("complexReference")
            .replaceExisting(true)
            .build();

        // Act
        RoleAssignmentRequest request = RoleAssignmentRequest.builder()
            .requestedRoles(requestedRoles)
            .roleRequest(roleRequest)
            .build();

        // Assert
        assertThat(request).isNotNull();
        assertThat(request.getRequestedRoles()).hasSize(2);
        assertThat(request.getRequestedRoles()).extracting(RoleAssignment::getId)
            .containsExactly("complex1", "complex2");
        assertThat(request.getRoleRequest()).isNotNull();
        assertThat(request.getRoleRequest().getAssignerId()).isEqualTo("complexAssigner");
    }

    @Test
    void jsonSerialization_AllFields_SerializesCorrectly() throws Exception {
        // Arrange
        RoleAssignment roleAssignment = RoleAssignment.builder()
            .id("json123")
            .actorId("jsonActor")
            .roleName("jsonRole")
            .build();

        RoleRequest roleRequest = RoleRequest.builder()
            .assignerId("jsonAssigner")
            .process("jsonProcess")
            .reference("jsonReference")
            .build();

        RoleAssignmentRequest request = RoleAssignmentRequest.builder()
            .requestedRoles(Collections.singletonList(roleAssignment))
            .roleRequest(roleRequest)
            .build();

        // Act
        String json = objectMapper.writeValueAsString(request);

        // Assert
        for (String s : Arrays.asList(
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
    void jsonDeserialization_EmptyJson_DeserializesCorrectly() throws Exception {
        // Arrange
        String json = "{}";

        // Act
        RoleAssignmentRequest request = objectMapper.readValue(json, RoleAssignmentRequest.class);

        // Assert
        assertThat(request).isNotNull();
        assertThat(request.getRequestedRoles()).isNull();
        assertThat(request.getRoleRequest()).isNull();
    }

    @Test
    void jsonDeserialization_EmptyLists_DeserializesCorrectly() throws Exception {
        // Arrange
        String json = "{\"requestedRoles\":[]}";

        // Act
        RoleAssignmentRequest request = objectMapper.readValue(json, RoleAssignmentRequest.class);

        // Assert
        assertThat(request).isNotNull();
        assertThat(request.getRequestedRoles()).isEmpty();
        assertThat(request.getRoleRequest()).isNull();
    }

    @Test
    void jsonDeserialization_NullValues_DeserializesCorrectly() throws Exception {
        // Arrange
        String json = "{\"requestedRoles\":null,\"roleRequest\":null}";

        // Act
        RoleAssignmentRequest request = objectMapper.readValue(json, RoleAssignmentRequest.class);

        // Assert
        assertThat(request).isNotNull();
        assertThat(request.getRequestedRoles()).isNull();
        assertThat(request.getRoleRequest()).isNull();
    }
}
