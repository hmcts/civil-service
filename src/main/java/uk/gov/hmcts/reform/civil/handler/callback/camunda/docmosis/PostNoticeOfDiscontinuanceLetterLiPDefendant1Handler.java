package uk.gov.hmcts.reform.civil.handler.callback.camunda.docmosis;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.docmosis.settlediscontinue.NoticeOfDiscontinuanceLiPLetterGenerator;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.SEND_DISCONTINUANCE_LETTER_LIP_DEFENDANT1;

@Service
@RequiredArgsConstructor
public class PostNoticeOfDiscontinuanceLetterLiPDefendant1Handler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = List.of(
            SEND_DISCONTINUANCE_LETTER_LIP_DEFENDANT1);

    public static final String TASK_ID = "PostNoticeOfDiscontinuanceDefendant1LIP";
    private final NoticeOfDiscontinuanceLiPLetterGenerator lipLetterGenerator;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_SUBMIT),
            this::sendNoticeOfDiscontinuanceLetterToLiPDefendant1
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

    private CallbackResponse sendNoticeOfDiscontinuanceLetterToLiPDefendant1(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        if (caseData.isRespondent1LiP()) {
            generateNoticeOfDiscontinuanceLiPDefendantLetter(callbackParams);
        }
        return AboutToStartOrSubmitCallbackResponse.builder().build();
    }

    private void generateNoticeOfDiscontinuanceLiPDefendantLetter(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        lipLetterGenerator.printNoticeOfDiscontinuanceLetter(caseData, callbackParams.getParams().get(BEARER_TOKEN).toString());
    }
}
