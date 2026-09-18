package uk.gov.hmcts.reform.civil.service.camunda;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import uk.gov.hmcts.reform.civil.model.camunda.CamundaVariableValue;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CamundaRuntimeClientTest {

    @InjectMocks
    private CamundaRuntimeClient camundaClient;

    @Mock
    private CamundaRuntimeApi camundaApi;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    private final ObjectMapper mapper = new ObjectMapper();
    private String authToken;
    private String processInstanceId;

    @BeforeEach
    void setUp() {
        authToken = "auth-token";
        processInstanceId = "process-instance-id";
        when(authTokenGenerator.generate()).thenReturn(authToken);
    }

    @Test
    void shouldReturnExpectedParsedResponse() {
        when(camundaApi.getProcessVariables(processInstanceId, authToken)).thenReturn(mapper.convertValue(Map.of(
            "caseId", Map.of(
                "type", "Long",
                "value", "1713874015833902",
                "valueInfo", Map.of()),
            "hearingId", Map.of(
                "type", "String",
                "value", "2000005721",
                "valueInfo", Map.of()),
            "flowState", Map.of(
                "type", "String",
                "value", "MAIN.FULL_DEFENCE_PROCEED",
                "valueInfo", Map.of()),
            "flowFlags", Map.of(
                "type", "Object",
                "value", Map.of(
                    "BULK_CLAIM_ENABLED", true,
                    "SDO_ENABLED", true
                ),
                "valueInfo", Map.of(
                    "objectTypeName", "java.util.HashMap<java.lang.Object,java.lang.Object>",
                    "serializationDataFormat", "application/json"
                ))
        ), new TypeReference<>() {}));

        Map<String, Object> actual = camundaClient.getProcessVariables(processInstanceId);

        Map<String, Object> expected = Map.of(
            "caseId", "1713874015833902",
            "hearingId", "2000005721",
            "flowState", "MAIN.FULL_DEFENCE_PROCEED",
            "flowFlags", Map.of(
                "BULK_CLAIM_ENABLED", true,
                "SDO_ENABLED", true
            ));

        assertEquals(expected, actual);
    }

    @Test
    void shouldReturnEvaluatedCourtsWhenValid() {
        Map<String, Object> testMap = new HashMap<>();
        testMap.put("Trial", Map.of(
            "type", "String",
            "value", "123456",
            "valueInfo", Map.of()));
        testMap.put("CMC", Map.of(
            "type", "String",
            "value", "123456",
            "valueInfo", Map.of()));
        testMap.put("CCMC", Map.of(
            "type", "String",
            "value", "123456",
            "valueInfo", Map.of()));
        testMap.put("PTR", Map.of(
            "type", "String",
            "value", "123456",
            "valueInfo", Map.of()));
        List<Map<String, Object>> evaluation = List.of(testMap);

        when(camundaApi.evaluateDecision(any(), any(), any())).thenReturn(evaluation);

        Map<String, Object> actual = camundaClient.getEvaluatedDmnCourtLocations("123456", "MULTI_CLAIM");

        assertEquals(testMap, actual);
    }

    @Test
    void shouldReturnNullWhenNoCourtInEvaluatedDmn() {
        when(camundaApi.evaluateDecision(any(), any(), any())).thenReturn(Collections.emptyList());

        Map<String, Object> actual = camundaClient.getEvaluatedDmnCourtLocations("123456", "MULTI_CLAIM");

        assertNull(actual);
    }

    @ParameterizedTest
    @CsvSource({
        "aString,   String",
        "true,      Boolean",
        "42,        Integer",
        "9000000000, Long",
        "1.5,       Double"
    })
    void shouldSetScalarProcessVariableWithTheMatchingCamundaType(String raw, String expectedType) {
        Object value = switch (expectedType) {
            case "Boolean" -> Boolean.valueOf(raw);
            case "Integer" -> Integer.valueOf(raw);
            case "Long" -> Long.valueOf(raw);
            case "Double" -> Double.valueOf(raw);
            default -> raw;
        };

        camundaClient.setProcessVariable("proc-1", "aVariable", value);

        ArgumentCaptor<CamundaVariableValue> captor = ArgumentCaptor.forClass(CamundaVariableValue.class);
        verify(camundaApi).setProcessVariable(any(), eq("proc-1"), eq("aVariable"), captor.capture());
        assertThat(captor.getValue().getType()).isEqualTo(expectedType);
        assertThat(captor.getValue().getValue()).isEqualTo(value);
    }

    @Test
    void shouldSetNullProcessVariableAsNullType() {
        camundaClient.setProcessVariable("proc-1", "aVariable", null);

        ArgumentCaptor<CamundaVariableValue> captor = ArgumentCaptor.forClass(CamundaVariableValue.class);
        verify(camundaApi).setProcessVariable(any(), eq("proc-1"), eq("aVariable"), captor.capture());
        assertThat(captor.getValue().getType()).isEqualTo("Null");
        assertThat(captor.getValue().getValue()).isNull();
    }

    @Test
    void shouldRejectNonScalarProcessVariableRatherThanWriteAnUnreadableShape() {
        assertThatThrownBy(() -> camundaClient.setProcessVariable("proc-1", "hearingIds", List.of("h1", "h2")))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("hearingIds")
            .hasMessageContaining("Only scalar variables");
    }
}
