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
import uk.gov.hmcts.reform.civil.service.judgments.ReportJudgmentsService;
import java.util.List;
import java.util.Map;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.SEND_JUDGMENT_DETAILS_CJES;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.SEND_JUDGMENT_DETAILS_CJES_SA;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;

@Service
@RequiredArgsConstructor
public class SendJudgmentDetailsCjesHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = List.of(SEND_JUDGMENT_DETAILS_CJES,
                                                          SEND_JUDGMENT_DETAILS_CJES_SA);
    public static final String TASK_ID = "SendJudgmentDetailsToCJES";
    private final ObjectMapper objectMapper;
    private final RuntimeService runTimeService;
    private final ReportJudgmentsService reportJudgmentsService;

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

        if (YES.equals(caseData.getJoIsRegisteredWithRTL())) {
            if (SEND_JUDGMENT_DETAILS_CJES.equals(caseEvent)) {
                reportJudgmentsService.sendJudgment(caseData, true);
            } else if (SEND_JUDGMENT_DETAILS_CJES_SA.equals(caseEvent)) {
                reportJudgmentsService.sendJudgment(caseData, false);
            }
        }

        updateCamundaVars(caseData);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.build().toMap(objectMapper))
            .build();
    }

    private void updateCamundaVars(CaseData caseData) {
        runTimeService.setVariable(
            caseData.getBusinessProcess().getProcessInstanceId(),
            "judgmentRecordedReason",
            caseData.getJoJudgmentRecordReason().toString()
        );
    }
}
