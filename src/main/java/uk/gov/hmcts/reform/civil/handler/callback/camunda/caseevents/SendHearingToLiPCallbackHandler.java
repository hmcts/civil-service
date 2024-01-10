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
import uk.gov.hmcts.reform.civil.service.SendHearingBulkPrintService;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.SEND_HEARING_TO_LIP_CLAIMANT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.SEND_HEARING_TO_LIP_DEFENDANT;

@Service
@RequiredArgsConstructor
public class SendHearingToLiPCallbackHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = List.of(SEND_HEARING_TO_LIP_DEFENDANT,
                                                          SEND_HEARING_TO_LIP_CLAIMANT);
    public static final String TASK_ID_DEFENDANT = "SendHearingToDefendantLIP";
    public static final String TASK_ID_CLAIMANT = "SendHearingToClaimantLIP";
    private final SendHearingBulkPrintService sendHearingBulkPrintService;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_SUBMIT), this::sendHearingLetter
        );
    }

    @Override
    public String camundaActivityId(CallbackParams callbackParams) {
        CaseEvent caseEvent = CaseEvent.valueOf(callbackParams.getRequest().getEventId());
        if (SEND_HEARING_TO_LIP_DEFENDANT.equals(caseEvent)) {
            return TASK_ID_DEFENDANT;
        } else {
            return TASK_ID_CLAIMANT;
        }
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse sendHearingLetter(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        sendHearingBulkPrintService.sendHearingToLIP(
            callbackParams.getParams().get(BEARER_TOKEN).toString(), caseData, camundaActivityId(callbackParams));
        return AboutToStartOrSubmitCallbackResponse.builder()
            .build();
    }
}
