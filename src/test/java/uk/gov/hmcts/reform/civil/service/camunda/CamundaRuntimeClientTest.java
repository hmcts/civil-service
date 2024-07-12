package uk.gov.hmcts.reform.civil.service.camunda;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
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
}
