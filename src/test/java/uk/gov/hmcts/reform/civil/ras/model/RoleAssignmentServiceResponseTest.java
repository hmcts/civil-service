package uk.gov.hmcts.reform.civil.ras.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RoleAssignmentServiceResponseTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Test
    void builder_AllFields_ReturnsRoleAssignmentServiceResponse() {
        // Arrange
        RoleAssignmentResponse response1 = RoleAssignmentResponse.builder().actorId("actor1").roleName("judge").roleType(
            "CASE").build();

        RoleAssignmentResponse response2 = RoleAssignmentResponse.builder().actorId("actor2").roleName("solicitor").roleType(
            "ORGANISATION").build();

        List<RoleAssignmentResponse> responses = Arrays.asList(response1, response2);

        // Act
        RoleAssignmentServiceResponse serviceResponse = RoleAssignmentServiceResponse.builder().roleAssignmentResponse(
            responses).build();

        // Assert
        assertThat(serviceResponse).isNotNull();
        assertThat(serviceResponse.getRoleAssignmentResponse()).hasSize(2)
            .containsExactly(response1, response2);
    }

    @Test
    void noArgsConstructor_CreatesEmptyInstance() {
        // Act
        RoleAssignmentServiceResponse serviceResponse = new RoleAssignmentServiceResponse();

        // Assert
        assertThat(serviceResponse).isNotNull();
        assertThat(serviceResponse.getRoleAssignmentResponse()).isNull();
    }

    @Test
    void allArgsConstructor_AllFields_CreatesInstance() {
        // Arrange
        RoleAssignmentResponse response = RoleAssignmentResponse.builder().actorId("actor123").roleName("legal-adviser").build();
        List<RoleAssignmentResponse> responses = Collections.singletonList(response);

        // Act
        RoleAssignmentServiceResponse serviceResponse = new RoleAssignmentServiceResponse(responses);

        // Assert
        assertThat(serviceResponse).isNotNull();
        assertThat(serviceResponse.getRoleAssignmentResponse()).hasSize(1)
            .containsExactly(response);
    }

    @Test
    void setters_UpdateFields() {
        // Arrange
        RoleAssignmentServiceResponse serviceResponse = new RoleAssignmentServiceResponse();
        RoleAssignmentResponse response = RoleAssignmentResponse.builder().actorId("updatedActor").roleName(
            "updatedRole").build();
        List<RoleAssignmentResponse> responses = Collections.singletonList(response);

        // Act
        serviceResponse.setRoleAssignmentResponse(responses);

        // Assert
        assertThat(serviceResponse.getRoleAssignmentResponse()).hasSize(1)
            .containsExactly(response);
    }

    @Test
    void equals_SameValues_ReturnsTrue() {
        // Arrange
        RoleAssignmentResponse response = RoleAssignmentResponse.builder().actorId("sameActor").roleName("sameRole").build();
        List<RoleAssignmentResponse> responses = Collections.singletonList(response);

        RoleAssignmentServiceResponse serviceResponse1 = RoleAssignmentServiceResponse.builder().roleAssignmentResponse(
            responses).build();

        RoleAssignmentServiceResponse serviceResponse2 = RoleAssignmentServiceResponse.builder().roleAssignmentResponse(
            responses).build();

        // Act & Assert
        assertThat(serviceResponse1).isEqualTo(serviceResponse2);
        assertThat(serviceResponse1.hashCode()).hasSameHashCodeAs(serviceResponse2.hashCode());
    }

    @Test
    void equals_DifferentValues_ReturnsFalse() {
        // Arrange
        RoleAssignmentServiceResponse serviceResponse1 = RoleAssignmentServiceResponse.builder().roleAssignmentResponse(
            Collections.singletonList(RoleAssignmentResponse.builder().actorId("actor1").build())).build();

        RoleAssignmentServiceResponse serviceResponse2 = RoleAssignmentServiceResponse.builder().roleAssignmentResponse(
            Collections.singletonList(RoleAssignmentResponse.builder().actorId("actor2").build())).build();

        // Act & Assert
        assertThat(serviceResponse1).isNotEqualTo(serviceResponse2);
    }

    @Test
    void toString_ReturnsFormattedString() {
        // Arrange
        RoleAssignmentResponse response = RoleAssignmentResponse.builder().actorId("toStringActor").roleName(
            "toStringRole").build();
        RoleAssignmentServiceResponse serviceResponse = RoleAssignmentServiceResponse.builder().roleAssignmentResponse(
            Collections.singletonList(response)).build();

        // Act
        String toString = serviceResponse.toString();

        // Assert
        assertThat(toString).contains("RoleAssignmentServiceResponse")
            .contains("roleAssignmentResponse");
    }

    @Test
    void builder_EmptyList_ReturnsRoleAssignmentServiceResponse() {
        // Act
        RoleAssignmentServiceResponse serviceResponse = RoleAssignmentServiceResponse.builder().roleAssignmentResponse(
            Collections.emptyList()).build();

        // Assert
        assertThat(serviceResponse).isNotNull();
        assertThat(serviceResponse.getRoleAssignmentResponse()).isEmpty();
    }

    @Test
    void builder_NullList_ReturnsRoleAssignmentServiceResponse() {
        // Act
        RoleAssignmentServiceResponse serviceResponse = RoleAssignmentServiceResponse.builder().roleAssignmentResponse(
            null).build();

        // Assert
        assertThat(serviceResponse).isNotNull();
        assertThat(serviceResponse.getRoleAssignmentResponse()).isNull();
    }

    @Test
    void builder_ComplexScenario_ReturnsRoleAssignmentServiceResponse() {
        // Arrange
        ZonedDateTime now = ZonedDateTime.now();

        RoleAssignmentResponse response1 = RoleAssignmentResponse.builder().actorId("complex1").actorIdType("IDAM").roleType(
            "CASE").roleName("judge").roleLabel("Judge").classification("PUBLIC").grantType("STANDARD").roleCategory(
            "JUDICIAL").readOnly(false).beginTime(now).endTime(now.plusDays(30)).attributes(Attributes.builder().substantive(
            "Y").caseId("case123").jurisdiction("CIVIL").build()).build();

        RoleAssignmentResponse response2 = RoleAssignmentResponse.builder().actorId("complex2").actorIdType("CASEWORKER").roleType(
            "ORGANISATION").roleName("solicitor").roleLabel("Solicitor").classification("PRIVATE").grantType("SPECIFIC").roleCategory(
            "LEGAL_OPERATIONS").readOnly(true).beginTime(now.minusDays(10)).endTime(now.plusDays(20)).build();

        RoleAssignmentResponse response3 = RoleAssignmentResponse.builder().actorId("complex3").roleName("admin").roleCategory(
            "ADMIN").build();

        List<RoleAssignmentResponse> responses = Arrays.asList(response1, response2, response3);

        // Act
        RoleAssignmentServiceResponse serviceResponse = RoleAssignmentServiceResponse.builder().roleAssignmentResponse(
            responses).build();

        // Assert
        assertThat(serviceResponse).isNotNull();
        assertThat(serviceResponse.getRoleAssignmentResponse()).hasSize(3);
        assertThat(serviceResponse.getRoleAssignmentResponse()).extracting(RoleAssignmentResponse::getActorId).containsExactly(
            "complex1",
            "complex2",
            "complex3"
        );
        assertThat(serviceResponse.getRoleAssignmentResponse()).extracting(RoleAssignmentResponse::getRoleName).containsExactly(
            "judge",
            "solicitor",
            "admin"
        );
    }

    @Test
    void jsonSerialization_AllFields_SerializesCorrectly() throws Exception {
        // Arrange
        ZonedDateTime beginTime = ZonedDateTime.of(2023, 12, 25, 10, 0, 0, 0, ZoneId.of("UTC"));

        RoleAssignmentResponse response1 = RoleAssignmentResponse.builder().actorId("json1").roleName("jsonRole1").roleType(
            "CASE").beginTime(beginTime).build();

        RoleAssignmentResponse response2 = RoleAssignmentResponse.builder().actorId("json2").roleName("jsonRole2").roleType(
            "ORGANISATION").build();

        RoleAssignmentServiceResponse serviceResponse = RoleAssignmentServiceResponse.builder().roleAssignmentResponse(
            Arrays.asList(response1, response2)).build();

        // Act
        String json = objectMapper.writeValueAsString(serviceResponse);

        // Assert
        for (String s : Arrays.asList(
            "\"roleAssignmentResponse\"",
            "\"actorId\":\"json1\"",
            "\"roleName\":\"jsonRole1\"",
            "\"roleType\":\"CASE\"",
            "\"beginTime\":\"2023-12-25T10:00:00Z\"",
            "\"actorId\":\"json2\"",
            "\"roleName\":\"jsonRole2\"",
            "\"roleType\":\"ORGANISATION\""
        )) {
            assertThat(json).contains(s);
        }
    }

    @Test
    void jsonDeserialization_ValidJson_DeserializesCorrectly() throws Exception {
        // Arrange
        String json = "{\"roleAssignmentResponse\":[{\"actorId\":\"deser1\",\"roleName\":\"deserRole1\","
            + "\"roleType\":\"CASE\"},{\"actorId\":\"deser2\",\"roleName\":\"deserRole2\","
            + "\"roleType\":\"ORGANISATION\"}]}";

        // Act
        RoleAssignmentServiceResponse serviceResponse = objectMapper.readValue(
            json,
            RoleAssignmentServiceResponse.class
        );

        // Assert
        assertThat(serviceResponse).isNotNull();
        assertThat(serviceResponse.getRoleAssignmentResponse()).hasSize(2);
        assertThat(serviceResponse.getRoleAssignmentResponse().get(0).getActorId()).isEqualTo("deser1");
        assertThat(serviceResponse.getRoleAssignmentResponse().get(0).getRoleName()).isEqualTo("deserRole1");
        assertThat(serviceResponse.getRoleAssignmentResponse().get(0).getRoleType()).isEqualTo("CASE");
        assertThat(serviceResponse.getRoleAssignmentResponse().get(1).getActorId()).isEqualTo("deser2");
        assertThat(serviceResponse.getRoleAssignmentResponse().get(1).getRoleName()).isEqualTo("deserRole2");
        assertThat(serviceResponse.getRoleAssignmentResponse().get(1).getRoleType()).isEqualTo("ORGANISATION");
    }

    @Test
    void jsonDeserialization_EmptyJson_DeserializesCorrectly() throws Exception {
        // Arrange
        String json = "{}";

        // Act
        RoleAssignmentServiceResponse serviceResponse = objectMapper.readValue(
            json,
            RoleAssignmentServiceResponse.class
        );

        // Assert
        assertThat(serviceResponse).isNotNull();
        assertThat(serviceResponse.getRoleAssignmentResponse()).isNull();
    }

    @Test
    void jsonDeserialization_EmptyList_DeserializesCorrectly() throws Exception {
        // Arrange
        String json = "{\"roleAssignmentResponse\":[]}";

        // Act
        RoleAssignmentServiceResponse serviceResponse = objectMapper.readValue(
            json,
            RoleAssignmentServiceResponse.class
        );

        // Assert
        assertThat(serviceResponse).isNotNull();
        assertThat(serviceResponse.getRoleAssignmentResponse()).isEmpty();
    }

    @Test
    void jsonDeserialization_NullList_DeserializesCorrectly() throws Exception {
        // Arrange
        String json = "{\"roleAssignmentResponse\":null}";

        // Act
        RoleAssignmentServiceResponse serviceResponse = objectMapper.readValue(
            json,
            RoleAssignmentServiceResponse.class
        );

        // Assert
        assertThat(serviceResponse).isNotNull();
        assertThat(serviceResponse.getRoleAssignmentResponse()).isNull();
    }
}
