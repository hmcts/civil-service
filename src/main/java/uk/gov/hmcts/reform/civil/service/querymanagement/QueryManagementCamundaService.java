package uk.gov.hmcts.reform.civil.service.querymanagement;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.service.camunda.CamundaRuntimeClient;

@Service
@RequiredArgsConstructor
public class QueryManagementCamundaService {

    private final CamundaRuntimeClient camundaClient;
    private final ObjectMapper mapper;

    public QueryManagementVariables getProcessVariables(String processInstanceId) {
        return mapper.convertValue(camundaClient.getProcessVariables(processInstanceId), QueryManagementVariables.class);
    }

    public void setProcessVariables(String processInstanceId, QueryManagementVariables variables) {
        camundaClient.setProcessVariables(processInstanceId, variables.toMap(mapper));
    }

}
