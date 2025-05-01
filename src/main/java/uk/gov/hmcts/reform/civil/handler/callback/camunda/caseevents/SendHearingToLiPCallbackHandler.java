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
import uk.gov.hmcts.reform.civil.utils.HmcDataUtils;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.SEND_HEARING_TO_LIP_CLAIMANT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.SEND_HEARING_TO_LIP_CLAIMANT_HMC;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.SEND_HEARING_TO_LIP_DEFENDANT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.SEND_HEARING_TO_LIP_DEFENDANT_HMC;

@Service
@RequiredArgsConstructor
public class SendHearingToLiPCallbackHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = List.of(SEND_HEARING_TO_LIP_DEFENDANT,
                                                          SEND_HEARING_TO_LIP_CLAIMANT,
                                                          SEND_HEARING_TO_LIP_DEFENDANT_HMC,
                                                          SEND_HEARING_TO_LIP_CLAIMANT_HMC);
    public static final String TASK_ID_DEFENDANT = "SendHearingToDefendantLIP";
    public static final String TASK_ID_CLAIMANT = "SendHearingToClaimantLIP";
    public static final String TASK_ID_DEFENDANT_HMC = "SendAutomaticHearingToDefendantLIP";
    public static final String TASK_ID_CLAIMANT_HMC = "SendAutomaticHearingToClaimantLIP";
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

        return switch (caseEvent) {
            case SEND_HEARING_TO_LIP_DEFENDANT -> TASK_ID_DEFENDANT;
            case SEND_HEARING_TO_LIP_DEFENDANT_HMC -> TASK_ID_DEFENDANT_HMC;
            case SEND_HEARING_TO_LIP_CLAIMANT_HMC -> TASK_ID_CLAIMANT_HMC;
            default -> TASK_ID_CLAIMANT;
        };
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse sendHearingLetter(CallbackParams callbackParams) {

        CaseData caseData = callbackParams.getCaseData();
        String task = camundaActivityId(callbackParams);
        sendHearingBulkPrintService.sendHearingToLIP(
            callbackParams.getParams().get(BEARER_TOKEN).toString(), caseData, task,
            featureToggleService.isHmcForLipEnabled() && sendWelshHearingToLip(task, caseData));
        return AboutToStartOrSubmitCallbackResponse.builder()
            .build();
    }

    private boolean sendWelshHearingToLip(String task, CaseData caseData) {
        return (isClaimantHMC(task) && HmcDataUtils.isWelshHearingTemplateClaimant(caseData))
            || (isDefendantHMC(task) && HmcDataUtils.isWelshHearingTemplateDefendant(caseData));
    }

    private boolean isClaimantHMC(String task) {
        return TASK_ID_CLAIMANT_HMC.equals(task);
    }

    private boolean isDefendantHMC(String task) {
        return TASK_ID_DEFENDANT_HMC.equals(task);
    }
}
