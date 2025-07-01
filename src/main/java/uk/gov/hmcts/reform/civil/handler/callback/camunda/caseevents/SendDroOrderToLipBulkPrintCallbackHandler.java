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
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.SendHearingBulkPrintService;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.SEND_DRO_ORDER_TO_LIP_CLAIMANT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.SEND_DRO_ORDER_TO_LIP_DEFENDANT;

@Service
@RequiredArgsConstructor
public class SendDroOrderToLipBulkPrintCallbackHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = List.of(SEND_DRO_ORDER_TO_LIP_CLAIMANT,
                                                          SEND_DRO_ORDER_TO_LIP_DEFENDANT);
    public static final String TASK_ID_DEFENDANT_DRO = "SendToDefendantLIP";
    public static final String TASK_ID_CLAIMANT_DRO = "SendDORToClaimantLIP";
    private final SendHearingBulkPrintService sendDroBulkPrintService;
    private final FeatureToggleService featureToggleService;

    @Override
    protected Map<String, Callback> callbacks() {
        return featureToggleService.isGaForWelshEnabled()
            ? Map.of(callbackKey(ABOUT_TO_SUBMIT), this::sendDroDocument)
            : Map.of(callbackKey(ABOUT_TO_SUBMIT), this::emptyCallbackResponse);
    }

    @Override
    public String camundaActivityId(CallbackParams callbackParams) {
        CaseEvent caseEvent = CaseEvent.valueOf(callbackParams.getRequest().getEventId());

        return switch (caseEvent) {
            case SEND_DRO_ORDER_TO_LIP_DEFENDANT -> TASK_ID_DEFENDANT_DRO;
            default -> TASK_ID_CLAIMANT_DRO;
        };
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse sendDroDocument(CallbackParams callbackParams) {

        CaseData caseData = callbackParams.getCaseData();
        String taskId = camundaActivityId(callbackParams);
        if (featureToggleService.isGaForWelshEnabled()) {
            sendDroBulkPrintService.sendDecisionReconsiderationToLip(
                callbackParams.getParams().get(BEARER_TOKEN).toString(), caseData, taskId);

        }
        return AboutToStartOrSubmitCallbackResponse.builder()
            .build();
    }

}
