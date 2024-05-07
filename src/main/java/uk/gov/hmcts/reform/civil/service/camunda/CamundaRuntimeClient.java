package uk.gov.hmcts.reform.civil.service.camunda;

import lombok.RequiredArgsConstructor;
import org.camunda.community.rest.client.model.VariableValueDto;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import java.util.HashMap;
import java.util.Map;

/*
* There is currently an issue with the getVariables with using the RunTimeService in org.camunda.community.
* This class was created to handle retrieving camunda process variables directly.
* */
@Component
@RequiredArgsConstructor
public class CamundaRuntimeClient {

    private final AuthTokenGenerator authTokenGenerator;
    private final CamundaRuntimeApi camundaRestEngineApi;

    @SuppressWarnings("")
    public Map<String, Object> getProcessVariables(String processInstanceId) {
        HashMap<String, VariableValueDto> variablesResponse = camundaRestEngineApi.getProcessVariables(processInstanceId, authTokenGenerator.generate());
        HashMap parsedResponse = new HashMap<String, Object>();
        variablesResponse.entrySet().stream().forEach(entry -> parsedResponse.put(entry.getKey(), entry.getValue().getValue()));
        return parsedResponse;
    }
}
