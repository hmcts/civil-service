package uk.gov.hmcts.reform.civil.service.hearingnotice;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.service.camunda.CamundaRuntimeClient;

@Service
@RequiredArgsConstructor
public class HearingNoticeCamundaService {

    private final CamundaRuntimeClient camundaClient;
    private final ObjectMapper mapper;

    public HearingNoticeVariables getProcessVariables(String processInstanceId) {
        return mapper.convertValue(camundaClient.getProcessVariables(processInstanceId), HearingNoticeVariables.class);
    }

    public void setProcessVariables(String processInstanceId, HearingNoticeVariables variables) {
        camundaClient.setProcessVariables(processInstanceId, variables.toMap(mapper));
    }

}
