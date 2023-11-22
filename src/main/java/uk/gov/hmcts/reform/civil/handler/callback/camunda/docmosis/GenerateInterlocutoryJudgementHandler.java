package uk.gov.hmcts.reform.civil.handler.callback.camunda.docmosis;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.SystemGeneratedDocumentService;
import uk.gov.hmcts.reform.civil.service.docmosis.claimantresponse.InterlocutoryJudgementDocGenerator;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;

@Service
@RequiredArgsConstructor
public class GenerateInterlocutoryJudgementHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = List.of(CaseEvent.GENERATE_INTERLOCUTORY_JUDGEMENT_DOCUMENT);
    private final ObjectMapper objectMapper;
    private final InterlocutoryJudgementDocGenerator interlocutoryJudgementDocGenerator;
    private final SystemGeneratedDocumentService systemGeneratedDocumentService;
    private final Map<String, Callback> callbackMap = Map.of(
        callbackKey(ABOUT_TO_SUBMIT),
        this::generateInterlocutoryJudgementDoc
    );

    @Override
    protected Map<String, Callback> callbacks() {
        return callbackMap;
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse generateInterlocutoryJudgementDoc(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        CaseDocument interlocutoryJudgementDoc = interlocutoryJudgementDocGenerator.generateInterlocutoryJudgementDoc(
            caseData,
            callbackParams.getParams().get(BEARER_TOKEN).toString()
        );

        CaseData updatedCaseData = caseData.toBuilder()
            .systemGeneratedCaseDocuments(systemGeneratedDocumentService.getSystemGeneratedDocumentsWithAddedDocument(
                interlocutoryJudgementDoc,
                caseData
            ))
            .build();

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(updatedCaseData.toMap(objectMapper))
            .build();
    }

}

