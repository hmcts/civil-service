package uk.gov.hmcts.reform.civil.handler.callback.camunda.caseevents;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.SendHearingBulkPrintService;
import uk.gov.hmcts.reform.civil.utils.HmcDataUtils;

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
    private final FeatureToggleService featureToggleService;

    @Override
    protected Map<String, Callback> callbacks() {
        return featureToggleService.isCaseProgressionEnabled()
            ? Map.of(callbackKey(ABOUT_TO_SUBMIT), this::sendHearingLetter)
            : Map.of(callbackKey(ABOUT_TO_SUBMIT), this::emptyCallbackResponse);
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
        String task = camundaActivityId(callbackParams);
        if (isClaimantLip(task, caseData) || isDefendantLip(task, caseData)) {
            sendHearingBulkPrintService.sendHearingToLIP(
                callbackParams.getParams().get(BEARER_TOKEN).toString(), caseData, task,
                featureToggleService.isHmcForLipEnabled() && sendWelshHearingToLip(task, caseData));
        }
        return AboutToStartOrSubmitCallbackResponse.builder()
            .build();
    }

    private boolean sendWelshHearingToLip(String task, CaseData caseData) {
        return (isClaimant(task) && HmcDataUtils.isWelshHearingTemplateClaimant(caseData))
            || (isDefendant(task) && HmcDataUtils.isWelshHearingTemplateDefendant(caseData));
    }

    private boolean isClaimant(String task) {
        return TASK_ID_CLAIMANT.equals(task);
    }

    private boolean isDefendant(String task) {
        return TASK_ID_DEFENDANT.equals(task);
    }

    private boolean isClaimantLip(String task, CaseData caseData) {
        return isClaimant(task) && YesOrNo.NO.equals(caseData.getApplicant1Represented());
    }

    private boolean isDefendantLip(String task, CaseData caseData) {
        return isDefendant(task) && caseData.isRespondent1NotRepresented();
    }
}
