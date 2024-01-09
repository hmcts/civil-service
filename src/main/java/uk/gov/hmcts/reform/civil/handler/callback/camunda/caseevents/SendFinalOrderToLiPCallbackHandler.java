package uk.gov.hmcts.reform.civil.handler.callback.camunda.caseevents;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.SendFinalOrderBulkPrintService;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.SEND_FINAL_ORDER_TO_LIP_CLAIMANT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.SEND_FINAL_ORDER_TO_LIP_DEFENDANT;

@Service
@RequiredArgsConstructor
public class SendFinalOrderToLiPCallbackHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = List.of(SEND_FINAL_ORDER_TO_LIP_DEFENDANT,
                                                          SEND_FINAL_ORDER_TO_LIP_CLAIMANT);
    public static final String TASK_ID_DEFENDANT = "SendFinalOrderToDefendantLIP";
    public static final String TASK_ID_CLAIMANT = "SendFinalOrderToClaimantLIP";
    private final SendFinalOrderBulkPrintService sendFinalOrderBulkPrintService;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_SUBMIT), this::sendFinalOrderLetter
        );
    }

    @Override
    public String camundaActivityId(CallbackParams callbackParams) {
        CaseEvent caseEvent = CaseEvent.valueOf(callbackParams.getRequest().getEventId());
        if (SEND_FINAL_ORDER_TO_LIP_DEFENDANT.equals(caseEvent)) {
            return TASK_ID_DEFENDANT;
        } else {
            return TASK_ID_CLAIMANT;
        }
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse sendFinalOrderLetter(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        sendFinalOrderBulkPrintService.sendFinalOrderToLIP(
            callbackParams.getParams().get(BEARER_TOKEN).toString(), caseData, camundaActivityId(callbackParams));
        return AboutToStartOrSubmitCallbackResponse.builder()
            .build();
    }
}
