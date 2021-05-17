package uk.gov.hmcts.reform.civil.handler.callback.camunda.caseevents;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;

@Service
public class ProceedOfflineForUnRepresentedCallbackHandler extends ProceedOfflineCallbackHandler {

    private static final String TASK_ID = "ProceedOfflineForUnRepresentedSolicitor";

    @Override
    public String camundaActivityId(CallbackParams callbackParams) {
        return TASK_ID;
    }

    public ProceedOfflineForUnRepresentedCallbackHandler(ObjectMapper objectMapper) {
        super(objectMapper);
    }
}
