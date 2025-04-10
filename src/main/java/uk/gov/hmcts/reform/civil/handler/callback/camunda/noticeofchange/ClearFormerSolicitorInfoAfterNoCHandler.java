package uk.gov.hmcts.reform.civil.handler.callback.camunda.noticeofchange;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.utils.NocNotificationUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.UPDATE_CASE_DETAILS_AFTER_NOC;

@Service
@RequiredArgsConstructor
public class ClearFormerSolicitorInfoAfterNoCHandler extends CallbackHandler {

    public static final String TASK_ID = "ClearFormerSolicitorInfoAfterNotifyNoC";
    private final ObjectMapper objectMapper;
    private static final List<CaseEvent> EVENTS = Collections.singletonList(UPDATE_CASE_DETAILS_AFTER_NOC);

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_SUBMIT), this::clearFormerSolicitorInfo
        );
    }

    @Override
    public String camundaActivityId(CallbackParams callbackParams) {
        return TASK_ID;
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse clearFormerSolicitorInfo(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        Map<String, Object> updatedCaseData = NocNotificationUtils.getCaseDataWithoutFormerSolicitorEmail(caseData)
            .toMap(objectMapper);
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(updatedCaseData)
            .build();
    }
}
