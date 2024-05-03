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
import uk.gov.hmcts.reform.civil.service.docmosis.dj.RecordJudgmentDeterminationOfMeansPiPLetterGenerator;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.POST_JO_DEFENDANT1_PIN_IN_LETTER;

@Service
@RequiredArgsConstructor
public class RecordJudgmentDeterminationOfMeansLiPDefendant1LetterHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = List.of(
        POST_JO_DEFENDANT1_PIN_IN_LETTER);
    public static final String TASK_ID = "SendRecordJudgmentDeterminationOfMeansLiPLetterDef1";
    private final RecordJudgmentDeterminationOfMeansPiPLetterGenerator recordJudgmentDeterminationOfMeansPiPLetterGenerator;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_SUBMIT),
            this::sendRecordJudgmentDeterminationOfMeansLetterToLiPDefendant1
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

    private CallbackResponse sendRecordJudgmentDeterminationOfMeansLetterToLiPDefendant1(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        if (isRespondent1Lip(caseData) && caseData.getRespondent1() != null && caseData.getRespondent1().getPartyEmail() != null) {
            recordJudgmentDeterminationOfMeansPiPLetterGenerator
                .generateAndPrintRecordJudgmentDeterminationOfMeansLetter(caseData, callbackParams.getParams().get(BEARER_TOKEN).toString());
        }
        return AboutToStartOrSubmitCallbackResponse.builder().build();
    }

    private boolean isRespondent1Lip(CaseData caseData) {
        return YesOrNo.NO.equals(caseData.getRespondent1Represented());
    }

}
