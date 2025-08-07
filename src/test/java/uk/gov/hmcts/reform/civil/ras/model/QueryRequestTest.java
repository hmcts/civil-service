package uk.gov.hmcts.reform.civil.ras.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class QueryRequestTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Test
    void builder_AllFields_ReturnsQueryRequest() {
        // Arrange
        LocalDateTime now = LocalDateTime.of(2023, 12, 25, 10, 30, 0);
        Map<String, List<String>> attributes = new HashMap<>();
        attributes.put("jurisdiction", Arrays.asList("PRIVATELAW", "PUBLICLAW"));
        attributes.put("caseType", Collections.singletonList("CARE_SUPERVISION_EPO"));

        // Act
        QueryRequest queryRequest = QueryRequest.builder()
            .actorId("actor123")
            .actorId("actor456")
            .roleType("CASE")
            .roleType("ORGANISATION")
            .roleName("judge")
            .roleName("legal-adviser")
            .classification("PUBLIC")
            .classification("PRIVATE")
            .grantType("STANDARD")
            .grantType("SPECIFIC")
            .validAt(now)
            .roleCategory("JUDICIAL")
            .roleCategory("LEGAL_OPERATIONS")
            .attributes(attributes)
            .authorisations("auth1")
            .authorisations("auth2")
            .hasAttributes("substantive")
            .hasAttributes("caseId")
            .readOnly(true)
            .build();

        // Assert
        assertThat(queryRequest).isNotNull();
        assertThat(queryRequest.getActorId()).hasSize(2).containsExactly("actor123", "actor456");
        assertThat(queryRequest.getRoleType()).hasSize(2).containsExactly("CASE", "ORGANISATION");
        assertThat(queryRequest.getRoleName()).hasSize(2).containsExactly("judge", "legal-adviser");
        assertThat(queryRequest.getClassification()).hasSize(2).containsExactly("PUBLIC", "PRIVATE");
        assertThat(queryRequest.getGrantType()).hasSize(2).containsExactly("STANDARD", "SPECIFIC");
        assertThat(queryRequest.getValidAt()).isEqualTo(now);
        assertThat(queryRequest.getRoleCategory()).hasSize(2).containsExactly("JUDICIAL", "LEGAL_OPERATIONS");
        assertThat(queryRequest.getAttributes()).isEqualTo(attributes);
        assertThat(queryRequest.getAuthorisations()).hasSize(2).containsExactly("auth1", "auth2");
        assertThat(queryRequest.getHasAttributes()).hasSize(2).containsExactly("substantive", "caseId");
        assertThat(queryRequest.getReadOnly()).isTrue();
    }

    @Test
    void builder_SingleValues_ReturnsQueryRequest() {
        // Act
        QueryRequest queryRequest = QueryRequest.builder()
            .actorId("singleActor")
            .roleType("CASE")
            .roleName("solicitor")
            .classification("PUBLIC")
            .grantType("BASIC")
            .roleCategory("PROFESSIONAL")
            .authorisations("singleAuth")
            .hasAttributes("jurisdiction")
            .readOnly(false)
            .build();

        // Assert
        assertThat(queryRequest).isNotNull();
        assertThat(queryRequest.getActorId()).hasSize(1).containsExactly("singleActor");
        assertThat(queryRequest.getRoleType()).hasSize(1).containsExactly("CASE");
        assertThat(queryRequest.getRoleName()).hasSize(1).containsExactly("solicitor");
        assertThat(queryRequest.getClassification()).hasSize(1).containsExactly("PUBLIC");
        assertThat(queryRequest.getGrantType()).hasSize(1).containsExactly("BASIC");
        assertThat(queryRequest.getRoleCategory()).hasSize(1).containsExactly("PROFESSIONAL");
        assertThat(queryRequest.getAuthorisations()).hasSize(1).containsExactly("singleAuth");
        assertThat(queryRequest.getHasAttributes()).hasSize(1).containsExactly("jurisdiction");
        assertThat(queryRequest.getReadOnly()).isFalse();
    }

    @Test
    void builder_EmptyLists_ReturnsQueryRequest() {
        // Act
        QueryRequest queryRequest = QueryRequest.builder().build();

        // Assert
        assertThat(queryRequest).isNotNull();
        assertThat(queryRequest.getActorId()).isEmpty();
        assertThat(queryRequest.getRoleType()).isEmpty();
        assertThat(queryRequest.getRoleName()).isEmpty();
        assertThat(queryRequest.getClassification()).isEmpty();
        assertThat(queryRequest.getGrantType()).isEmpty();
        assertThat(queryRequest.getValidAt()).isNull();
        assertThat(queryRequest.getRoleCategory()).isEmpty();
        assertThat(queryRequest.getAttributes()).isNull();
        assertThat(queryRequest.getAuthorisations()).isEmpty();
        assertThat(queryRequest.getHasAttributes()).isEmpty();
        assertThat(queryRequest.getReadOnly()).isNull();
    }

    @Test
    void builder_WithNullValues_ReturnsQueryRequest() {
        // Act
        QueryRequest queryRequest = QueryRequest.builder()
            .validAt(null)
            .attributes(null)
            .readOnly(null)
            .build();

        // Assert
        assertThat(queryRequest).isNotNull();
        assertThat(queryRequest.getValidAt()).isNull();
        assertThat(queryRequest.getAttributes()).isNull();
        assertThat(queryRequest.getReadOnly()).isNull();
    }

    @Test
    void allArgsConstructor_CreatesInstance() {
        // Arrange
        List<String> actorIds = Arrays.asList("actor1", "actor2");
        List<String> roleTypes = Collections.singletonList("CASE");
        List<String> roleNames = Arrays.asList("judge", "clerk");
        List<String> classifications = Collections.singletonList("PUBLIC");
        List<String> grantTypes = Collections.singletonList("STANDARD");
        LocalDateTime validAt = LocalDateTime.now();
        List<String> roleCategories = Collections.singletonList("JUDICIAL");
        Map<String, List<String>> attributes = new HashMap<>();
        attributes.put("key", Collections.singletonList("value"));
        List<String> authorisations = Collections.singletonList("auth");
        List<String> hasAttributes = Collections.singletonList("attr");

        // Act
        QueryRequest queryRequest = new QueryRequest(
            actorIds, roleTypes, roleNames, classifications, grantTypes,
            validAt, roleCategories, attributes, authorisations, hasAttributes, true
        );

        // Assert
        assertThat(queryRequest).isNotNull();
        assertThat(queryRequest.getActorId()).isEqualTo(actorIds);
        assertThat(queryRequest.getRoleType()).isEqualTo(roleTypes);
        assertThat(queryRequest.getRoleName()).isEqualTo(roleNames);
        assertThat(queryRequest.getClassification()).isEqualTo(classifications);
        assertThat(queryRequest.getGrantType()).isEqualTo(grantTypes);
        assertThat(queryRequest.getValidAt()).isEqualTo(validAt);
        assertThat(queryRequest.getRoleCategory()).isEqualTo(roleCategories);
        assertThat(queryRequest.getAttributes()).isEqualTo(attributes);
        assertThat(queryRequest.getAuthorisations()).isEqualTo(authorisations);
        assertThat(queryRequest.getHasAttributes()).isEqualTo(hasAttributes);
        assertThat(queryRequest.getReadOnly()).isTrue();
    }

    @Test
    void equals_SameValues_ReturnsTrue() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        QueryRequest request1 = QueryRequest.builder()
            .actorId("actor1")
            .roleType("CASE")
            .validAt(now)
            .readOnly(true)
            .build();

        QueryRequest request2 = QueryRequest.builder()
            .actorId("actor1")
            .roleType("CASE")
            .validAt(now)
            .readOnly(true)
            .build();

        // Act & Assert
        assertThat(request1).isEqualTo(request2);
        assertThat(request1.hashCode()).hasSameHashCodeAs(request2.hashCode());
    }

    @Test
    void equals_DifferentValues_ReturnsFalse() {
        // Arrange
        QueryRequest request1 = QueryRequest.builder()
            .actorId("actor1")
            .roleType("CASE")
            .build();

        QueryRequest request2 = QueryRequest.builder()
            .actorId("actor2")
            .roleType("ORGANISATION")
            .build();

        // Act & Assert
        assertThat(request1).isNotEqualTo(request2);
    }

    @Test
    void toString_ReturnsFormattedString() {
        // Arrange
        QueryRequest queryRequest = QueryRequest.builder()
            .actorId("testActor")
            .roleType("CASE")
            .roleName("testRole")
            .readOnly(false)
            .build();

        // Act
        String toString = queryRequest.toString();

        // Assert
        for (String s : Arrays.asList(
            "QueryRequest",
            "actorId=[testActor]",
            "roleType=[CASE]",
            "roleName=[testRole]",
            "readOnly=false"
        )) {
            assertThat(toString).contains(s);
        }
    }

    @Test
    void valueAnnotation_CreatesImmutableObject() {
        // Arrange
        QueryRequest queryRequest = QueryRequest.builder()
            .actorId("immutableActor")
            .build();

        // Act & Assert
        // Since @Value makes fields final, we can't modify them
        // The lists returned should be unmodifiable
        assertThat(queryRequest.getActorId()).containsExactly("immutableActor")
            .isUnmodifiable();
    }

    @Test
    void builder_ComplexAttributes_ReturnsQueryRequest() {
        // Arrange
        Map<String, List<String>> complexAttributes = new HashMap<>();
        complexAttributes.put("jurisdiction", Arrays.asList("CIVIL", "CRIMINAL", "FAMILY"));
        complexAttributes.put("caseType", Arrays.asList("type1", "type2"));
        complexAttributes.put("region", Collections.singletonList("North"));

        // Act
        QueryRequest queryRequest = QueryRequest.builder()
            .attributes(complexAttributes)
            .build();

        // Assert
        assertThat(queryRequest).isNotNull();
        assertThat(queryRequest.getAttributes()).hasSize(3);
        assertThat(queryRequest.getAttributes().get("jurisdiction")).hasSize(3);
        assertThat(queryRequest.getAttributes().get("caseType")).hasSize(2);
        assertThat(queryRequest.getAttributes().get("region")).hasSize(1);
    }

    @Test
    void jsonSerialization_AllFields_SerializesCorrectly() throws Exception {
        // Arrange
        LocalDateTime now = LocalDateTime.of(2023, 12, 25, 15, 45, 30);
        Map<String, List<String>> attributes = new HashMap<>();
        attributes.put("key1", Collections.singletonList("value1"));

        QueryRequest queryRequest = QueryRequest.builder()
            .actorId("jsonActor")
            .roleType("CASE")
            .roleName("jsonRole")
            .classification("PUBLIC")
            .grantType("STANDARD")
            .validAt(now)
            .roleCategory("JUDICIAL")
            .attributes(attributes)
            .authorisations("jsonAuth")
            .hasAttributes("jsonAttr")
            .readOnly(true)
            .build();

        // Act
        String json = objectMapper.writeValueAsString(queryRequest);

        // Assert
        for (String s : Arrays.asList(
            "\"actorId\":[\"jsonActor\"]",
            "\"roleType\":[\"CASE\"]",
            "\"roleName\":[\"jsonRole\"]",
            "\"classification\":[\"PUBLIC\"]",
            "\"grantType\":[\"STANDARD\"]",
            "\"validAt\":\"2023-12-25T15:45:30\"",
            "\"roleCategory\":[\"JUDICIAL\"]",
            "\"attributes\"",
            "\"authorisations\":[\"jsonAuth\"]",
            "\"hasAttributes\":[\"jsonAttr\"]",
            "\"readOnly\":true"
        )) {
            assertThat(json).contains(s);
        }
    }

    @Test
    void jsonSerialization_EmptyLists_SerializesCorrectly() throws Exception {
        // Arrange
        QueryRequest queryRequest = QueryRequest.builder().build();

        // Act
        String json = objectMapper.writeValueAsString(queryRequest);

        // Assert
        for (String s : Arrays.asList(
            "\"actorId\":[]",
            "\"roleType\":[]",
            "\"roleName\":[]",
            "\"classification\":[]",
            "\"grantType\":[]",
            "\"roleCategory\":[]",
            "\"authorisations\":[]",
            "\"hasAttributes\":[]"
        )) {
            assertThat(json).contains(s);
        }
    }

    @Test
    void builder_MultipleSingularCalls_AccumulatesValues() {
        // Act
        QueryRequest queryRequest = QueryRequest.builder()
            .actorId("actor1")
            .actorId("actor2")
            .actorId("actor3")
            .roleType("type1")
            .roleType("type2")
            .roleName("name1")
            .roleName("name2")
            .roleName("name3")
            .roleName("name4")
            .build();

        // Assert
        assertThat(queryRequest.getActorId()).hasSize(3).containsExactly("actor1", "actor2", "actor3");
        assertThat(queryRequest.getRoleType()).hasSize(2).containsExactly("type1", "type2");
        assertThat(queryRequest.getRoleName()).hasSize(4).containsExactly("name1", "name2", "name3", "name4");
    }

    @Test
    void builder_EmptyStringValues_ReturnsQueryRequest() {
        // Act
        QueryRequest queryRequest = QueryRequest.builder()
            .actorId("")
            .roleType("")
            .roleName("")
            .classification("")
            .grantType("")
            .roleCategory("")
            .authorisations("")
            .hasAttributes("")
            .build();

        // Assert
        assertThat(queryRequest).isNotNull();
        assertThat(queryRequest.getActorId()).hasSize(1).containsExactly("");
        assertThat(queryRequest.getRoleType()).hasSize(1).containsExactly("");
        assertThat(queryRequest.getRoleName()).hasSize(1).containsExactly("");
        assertThat(queryRequest.getClassification()).hasSize(1).containsExactly("");
        assertThat(queryRequest.getGrantType()).hasSize(1).containsExactly("");
        assertThat(queryRequest.getRoleCategory()).hasSize(1).containsExactly("");
        assertThat(queryRequest.getAuthorisations()).hasSize(1).containsExactly("");
        assertThat(queryRequest.getHasAttributes()).hasSize(1).containsExactly("");
    }

    @Test
    void getter_ReturnsUnmodifiableCollections() {
        // Arrange
        QueryRequest queryRequest = QueryRequest.builder()
            .actorId("test")
            .roleType("CASE")
            .build();

        // Act & Assert
        assertThat(queryRequest.getActorId()).isUnmodifiable();
        assertThat(queryRequest.getRoleType()).isUnmodifiable();
        assertThat(queryRequest.getRoleName()).isUnmodifiable();
        assertThat(queryRequest.getClassification()).isUnmodifiable();
        assertThat(queryRequest.getGrantType()).isUnmodifiable();
        assertThat(queryRequest.getRoleCategory()).isUnmodifiable();
        assertThat(queryRequest.getAuthorisations()).isUnmodifiable();
        assertThat(queryRequest.getHasAttributes()).isUnmodifiable();
    }
}
