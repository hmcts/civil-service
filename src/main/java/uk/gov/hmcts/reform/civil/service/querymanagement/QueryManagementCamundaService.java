package uk.gov.hmcts.reform.civil.service.querymanagement;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.RuntimeService;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.service.camunda.CamundaRuntimeClient;

@Service
@RequiredArgsConstructor
public class QueryManagementCamundaService {

    private final RuntimeService runtimeService;
    private final CamundaRuntimeClient camundaClient;
    private final ObjectMapper mapper;

    public QueryManagementVariables getProcessVariables(String processInstanceId) {
        return mapper.convertValue(camundaClient.getProcessVariables(processInstanceId), QueryManagementVariables.class);
    }

    public void setProcessVariables(String processInstanceId, QueryManagementVariables variables) {
        runtimeService.setVariables(processInstanceId, variables.toMap(mapper));
    }

}
