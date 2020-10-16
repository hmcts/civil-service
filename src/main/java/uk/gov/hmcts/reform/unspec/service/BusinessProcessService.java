package uk.gov.hmcts.reform.unspec.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.unspec.callback.CaseEvent;
import uk.gov.hmcts.reform.unspec.model.BusinessProcess;
import uk.gov.hmcts.reform.unspec.model.CaseData;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.unspec.enums.BusinessProcessStatus.READY;

@Service
@RequiredArgsConstructor
public class BusinessProcessService {

    private static final String ERROR_MESSAGE = "Business Process Error";

    private final ObjectMapper mapper;

    public List<String> updateBusinessProcess(Map<String, Object> data, CaseEvent caseEvent) {
        CaseData caseData = mapper.convertValue(data, CaseData.class);
        if (caseData.hasNoOngoingBusinessProcess()) {
            data.put("businessProcess", BusinessProcess.builder().camundaEvent(caseEvent.name()).status(READY).build());
            return List.of();
        }
        return List.of(ERROR_MESSAGE);
    }
}
