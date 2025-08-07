package uk.gov.hmcts.reform.civil.ras.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class RoleAssignmentTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Test
    void builder_AllFields_ReturnsRoleAssignment() {
        // Arrange
        ZonedDateTime beginTime = ZonedDateTime.of(2023, 12, 25, 10, 0, 0, 0, ZoneId.of("UTC"));
        ZonedDateTime endTime = ZonedDateTime.of(2024, 12, 25, 10, 0, 0, 0, ZoneId.of("UTC"));
        ZonedDateTime created = ZonedDateTime.of(2023, 12, 1, 9, 0, 0, 0, ZoneId.of("UTC"));

        Map<String, Object> attributes = new HashMap<>();
        attributes.put("jurisdiction", "PRIVATELAW");
        attributes.put("caseId", "12345678");

        List<String> authorisations = Arrays.asList("auth1", "auth2");
        List<String> notes = Arrays.asList("Note 1", "Note 2");

        // Act
        RoleAssignment roleAssignment = RoleAssignment.builder().id("assignment123").actorIdType("CASEWORKER").attributes(
            attributes).authorisations(authorisations).notes(notes).beginTime(beginTime).endTime(endTime).created(
            created).status("APPROVED").classification("PRIVATE").actorId("actor123").grantType(GrantType.STANDARD).roleCategory(
            RoleCategory.JUDICIAL).roleName("judge").roleType(RoleType.CASE).readOnly(true).build();

        // Assert
        assertThat(roleAssignment).isNotNull();
        assertThat(roleAssignment.getId()).isEqualTo("assignment123");
        assertThat(roleAssignment.getActorIdType()).isEqualTo("CASEWORKER");
        assertThat(roleAssignment.getAttributes()).isEqualTo(attributes);
        assertThat(roleAssignment.getAuthorisations()).isEqualTo(authorisations);
        assertThat(roleAssignment.getNotes()).isEqualTo(notes);
        assertThat(roleAssignment.getBeginTime()).isEqualTo(beginTime);
        assertThat(roleAssignment.getEndTime()).isEqualTo(endTime);
        assertThat(roleAssignment.getCreated()).isEqualTo(created);
        assertThat(roleAssignment.getStatus()).isEqualTo("APPROVED");
        assertThat(roleAssignment.getClassification()).isEqualTo("PRIVATE");
        assertThat(roleAssignment.getActorId()).isEqualTo("actor123");
        assertThat(roleAssignment.getGrantType()).isEqualTo(GrantType.STANDARD);
        assertThat(roleAssignment.getRoleCategory()).isEqualTo(RoleCategory.JUDICIAL);
        assertThat(roleAssignment.getRoleName()).isEqualTo("judge");
        assertThat(roleAssignment.getRoleType()).isEqualTo(RoleType.CASE);
        assertThat(roleAssignment.isReadOnly()).isTrue();
    }

    @Test
    void builder_DefaultValues_ReturnsRoleAssignment() {
        // Act
        RoleAssignment roleAssignment = RoleAssignment.builder().id("test123").actorId("actor456").build();

        // Assert
        assertThat(roleAssignment).isNotNull();
        assertThat(roleAssignment.getId()).isEqualTo("test123");
        assertThat(roleAssignment.getActorIdType()).isEqualTo("IDAM");
        assertThat(roleAssignment.getStatus()).isEqualTo("CREATE_REQUESTED");
        assertThat(roleAssignment.getClassification()).isEqualTo("PUBLIC");
        assertThat(roleAssignment.getActorId()).isEqualTo("actor456");
        assertThat(roleAssignment.isReadOnly()).isFalse();
    }

    @Test
    void builder_EmptyBuild_ReturnsRoleAssignment() {
        // Act
        RoleAssignment roleAssignment = RoleAssignment.builder().build();

        // Assert
        assertThat(roleAssignment).isNotNull();
        assertThat(roleAssignment.getActorIdType()).isEqualTo("IDAM");
        assertThat(roleAssignment.getStatus()).isEqualTo("CREATE_REQUESTED");
        assertThat(roleAssignment.getClassification()).isEqualTo("PUBLIC");
        assertThat(roleAssignment.isReadOnly()).isFalse();
        assertThat(roleAssignment.getId()).isNull();
        assertThat(roleAssignment.getAttributes()).isNull();
        assertThat(roleAssignment.getAuthorisations()).isNull();
        assertThat(roleAssignment.getNotes()).isNull();
        assertThat(roleAssignment.getBeginTime()).isNull();
        assertThat(roleAssignment.getEndTime()).isNull();
        assertThat(roleAssignment.getCreated()).isNull();
        assertThat(roleAssignment.getActorId()).isNull();
        assertThat(roleAssignment.getGrantType()).isNull();
        assertThat(roleAssignment.getRoleCategory()).isNull();
        assertThat(roleAssignment.getRoleName()).isNull();
        assertThat(roleAssignment.getRoleType()).isNull();
    }

    @Test
    void allArgsConstructor_CreatesInstance() {
        // Arrange
        ZonedDateTime now = ZonedDateTime.now();
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("key", "value");
        List<String> authorisations = Collections.singletonList("auth");
        List<String> notes = Collections.singletonList("note");

        // Act
        RoleAssignment roleAssignment = new RoleAssignment(
            "id123",
            "IDAM",
            attributes,
            authorisations,
            notes,
            now,
            now.plusDays(1),
            now.minusDays(1),
            "ACTIVE",
            "PRIVATE",
            "actor789",
            GrantType.SPECIFIC,
            RoleCategory.LEGAL_OPERATIONS,
            "solicitor",
            RoleType.ORGANISATION,
            false
        );

        // Assert
        assertThat(roleAssignment).isNotNull();
        assertThat(roleAssignment.getId()).isEqualTo("id123");
        assertThat(roleAssignment.getActorIdType()).isEqualTo("IDAM");
        assertThat(roleAssignment.getAttributes()).isEqualTo(attributes);
        assertThat(roleAssignment.getAuthorisations()).isEqualTo(authorisations);
        assertThat(roleAssignment.getNotes()).isEqualTo(notes);
        assertThat(roleAssignment.getBeginTime()).isEqualTo(now);
        assertThat(roleAssignment.getEndTime()).isEqualTo(now.plusDays(1));
        assertThat(roleAssignment.getCreated()).isEqualTo(now.minusDays(1));
        assertThat(roleAssignment.getStatus()).isEqualTo("ACTIVE");
        assertThat(roleAssignment.getClassification()).isEqualTo("PRIVATE");
        assertThat(roleAssignment.getActorId()).isEqualTo("actor789");
        assertThat(roleAssignment.getGrantType()).isEqualTo(GrantType.SPECIFIC);
        assertThat(roleAssignment.getRoleCategory()).isEqualTo(RoleCategory.LEGAL_OPERATIONS);
        assertThat(roleAssignment.getRoleName()).isEqualTo("solicitor");
        assertThat(roleAssignment.getRoleType()).isEqualTo(RoleType.ORGANISATION);
        assertThat(roleAssignment.isReadOnly()).isFalse();
    }

    @Test
    void setters_UpdateFields() {
        // Arrange
        RoleAssignment roleAssignment = RoleAssignment.builder().build();
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("test", "value");
        List<String> authorisations = Arrays.asList("auth1", "auth2");
        List<String> notes = Arrays.asList("note1", "note2");

        // Act
        final ZonedDateTime now = ZonedDateTime.now();
        roleAssignment.setId("updated123");
        roleAssignment.setActorIdType("UPDATED_TYPE");
        roleAssignment.setAttributes(attributes);
        roleAssignment.setAuthorisations(authorisations);
        roleAssignment.setNotes(notes);
        roleAssignment.setBeginTime(now);
        roleAssignment.setEndTime(now.plusDays(30));
        roleAssignment.setCreated(now.minusDays(5));
        roleAssignment.setStatus("UPDATED");
        roleAssignment.setClassification("RESTRICTED");
        roleAssignment.setActorId("updatedActor");
        roleAssignment.setGrantType(GrantType.BASIC);
        roleAssignment.setRoleCategory(RoleCategory.CITIZEN);
        roleAssignment.setRoleName("updatedRole");
        roleAssignment.setRoleType(RoleType.CASE);
        roleAssignment.setReadOnly(true);

        // Assert
        assertThat(roleAssignment.getId()).isEqualTo("updated123");
        assertThat(roleAssignment.getActorIdType()).isEqualTo("UPDATED_TYPE");
        assertThat(roleAssignment.getAttributes()).isEqualTo(attributes);
        assertThat(roleAssignment.getAuthorisations()).isEqualTo(authorisations);
        assertThat(roleAssignment.getNotes()).isEqualTo(notes);
        assertThat(roleAssignment.getBeginTime()).isEqualTo(now);
        assertThat(roleAssignment.getEndTime()).isEqualTo(now.plusDays(30));
        assertThat(roleAssignment.getCreated()).isEqualTo(now.minusDays(5));
        assertThat(roleAssignment.getStatus()).isEqualTo("UPDATED");
        assertThat(roleAssignment.getClassification()).isEqualTo("RESTRICTED");
        assertThat(roleAssignment.getActorId()).isEqualTo("updatedActor");
        assertThat(roleAssignment.getGrantType()).isEqualTo(GrantType.BASIC);
        assertThat(roleAssignment.getRoleCategory()).isEqualTo(RoleCategory.CITIZEN);
        assertThat(roleAssignment.getRoleName()).isEqualTo("updatedRole");
        assertThat(roleAssignment.getRoleType()).isEqualTo(RoleType.CASE);
        assertThat(roleAssignment.isReadOnly()).isTrue();
    }

    @Test
    void equals_SameValues_ReturnsTrue() {
        // Arrange
        ZonedDateTime now = ZonedDateTime.now();
        RoleAssignment assignment1 = RoleAssignment.builder().id("same123").actorId("sameActor").beginTime(now).roleName(
            "sameRole").build();

        RoleAssignment assignment2 = RoleAssignment.builder().id("same123").actorId("sameActor").beginTime(now).roleName(
            "sameRole").build();

        // Act & Assert
        assertThat(assignment1).isEqualTo(assignment2);
        assertThat(assignment1.hashCode()).hasSameHashCodeAs(assignment2.hashCode());
    }

    @Test
    void equals_DifferentValues_ReturnsFalse() {
        // Arrange
        RoleAssignment assignment1 = RoleAssignment.builder().id("id1").actorId("actor1").build();

        RoleAssignment assignment2 = RoleAssignment.builder().id("id2").actorId("actor2").build();

        // Act & Assert
        assertThat(assignment1).isNotEqualTo(assignment2);
    }

    @Test
    void toString_ReturnsFormattedString() {
        // Arrange
        RoleAssignment roleAssignment = RoleAssignment.builder().id("toString123").actorId("toStringActor").roleName(
            "toStringRole").status("ACTIVE").build();

        // Act
        String toString = roleAssignment.toString();

        // Assert
        for (String s : Arrays.asList(
            "RoleAssignment",
            "id=toString123",
            "actorId=toStringActor",
            "roleName=toStringRole",
            "status=ACTIVE"
        )) {
            assertThat(toString).contains(s);
        }
    }

    @Test
    void jsonSerialization_AllFields_SerializesCorrectly() throws Exception {
        // Arrange
        ZonedDateTime beginTime = ZonedDateTime.of(2023, 12, 25, 10, 0, 0, 0, ZoneId.of("UTC"));
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("jurisdiction", "CIVIL");

        RoleAssignment roleAssignment = RoleAssignment.builder().id("json123").actorIdType("IDAM").attributes(attributes).authorisations(
            Arrays.asList("auth1", "auth2")).notes(Collections.singletonList("JSON note")).beginTime(beginTime).status(
            "CREATE_REQUESTED").classification("PUBLIC").actorId("jsonActor").grantType(GrantType.STANDARD).roleCategory(
            RoleCategory.JUDICIAL).roleName("jsonRole").roleType(RoleType.CASE).readOnly(false).build();

        // Act
        String json = objectMapper.writeValueAsString(roleAssignment);

        // Assert
        for (String s : Arrays.asList(
            "\"id\":\"json123\"",
            "\"actorIdType\":\"IDAM\"",
            "\"attributes\":{\"jurisdiction\":\"CIVIL\"}",
            "\"authorisations\":[\"auth1\",\"auth2\"]",
            "\"notes\":[\"JSON note\"]",
            "\"beginTime\":\"2023-12-25T10:00:00Z\"",
            "\"status\":\"CREATE_REQUESTED\"",
            "\"classification\":\"PUBLIC\"",
            "\"actorId\":\"jsonActor\"",
            "\"grantType\":\"STANDARD\"",
            "\"roleCategory\":\"JUDICIAL\"",
            "\"roleName\":\"jsonRole\"",
            "\"roleType\":\"CASE\"",
            "\"readOnly\":false"
        )) {
            assertThat(json).contains(s);
        }
    }

    @Test
    void jsonSerialization_NullFields_ExcludesNulls() throws Exception {
        // Arrange
        RoleAssignment roleAssignment = RoleAssignment.builder().id("minimal123").actorId("minimalActor").build();

        // Act
        String json = objectMapper.writeValueAsString(roleAssignment);

        // Assert
        for (String s : Arrays.asList("\"id\":\"minimal123\"", "\"actorId\":\"minimalActor\"")) {
            assertThat(json).contains(s);
        }
        assertThat(json).doesNotContain("\"attributes\":null")
            .doesNotContain("\"authorisations\":null")
            .doesNotContain("\"notes\":null")
            .doesNotContain("\"beginTime\":null")
            .doesNotContain("\"endTime\":null")
            .doesNotContain("\"created\":null")
            .doesNotContain("\"grantType\":null")
            .doesNotContain("\"roleCategory\":null")
            .doesNotContain("\"roleType\":null");
    }

    @Test
    void builder_ComplexAttributes_ReturnsRoleAssignment() {
        // Arrange
        Map<String, Object> complexAttributes = new HashMap<>();
        complexAttributes.put("string", "value");
        complexAttributes.put("number", 123);
        complexAttributes.put("boolean", true);
        complexAttributes.put("list", Arrays.asList("item1", "item2"));

        Map<String, String> nestedMap = new HashMap<>();
        nestedMap.put("nested", "value");
        complexAttributes.put("map", nestedMap);

        // Act
        RoleAssignment roleAssignment = RoleAssignment.builder().id("complex123").attributes(complexAttributes).build();

        // Assert
        assertThat(roleAssignment).isNotNull();
        assertThat(roleAssignment.getAttributes()).hasSize(5)
            .containsEntry("string", "value")
            .containsEntry("number", 123)
            .containsEntry("boolean", true);
        assertThat(roleAssignment.getAttributes().get("list")).isInstanceOf(List.class);
        assertThat(roleAssignment.getAttributes().get("map")).isInstanceOf(Map.class);
    }

    @Test
    void builder_EmptyCollections_ReturnsRoleAssignment() {
        // Act
        RoleAssignment roleAssignment = RoleAssignment.builder().id("empty123").authorisations(Collections.emptyList()).notes(
            Collections.emptyList()).attributes(Collections.emptyMap()).build();

        // Assert
        assertThat(roleAssignment).isNotNull();
        assertThat(roleAssignment.getAuthorisations()).isEmpty();
        assertThat(roleAssignment.getNotes()).isEmpty();
        assertThat(roleAssignment.getAttributes()).isEmpty();
    }
}
