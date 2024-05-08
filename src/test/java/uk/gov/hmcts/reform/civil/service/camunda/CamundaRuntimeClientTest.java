package uk.gov.hmcts.reform.civil.service.camunda;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = {
    JacksonAutoConfiguration.class,
    CamundaRuntimeClient.class
})
class CamundaRuntimeClientTest {

    @MockBean
    private CamundaRuntimeApi camundaApi;

    @MockBean
    private AuthTokenGenerator authTokenGenerator;

    @Autowired
    private CamundaRuntimeClient camundaClient;

    @Autowired
    ObjectMapper mapper;

    @Test
    void shouldReturnExpectedParsedResponse() {
        String authToken = "auth-token";
        String processInstanceId  = "process-instance-id";

        when(authTokenGenerator.generate()).thenReturn(authToken);
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

        Map actual = camundaClient.getProcessVariables(processInstanceId);

        Map expected = Map.of(
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
