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
import uk.gov.hmcts.reform.civil.service.docmosis.judgmentonline.SetAsideJudgmentInErrorLiPLetterGenerator;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.SEND_SET_ASIDE_JUDGEMENT_IN_ERROR_LETTER_TO_LIP_DEFENDANT1;

@Service
@RequiredArgsConstructor
public class SetAsideJudgementInErrorLiPDefendant1LetterHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = List.of(
        SEND_SET_ASIDE_JUDGEMENT_IN_ERROR_LETTER_TO_LIP_DEFENDANT1);
    public static final String TASK_ID = "SendSetAsideLiPLetterDef1";
    private final SetAsideJudgmentInErrorLiPLetterGenerator lipLetterGenerator;
    private static final String FIRST_CONTACT_PACK_LETTER_TYPE = "first-contact-pack";

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_SUBMIT),
            this::sendSetAsideLetterToLiPDefendant1
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

    private CallbackResponse sendSetAsideLetterToLiPDefendant1(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        if (isRespondent1Lip(caseData)) {
            generateSetAsideJudgmentInErrorLiPDefendantLetter(callbackParams);
        }
        return AboutToStartOrSubmitCallbackResponse.builder().build();
    }

    private boolean isRespondent1Lip(CaseData caseData) {
        return YesOrNo.NO.equals(caseData.getRespondent1Represented());
    }

    private void generateSetAsideJudgmentInErrorLiPDefendantLetter(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        lipLetterGenerator.generateAndPrintSetAsideLetter(caseData, callbackParams.getParams().get(BEARER_TOKEN).toString());
    }
}
