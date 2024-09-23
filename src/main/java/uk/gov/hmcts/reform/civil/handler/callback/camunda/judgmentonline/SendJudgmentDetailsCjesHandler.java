package uk.gov.hmcts.reform.civil.handler.callback.camunda.judgmentonline;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.RuntimeService;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentSetAsideReason;
import uk.gov.hmcts.reform.civil.service.judgments.CjesService;
import java.util.List;
import java.util.Map;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.SEND_JUDGMENT_DETAILS_CJES;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.SEND_JUDGMENT_DETAILS_CJES_SA;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.unwrapElements;

@Service
@RequiredArgsConstructor
public class SendJudgmentDetailsCjesHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = List.of(SEND_JUDGMENT_DETAILS_CJES,
                                                          SEND_JUDGMENT_DETAILS_CJES_SA);
    public static final String TASK_ID = "SendJudgmentDetailsToCJES";
    private final ObjectMapper objectMapper;
    private final RuntimeService runTimeService;
    private final CjesService cjesService;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_SUBMIT), this::generatePayload
        );
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    CallbackResponse generatePayload(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = caseData.toBuilder();
        CaseEvent caseEvent = CaseEvent.valueOf(callbackParams.getRequest().getEventId());

        if (SEND_JUDGMENT_DETAILS_CJES.equals(caseEvent)) {
            updateCamundaVars(caseData);
            if (Boolean.TRUE.equals(isActiveJudgmentRegisteredWithRTL(caseData))) {
                cjesService.sendJudgment(caseData, true);
            }
        } else if (SEND_JUDGMENT_DETAILS_CJES_SA.equals(caseEvent)) {
            updateCamundaVarsSetAside(caseData);
            if (Boolean.TRUE.equals(isLatestHistoricJudgmentRegisteredWithRTL(caseData))) {
                cjesService.sendJudgment(caseData, false);
            }
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.build().toMap(objectMapper))
            .build();
    }

    private Boolean isActiveJudgmentRegisteredWithRTL(CaseData caseData) {
        return YES.equals(caseData.getActiveJudgment().getIsRegisterWithRTL());
    }

    private Boolean isLatestHistoricJudgmentRegisteredWithRTL(CaseData caseData) {
        if (caseData.getHistoricJudgment() != null && !caseData.getHistoricJudgment().isEmpty()) {
            return YES.equals((unwrapElements(caseData.getHistoricJudgment()).get(0)).getIsRegisterWithRTL());
        }
        throw new IllegalArgumentException("Historic judgement cannot be empty or null after a judgment is set aside");
    }

    private void updateCamundaVars(CaseData caseData) {
        if (caseData.getJoJudgmentRecordReason() != null) {
            runTimeService.setVariable(
                caseData.getBusinessProcess().getProcessInstanceId(),
                "judgmentRecordedReason", caseData.getJoJudgmentRecordReason().toString());
        }
    }

    private void updateCamundaVarsSetAside(CaseData caseData) {
        if (caseData.getJoSetAsideReason() != null) {
            runTimeService.setVariable(
                caseData.getBusinessProcess().getProcessInstanceId(),
                "JUDGMENT_SET_ASIDE_ERROR",
                caseData.getJoSetAsideReason().equals(JudgmentSetAsideReason.JUDGMENT_ERROR));
        }
    }
}
