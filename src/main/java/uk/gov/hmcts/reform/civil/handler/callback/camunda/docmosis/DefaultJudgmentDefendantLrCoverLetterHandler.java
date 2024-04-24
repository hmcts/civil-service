package uk.gov.hmcts.reform.civil.handler.callback.camunda.docmosis;

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
import uk.gov.hmcts.reform.civil.service.docmosis.judgmentonline.DefaultJudgmentCoverLetterGenerator;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.SEND_COVER_LETTER_DEFENDANT_LR;

@Service
@RequiredArgsConstructor
public class DefaultJudgmentDefendantLrCoverLetterHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = List.of(
        SEND_COVER_LETTER_DEFENDANT_LR);
    public static final String TASK_ID = "SendCoverLetterToDefendantLR";
    private final DefaultJudgmentCoverLetterGenerator defaultJudgmentCoverLetterGenerator;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_SUBMIT),
            this::sendCoverLetterToDefendantLR
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

    private CallbackResponse sendCoverLetterToDefendantLR(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        if (YesOrNo.YES.equals(caseData.getRespondent1Represented())) {
            generateCoverLetterDefendantLr(callbackParams);
        }
        return AboutToStartOrSubmitCallbackResponse.builder().build();
    }

    private void generateCoverLetterDefendantLr(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        defaultJudgmentCoverLetterGenerator.generateAndPrintDjCoverLetter(caseData, callbackParams.getParams().get(BEARER_TOKEN).toString());
    }
}
