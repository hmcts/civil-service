package uk.gov.hmcts.reform.civil.service.hearingnotice;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.RuntimeService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class HearingNoticeCamundaService {

    private final RuntimeService runtimeService;
    private final ObjectMapper mapper;

    public HearingNoticeVariables getProcessVariables(String processInstanceId) {
        return mapper.convertValue(runtimeService.getVariables(processInstanceId), HearingNoticeVariables.class);
    }

    public void setProcessVariables(String processInstanceId, HearingNoticeVariables variables) {
        runtimeService.setVariables(processInstanceId, variables.toMap(mapper));
    }

}
