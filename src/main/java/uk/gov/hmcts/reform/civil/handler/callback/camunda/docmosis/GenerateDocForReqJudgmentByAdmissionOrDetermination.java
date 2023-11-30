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
import uk.gov.hmcts.reform.civil.service.docmosis.claimantResponse.RequestJudgmentByAdmissionOrDeterminationResponseDocGenerator;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.GENERATE_JUDGMENT_BY_ADMISSION_RESPONSE_DOC;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.GENERATE_JUDGMENT_BY_DETERMINATION_RESPONSE_DOC;

@Service
@RequiredArgsConstructor
public class GenerateDocForReqJudgmentByAdmissionOrDetermination extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = List.of(GENERATE_JUDGMENT_BY_ADMISSION_RESPONSE_DOC, GENERATE_JUDGMENT_BY_DETERMINATION_RESPONSE_DOC);
    private final Map<String, Callback> callbackMap = Map.of(callbackKey(ABOUT_TO_SUBMIT), this::generateResponseDocument);

    private final ObjectMapper objectMapper;
    private final RequestJudgmentByAdmissionOrDeterminationResponseDocGenerator requestJudgmentByAdmissionOrDeterminationResponseDocGenerator;
    private final SystemGeneratedDocumentService systemGeneratedDocumentService;


    private CallbackResponse generateResponseDocument(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();

        CaseDocument claimantResponseDoc = requestJudgmentByAdmissionOrDeterminationResponseDocGenerator.generate(caseData, callbackParams.getParams().get(BEARER_TOKEN).toString());
        CaseData updatedCaseData = caseData.toBuilder()
            .systemGeneratedCaseDocuments(systemGeneratedDocumentService.getSystemGeneratedDocumentsWithAddedDocument(
                claimantResponseDoc,
                caseData
            ))
            .build();
        if (true) throw new RuntimeException("Dummy exception");
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(updatedCaseData.toMap(objectMapper))
            .build();
    }

    @Override
    protected Map<String, Callback> callbacks() {
        return callbackMap;
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }
}
