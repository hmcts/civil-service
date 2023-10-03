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
import uk.gov.hmcts.reform.civil.service.SendSDOBulkPrintService;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.SEND_SDO_ORDER_TO_LIP_DEFENDANT;

@Service
@RequiredArgsConstructor
public class SendSDOToLiPDefendantCallbackHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = List.of(SEND_SDO_ORDER_TO_LIP_DEFENDANT);
    public static final String TASK_ID = "SendSDOToDefendantLIP";
    private final SendSDOBulkPrintService sendSDOBulkPrintService;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_SUBMIT), this::sendSDOLetter
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

    private CallbackResponse sendSDOLetter(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        sendSDOBulkPrintService.sendSDOToDefendantLIP(callbackParams.getParams().get(BEARER_TOKEN).toString(), caseData);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .build();
    }

}
