package uk.gov.hmcts.reform.civil.handler.callback.hmc;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.UpdateNextHearingInfo;

@Service
@RequiredArgsConstructor
public class NextHearingDateSchedulerCallbackHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = List.of(UpdateNextHearingInfo);
    private static final String TASK_ID = "UpdateNextHearingInfo";

    private final ObjectMapper objectMapper;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_SUBMIT), this::aboutToSubmit,
            callbackKey(SUBMITTED), this::emptyCallbackResponse
        );
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    @Override
    public String camundaActivityId(CallbackParams callbackParams) {
        return TASK_ID;
    }

    private CallbackResponse aboutToSubmit(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData().toBuilder()
            .businessProcess(BusinessProcess.ready(UpdateNextHearingInfo))
            .build();
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseData.toMap(objectMapper))
            .build();
    }
}
