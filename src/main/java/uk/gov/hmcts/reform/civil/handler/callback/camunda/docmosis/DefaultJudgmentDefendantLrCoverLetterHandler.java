package uk.gov.hmcts.reform.civil.handler.callback.camunda.docmosis;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackException;
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
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.POST_DJ_NON_DIVERGENT_LETTER_DEFENDANT1;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.POST_DJ_NON_DIVERGENT_LETTER_DEFENDANT2;
import static uk.gov.hmcts.reform.civil.utils.JudgmentOnlineUtils.areRespondentLegalOrgsEqual;
import static uk.gov.hmcts.reform.civil.utils.JudgmentOnlineUtils.respondent2Present;

@Service
@RequiredArgsConstructor
public class DefaultJudgmentDefendantLrCoverLetterHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = List.of(
        POST_DJ_NON_DIVERGENT_LETTER_DEFENDANT1, POST_DJ_NON_DIVERGENT_LETTER_DEFENDANT2);
    public static final String TASK_ID_DEFENDANT_1 = "PostDjLetterDefendant1";
    public static final String TASK_ID_DEFENDANT_2 = "PostDjLetterDefendant2";
    private final DefaultJudgmentCoverLetterGenerator defaultJudgmentCoverLetterGenerator;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_SUBMIT),
            this::sendCoverLetterToDefendantLrLegalOrganisations
        );
    }

    @Override
    public String camundaActivityId(CallbackParams callbackParams) {
        CaseEvent caseEvent = CaseEvent.valueOf(callbackParams.getRequest().getEventId());
        if (POST_DJ_NON_DIVERGENT_LETTER_DEFENDANT1.equals(caseEvent)) {
            return TASK_ID_DEFENDANT_1;
        } else if (POST_DJ_NON_DIVERGENT_LETTER_DEFENDANT2.equals(caseEvent)) {
            return TASK_ID_DEFENDANT_2;
        } else {
            throw new CallbackException(String.format("Callback handler received illegal event: %s", caseEvent));
        }
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse sendCoverLetterToDefendantLrLegalOrganisations(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        if (YesOrNo.YES.equals(caseData.getRespondent1Represented())) {
            generateCoverLetterDefendantLrLegalOrganisations(callbackParams);
        }
        return AboutToStartOrSubmitCallbackResponse.builder().build();
    }

    private void generateCoverLetterDefendantLrLegalOrganisations(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        String taskId = camundaActivityId(callbackParams);
        if (taskId.equals(TASK_ID_DEFENDANT_1)) {
            defaultJudgmentCoverLetterGenerator.generateAndPrintDjCoverLettersPlusDocument(
                caseData, callbackParams.getParams().get(BEARER_TOKEN).toString(), false);
        } else {
            if (respondent2Present(caseData) && !areRespondentLegalOrgsEqual(caseData)) {
                defaultJudgmentCoverLetterGenerator.generateAndPrintDjCoverLettersPlusDocument(
                    caseData, callbackParams.getParams().get(BEARER_TOKEN).toString(), true);
            }
        }
    }
}
