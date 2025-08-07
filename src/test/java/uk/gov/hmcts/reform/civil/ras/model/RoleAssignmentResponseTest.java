package uk.gov.hmcts.reform.civil.ras.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

class RoleAssignmentResponseTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Test
    void builder_AllFields_ReturnsRoleAssignmentResponse() {
        // Arrange
        ZonedDateTime beginTime = ZonedDateTime.of(2023, 12, 25, 10, 0, 0, 0, ZoneId.of("UTC"));
        ZonedDateTime endTime = ZonedDateTime.of(2024, 12, 25, 10, 0, 0, 0, ZoneId.of("UTC"));
        ZonedDateTime created = ZonedDateTime.of(2023, 12, 1, 9, 0, 0, 0, ZoneId.of("UTC"));

        Attributes attributes = Attributes.builder().substantive("Y").caseId("12345678").jurisdiction("PRIVATELAW").caseType(
            "CARE_SUPERVISION_EPO").primaryLocation("12345").region("North West").contractType("SALARIED").build();

        // Act
        RoleAssignmentResponse response = RoleAssignmentResponse.builder().actorId("actor123").actorIdType("IDAM").roleType(
            "CASE").roleName("judge").roleLabel("Judge").classification("PUBLIC").grantType("STANDARD").roleCategory(
            "JUDICIAL").readOnly(true).beginTime(beginTime).endTime(endTime).created(created).attributes(attributes).build();

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getActorId()).isEqualTo("actor123");
        assertThat(response.getActorIdType()).isEqualTo("IDAM");
        assertThat(response.getRoleType()).isEqualTo("CASE");
        assertThat(response.getRoleName()).isEqualTo("judge");
        assertThat(response.getRoleLabel()).isEqualTo("Judge");
        assertThat(response.getClassification()).isEqualTo("PUBLIC");
        assertThat(response.getGrantType()).isEqualTo("STANDARD");
        assertThat(response.getRoleCategory()).isEqualTo("JUDICIAL");
        assertThat(response.getReadOnly()).isTrue();
        assertThat(response.getBeginTime()).isEqualTo(beginTime);
        assertThat(response.getEndTime()).isEqualTo(endTime);
        assertThat(response.getCreated()).isEqualTo(created);
        assertThat(response.getAttributes()).isEqualTo(attributes);
    }

    @Test
    void noArgsConstructor_CreatesEmptyInstance() {
        // Act
        RoleAssignmentResponse response = new RoleAssignmentResponse();

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getActorId()).isNull();
        assertThat(response.getActorIdType()).isNull();
        assertThat(response.getRoleType()).isNull();
        assertThat(response.getRoleName()).isNull();
        assertThat(response.getRoleLabel()).isNull();
        assertThat(response.getClassification()).isNull();
        assertThat(response.getGrantType()).isNull();
        assertThat(response.getRoleCategory()).isNull();
        assertThat(response.getReadOnly()).isNull();
        assertThat(response.getBeginTime()).isNull();
        assertThat(response.getEndTime()).isNull();
        assertThat(response.getCreated()).isNull();
        assertThat(response.getAttributes()).isNull();
    }

    @Test
    void allArgsConstructor_AllFields_CreatesInstance() {
        // Arrange
        ZonedDateTime now = ZonedDateTime.now();
        Attributes attributes = Attributes.builder().substantive("N").build();

        // Act
        RoleAssignmentResponse response = new RoleAssignmentResponse(
            "actor456",
            "CASEWORKER",
            "ORGANISATION",
            "solicitor",
            "Solicitor",
            "PRIVATE",
            "SPECIFIC",
            "PROFESSIONAL",
            false,
            now,
            now.plusDays(30),
            now.minusDays(5),
            attributes
        );

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getActorId()).isEqualTo("actor456");
        assertThat(response.getActorIdType()).isEqualTo("CASEWORKER");
        assertThat(response.getRoleType()).isEqualTo("ORGANISATION");
        assertThat(response.getRoleName()).isEqualTo("solicitor");
        assertThat(response.getRoleLabel()).isEqualTo("Solicitor");
        assertThat(response.getClassification()).isEqualTo("PRIVATE");
        assertThat(response.getGrantType()).isEqualTo("SPECIFIC");
        assertThat(response.getRoleCategory()).isEqualTo("PROFESSIONAL");
        assertThat(response.getReadOnly()).isFalse();
        assertThat(response.getBeginTime()).isEqualTo(now);
        assertThat(response.getEndTime()).isEqualTo(now.plusDays(30));
        assertThat(response.getCreated()).isEqualTo(now.minusDays(5));
        assertThat(response.getAttributes()).isEqualTo(attributes);
    }

    @Test
    void setters_UpdateFields() {
        // Arrange
        RoleAssignmentResponse response = new RoleAssignmentResponse();
        ZonedDateTime now = ZonedDateTime.now();
        Attributes attributes = Attributes.builder().caseId("updated123").build();

        // Act
        response.setActorId("updatedActor");
        response.setActorIdType("UPDATED_TYPE");
        response.setRoleType("UPDATED_ROLE_TYPE");
        response.setRoleName("updatedRole");
        response.setRoleLabel("Updated Role");
        response.setClassification("RESTRICTED");
        response.setGrantType("CHALLENGED");
        response.setRoleCategory("ADMIN");
        response.setReadOnly(true);
        response.setBeginTime(now);
        response.setEndTime(now.plusDays(90));
        response.setCreated(now.minusDays(10));
        response.setAttributes(attributes);

        // Assert
        assertThat(response.getActorId()).isEqualTo("updatedActor");
        assertThat(response.getActorIdType()).isEqualTo("UPDATED_TYPE");
        assertThat(response.getRoleType()).isEqualTo("UPDATED_ROLE_TYPE");
        assertThat(response.getRoleName()).isEqualTo("updatedRole");
        assertThat(response.getRoleLabel()).isEqualTo("Updated Role");
        assertThat(response.getClassification()).isEqualTo("RESTRICTED");
        assertThat(response.getGrantType()).isEqualTo("CHALLENGED");
        assertThat(response.getRoleCategory()).isEqualTo("ADMIN");
        assertThat(response.getReadOnly()).isTrue();
        assertThat(response.getBeginTime()).isEqualTo(now);
        assertThat(response.getEndTime()).isEqualTo(now.plusDays(90));
        assertThat(response.getCreated()).isEqualTo(now.minusDays(10));
        assertThat(response.getAttributes()).isEqualTo(attributes);
    }

    @Test
    void equals_SameValues_ReturnsTrue() {
        // Arrange
        ZonedDateTime now = ZonedDateTime.now();
        RoleAssignmentResponse response1 = RoleAssignmentResponse.builder().actorId("same123").roleName("sameRole").beginTime(
            now).readOnly(true).build();

        RoleAssignmentResponse response2 = RoleAssignmentResponse.builder().actorId("same123").roleName("sameRole").beginTime(
            now).readOnly(true).build();

        // Act & Assert
        assertThat(response1).isEqualTo(response2);
        assertThat(response1.hashCode()).hasSameHashCodeAs(response2.hashCode());
    }

    @Test
    void equals_DifferentValues_ReturnsFalse() {
        // Arrange
        RoleAssignmentResponse response1 = RoleAssignmentResponse.builder().actorId("actor1").roleName("role1").build();

        RoleAssignmentResponse response2 = RoleAssignmentResponse.builder().actorId("actor2").roleName("role2").build();

        // Act & Assert
        assertThat(response1).isNotEqualTo(response2);
    }

    @Test
    void toString_ReturnsFormattedString() {
        // Arrange
        RoleAssignmentResponse response = RoleAssignmentResponse.builder().actorId("toString123").roleName(
            "toStringRole").roleLabel("ToString Role").classification("PUBLIC").build();

        // Act
        String toString = response.toString();

        // Assert
        for (String s : Arrays.asList(
            "RoleAssignmentResponse",
            "actorId=toString123",
            "roleName=toStringRole",
            "roleLabel=ToString Role",
            "classification=PUBLIC"
        )) {
            assertThat(toString).contains(s);
        }
    }

    @Test
    void builder_PartialFields_ReturnsRoleAssignmentResponse() {
        // Act
        RoleAssignmentResponse response = RoleAssignmentResponse.builder().actorId("partial123").roleName("partialRole").readOnly(
            false).build();

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getActorId()).isEqualTo("partial123");
        assertThat(response.getRoleName()).isEqualTo("partialRole");
        assertThat(response.getReadOnly()).isFalse();
        assertThat(response.getActorIdType()).isNull();
        assertThat(response.getRoleType()).isNull();
        assertThat(response.getRoleLabel()).isNull();
        assertThat(response.getClassification()).isNull();
        assertThat(response.getGrantType()).isNull();
        assertThat(response.getRoleCategory()).isNull();
        assertThat(response.getBeginTime()).isNull();
        assertThat(response.getEndTime()).isNull();
        assertThat(response.getCreated()).isNull();
        assertThat(response.getAttributes()).isNull();
    }

    @Test
    void builder_NullValues_ReturnsRoleAssignmentResponse() {
        // Act
        RoleAssignmentResponse response = RoleAssignmentResponse.builder().actorId(null).actorIdType(null).roleType(null).roleName(
            null).roleLabel(null).classification(null).grantType(null).roleCategory(null).readOnly(null).beginTime(null).endTime(
            null).created(null).attributes(null).build();

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getActorId()).isNull();
        assertThat(response.getActorIdType()).isNull();
        assertThat(response.getRoleType()).isNull();
        assertThat(response.getRoleName()).isNull();
        assertThat(response.getRoleLabel()).isNull();
        assertThat(response.getClassification()).isNull();
        assertThat(response.getGrantType()).isNull();
        assertThat(response.getRoleCategory()).isNull();
        assertThat(response.getReadOnly()).isNull();
        assertThat(response.getBeginTime()).isNull();
        assertThat(response.getEndTime()).isNull();
        assertThat(response.getCreated()).isNull();
        assertThat(response.getAttributes()).isNull();
    }

    @Test
    void jsonSerialization_AllFields_SerializesCorrectly() throws Exception {
        // Arrange
        ZonedDateTime beginTime = ZonedDateTime.of(2023, 12, 25, 10, 0, 0, 0, ZoneId.of("UTC"));
        Attributes attributes = Attributes.builder().substantive("Y").caseId("json123").build();

        RoleAssignmentResponse response = RoleAssignmentResponse.builder().actorId("jsonActor").actorIdType("IDAM").roleType(
            "CASE").roleName("jsonRole").roleLabel("JSON Role").classification("PUBLIC").grantType("STANDARD").roleCategory(
            "JUDICIAL").readOnly(false).beginTime(beginTime).attributes(attributes).build();

        // Act
        String json = objectMapper.writeValueAsString(response);

        // Assert
        for (String s : Arrays.asList(
            "\"actorId\":\"jsonActor\"",
            "\"actorIdType\":\"IDAM\"",
            "\"roleType\":\"CASE\"",
            "\"roleName\":\"jsonRole\"",
            "\"roleLabel\":\"JSON Role\"",
            "\"classification\":\"PUBLIC\"",
            "\"grantType\":\"STANDARD\"",
            "\"roleCategory\":\"JUDICIAL\"",
            "\"readOnly\":false",
            "\"beginTime\":\"2023-12-25T10:00:00Z\"",
            "\"attributes\"",
            "\"substantive\":\"Y\"",
            "\"caseId\":\"json123\""
        )) {
            assertThat(json).contains(s);
        }
    }

    @Test
    void jsonDeserialization_ValidJson_DeserializesCorrectly() throws Exception {
        // Arrange
        String json = "{\"actorId\":\"deser123\",\"actorIdType\":\"CASEWORKER\",\"roleType\":\"ORGANISATION\","
            + "\"roleName\":\"deserRole\",\"roleLabel\":\"Deser Role\",\"classification\":\"PRIVATE\","
            + "\"grantType\":\"SPECIFIC\",\"roleCategory\":\"PROFESSIONAL\",\"readOnly\":true,"
            + "\"beginTime\":\"2023-12-25T10:00:00Z\",\"endTime\":\"2024-12-25T10:00:00Z\","
            + "\"created\":\"2023-12-01T09:00:00Z\",\"attributes\":{\"substantive\":\"N\",\"caseId\":\"deser456\"}}";

        // Act
        RoleAssignmentResponse response = objectMapper.readValue(json, RoleAssignmentResponse.class);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getActorId()).isEqualTo("deser123");
        assertThat(response.getActorIdType()).isEqualTo("CASEWORKER");
        assertThat(response.getRoleType()).isEqualTo("ORGANISATION");
        assertThat(response.getRoleName()).isEqualTo("deserRole");
        assertThat(response.getRoleLabel()).isEqualTo("Deser Role");
        assertThat(response.getClassification()).isEqualTo("PRIVATE");
        assertThat(response.getGrantType()).isEqualTo("SPECIFIC");
        assertThat(response.getRoleCategory()).isEqualTo("PROFESSIONAL");
        assertThat(response.getReadOnly()).isTrue();
        assertThat(response.getBeginTime()).isEqualTo(ZonedDateTime.of(2023, 12, 25, 10, 0, 0, 0, ZoneId.of("UTC")));
        assertThat(response.getEndTime()).isEqualTo(ZonedDateTime.of(2024, 12, 25, 10, 0, 0, 0, ZoneId.of("UTC")));
        assertThat(response.getCreated()).isEqualTo(ZonedDateTime.of(2023, 12, 1, 9, 0, 0, 0, ZoneId.of("UTC")));
        assertThat(response.getAttributes()).isNotNull();
        assertThat(response.getAttributes().getSubstantive()).isEqualTo("N");
        assertThat(response.getAttributes().getCaseId()).isEqualTo("deser456");
    }

    @Test
    void jsonDeserialization_EmptyJson_DeserializesCorrectly() throws Exception {
        // Arrange
        String json = "{}";

        // Act
        RoleAssignmentResponse response = objectMapper.readValue(json, RoleAssignmentResponse.class);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getActorId()).isNull();
        assertThat(response.getActorIdType()).isNull();
        assertThat(response.getRoleType()).isNull();
        assertThat(response.getRoleName()).isNull();
        assertThat(response.getRoleLabel()).isNull();
        assertThat(response.getClassification()).isNull();
        assertThat(response.getGrantType()).isNull();
        assertThat(response.getRoleCategory()).isNull();
        assertThat(response.getReadOnly()).isNull();
        assertThat(response.getBeginTime()).isNull();
        assertThat(response.getEndTime()).isNull();
        assertThat(response.getCreated()).isNull();
        assertThat(response.getAttributes()).isNull();
    }

    @Test
    void jsonDeserialization_NullFields_DeserializesCorrectly() throws Exception {
        // Arrange
        String json = "{\"actorId\":null,\"actorIdType\":null,\"roleType\":null,\"roleName\":null,"
            + "\"roleLabel\":null,\"classification\":null,\"grantType\":null,\"roleCategory\":null,"
            + "\"readOnly\":null,\"beginTime\":null,\"endTime\":null,\"created\":null,\"attributes\":null}";

        // Act
        RoleAssignmentResponse response = objectMapper.readValue(json, RoleAssignmentResponse.class);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getActorId()).isNull();
        assertThat(response.getActorIdType()).isNull();
        assertThat(response.getRoleType()).isNull();
        assertThat(response.getRoleName()).isNull();
        assertThat(response.getRoleLabel()).isNull();
        assertThat(response.getClassification()).isNull();
        assertThat(response.getGrantType()).isNull();
        assertThat(response.getRoleCategory()).isNull();
        assertThat(response.getReadOnly()).isNull();
        assertThat(response.getBeginTime()).isNull();
        assertThat(response.getEndTime()).isNull();
        assertThat(response.getCreated()).isNull();
        assertThat(response.getAttributes()).isNull();
    }

    @Test
    void builder_ComplexAttributes_ReturnsRoleAssignmentResponse() {
        // Arrange
        Attributes complexAttributes = Attributes.builder().substantive("Y").caseId("complex123").jurisdiction("CIVIL").caseType(
            "MONEY_CLAIM").primaryLocation("LONDON").region("South East").contractType("PERMANENT").build();

        // Act
        RoleAssignmentResponse response = RoleAssignmentResponse.builder().actorId("complexActor").attributes(
            complexAttributes).build();

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getActorId()).isEqualTo("complexActor");
        assertThat(response.getAttributes()).isNotNull();
        assertThat(response.getAttributes().getSubstantive()).isEqualTo("Y");
        assertThat(response.getAttributes().getCaseId()).isEqualTo("complex123");
        assertThat(response.getAttributes().getJurisdiction()).isEqualTo("CIVIL");
        assertThat(response.getAttributes().getCaseType()).isEqualTo("MONEY_CLAIM");
        assertThat(response.getAttributes().getPrimaryLocation()).isEqualTo("LONDON");
        assertThat(response.getAttributes().getRegion()).isEqualTo("South East");
        assertThat(response.getAttributes().getContractType()).isEqualTo("PERMANENT");
    }
}
