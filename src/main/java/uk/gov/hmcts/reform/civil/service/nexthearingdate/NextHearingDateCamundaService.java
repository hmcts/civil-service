package uk.gov.hmcts.reform.civil.service.nexthearingdate;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.RuntimeService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NextHearingDateCamundaService {

    private final RuntimeService runtimeService;
    private final ObjectMapper mapper;

    public NextHearingDateVariables getProcessVariables(String processInstanceId) {
        return mapper.convertValue(runtimeService.getVariables(processInstanceId), NextHearingDateVariables.class);
    }

    public void setProcessVariables(String processInstanceId, NextHearingDateVariables variables) {
        runtimeService.setVariables(processInstanceId, variables.toMap(mapper));
    }
}
