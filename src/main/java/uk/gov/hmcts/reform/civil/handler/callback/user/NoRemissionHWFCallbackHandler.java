package uk.gov.hmcts.reform.civil.handler.callback.user;

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
import uk.gov.hmcts.reform.civil.model.citizenui.HelpWithFeesDetails;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NO_REMISSION_HWF;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_LIP_CLAIMANT_HWF_OUTCOME;

@Service
@RequiredArgsConstructor
public class NoRemissionHWFCallbackHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = List.of(NO_REMISSION_HWF);
    private final ObjectMapper objectMapper;
    private final Map<String, Callback> callbackMap = Map.of(
        callbackKey(ABOUT_TO_SUBMIT),
        this::noRemissionHWF,
        callbackKey(SUBMITTED), this::emptySubmittedCallbackResponse
    );

    @Override
    protected Map<String, Callback> callbacks() {
        return callbackMap;
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse noRemissionHWF(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        CaseData updatedData = caseData.toBuilder()
            .businessProcess(BusinessProcess.ready(NOTIFY_LIP_CLAIMANT_HWF_OUTCOME))
            .claimIssuedHwfDetails(HelpWithFeesDetails.builder().hwfCaseEvent(NO_REMISSION_HWF).build())
            .build();
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(updatedData.toMap(objectMapper))
            .build();
    }
}
