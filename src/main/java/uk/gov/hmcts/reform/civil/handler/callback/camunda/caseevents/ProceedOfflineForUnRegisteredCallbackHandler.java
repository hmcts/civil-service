package uk.gov.hmcts.reform.civil.handler.callback.camunda.caseevents;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;

@Service
public class ProceedOfflineForUnRegisteredCallbackHandler extends ProceedOfflineCallbackHandler {

    private static final String TASK_ID = "ProceedOfflineForUnregisteredFirm";

    @Override
    public String camundaActivityId(CallbackParams callbackParams) {
        return TASK_ID;
    }

    public ProceedOfflineForUnRegisteredCallbackHandler(ObjectMapper objectMapper) {
        super(objectMapper);
    }
}
