package uk.gov.hmcts.reform.civil.cas.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class DecisionRequestTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void builder_ValidCaseDetails_ReturnsDecisionRequest() {
        // Arrange
        CaseDetails caseDetails = CaseDetails.builder()
            .id(12345L)
            .jurisdiction("civil")
            .caseTypeId("CIVIL")
            .build();

        // Act
        DecisionRequest decisionRequest = DecisionRequest.builder()
            .caseDetails(caseDetails)
            .build();

        // Assert
        assertThat(decisionRequest).isNotNull();
        assertThat(decisionRequest.getCaseDetails()).isEqualTo(caseDetails);
        assertThat(decisionRequest.getCaseDetails().getId()).isEqualTo(12345L);
        assertThat(decisionRequest.getCaseDetails().getJurisdiction()).isEqualTo("civil");
        assertThat(decisionRequest.getCaseDetails().getCaseTypeId()).isEqualTo("CIVIL");
    }

    @Test
    void noArgsConstructor_WhenCalled_ReturnsEmptyDecisionRequest() {
        // Act
        DecisionRequest decisionRequest = new DecisionRequest();

        // Assert
        assertThat(decisionRequest).isNotNull();
        assertThat(decisionRequest.getCaseDetails()).isNull();
    }

    @Test
    void allArgsConstructor_ValidCaseDetails_ReturnsDecisionRequest() {
        // Arrange
        CaseDetails caseDetails = CaseDetails.builder()
            .id(67890L)
            .build();

        // Act
        DecisionRequest decisionRequest = new DecisionRequest(caseDetails);

        // Assert
        assertThat(decisionRequest).isNotNull();
        assertThat(decisionRequest.getCaseDetails()).isEqualTo(caseDetails);
    }

    @Test
    void decisionRequest_ValidCaseDetails_ReturnsDecisionRequest() {
        // Arrange
        CaseDetails caseDetails = CaseDetails.builder()
            .id(11111L)
            .jurisdiction("civil")
            .build();

        // Act
        DecisionRequest result = DecisionRequest.decisionRequest(caseDetails);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getCaseDetails()).isEqualTo(caseDetails);
        assertThat(result.getCaseDetails().getId()).isEqualTo(11111L);
    }

    @Test
    void decisionRequest_NullCaseDetails_ReturnsDecisionRequestWithNull() {
        // Act
        DecisionRequest result = DecisionRequest.decisionRequest(null);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getCaseDetails()).isNull();
    }

    @Test
    void setCaseDetails_ValidCaseDetails_UpdatesCaseDetails() {
        // Arrange
        DecisionRequest decisionRequest = new DecisionRequest();
        CaseDetails caseDetails = CaseDetails.builder()
            .id(99999L)
            .build();

        // Act
        decisionRequest.setCaseDetails(caseDetails);

        // Assert
        assertThat(decisionRequest.getCaseDetails()).isEqualTo(caseDetails);
    }

    @Test
    void equals_SameValues_ReturnsTrue() {
        // Arrange
        CaseDetails caseDetails1 = CaseDetails.builder().id(123L).build();
        CaseDetails caseDetails2 = CaseDetails.builder().id(123L).build();

        DecisionRequest request1 = DecisionRequest.builder().caseDetails(caseDetails1).build();
        DecisionRequest request2 = DecisionRequest.builder().caseDetails(caseDetails2).build();

        // Assert
        assertThat(request1).isEqualTo(request2);
        assertThat(request1.hashCode()).hasSameHashCodeAs(request2.hashCode());
    }

    @Test
    void equals_DifferentValues_ReturnsFalse() {
        // Arrange
        CaseDetails caseDetails1 = CaseDetails.builder().id(123L).build();
        CaseDetails caseDetails2 = CaseDetails.builder().id(456L).build();

        DecisionRequest request1 = DecisionRequest.builder().caseDetails(caseDetails1).build();
        DecisionRequest request2 = DecisionRequest.builder().caseDetails(caseDetails2).build();

        // Assert
        assertThat(request1).isNotEqualTo(request2).isNotNull()
            .isNotEqualTo(new Object());
    }

    @Test
    void toString_ValidObject_ReturnsStringRepresentation() {
        // Arrange
        CaseDetails caseDetails = CaseDetails.builder()
            .id(789L)
            .build();
        DecisionRequest decisionRequest = DecisionRequest.builder()
            .caseDetails(caseDetails)
            .build();

        // Act
        String result = decisionRequest.toString();

        // Assert
        assertThat(result).isNotNull()
            .contains("DecisionRequest")
            .contains("caseDetails");
    }

    @Test
    void serialization_ValidObject_SerializesCorrectly() throws Exception {
        // Arrange
        CaseDetails caseDetails = CaseDetails.builder()
            .id(555L)
            .jurisdiction("civil")
            .caseTypeId("CIVIL")
            .build();
        DecisionRequest original = DecisionRequest.builder()
            .caseDetails(caseDetails)
            .build();

        // Act
        String json = objectMapper.writeValueAsString(original);

        // Assert - JSON contains expected field with JsonProperty name
        assertThat(json).contains("case_details")
            .contains("555");
    }

    @Test
    void deserialization_ValidJson_DeserializesCorrectly() throws Exception {
        // Arrange
        String json = """
            {
                "case_details": {
                    "id": 555,
                    "jurisdiction": "civil"
                }
            }
            """;

        // Act
        DecisionRequest result = objectMapper.readValue(json, DecisionRequest.class);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getCaseDetails()).isNotNull();
        assertThat(result.getCaseDetails().getId()).isEqualTo(555L);
        assertThat(result.getCaseDetails().getJurisdiction()).isEqualTo("civil");
    }

    @Test
    void deserialization_JsonWithUnknownProperties_IgnoresUnknownProperties() throws Exception {
        // Arrange
        String jsonWithUnknownProperties = """
            {
                "case_details": {
                    "id": 777,
                    "jurisdiction": "civil"
                },
                "unknown_field": "unknown_value",
                "another_unknown": 123
            }
            """;

        // Act
        DecisionRequest result = objectMapper.readValue(jsonWithUnknownProperties, DecisionRequest.class);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getCaseDetails()).isNotNull();
        assertThat(result.getCaseDetails().getId()).isEqualTo(777L);
    }
}
