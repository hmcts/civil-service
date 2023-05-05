package uk.gov.hmcts.reform.civil.handler.callback.user;

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
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.TranslatedDocument;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.service.SystemGeneratedDocumentService;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.UPLOAD_TRANSLATED_DOCUMENT;
import static uk.gov.hmcts.reform.civil.enums.CaseState.AWAITING_APPLICANT_INTENTION;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.element;

@Service
@RequiredArgsConstructor
public class UploadTranslatedDocumentHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = List.of(UPLOAD_TRANSLATED_DOCUMENT);
    private final SystemGeneratedDocumentService systemGeneratedDocumentService;
    private final ObjectMapper objectMapper;

    private Map<String, Callback> callbackMap = Map.of(callbackKey(ABOUT_TO_SUBMIT), this::uploadDocument);

    @Override
    protected Map<String, Callback> callbacks() {
        return callbackMap;
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse uploadDocument(CallbackParams callbackParams) {
        List<Element<CaseDocument>> updatedDocumentList = updateSystemGeneratedDocumentsWithTranslationDocument(
            callbackParams);
        CaseData updatedCaseData = callbackParams.getCaseData().builder().systemGeneratedCaseDocuments(
            updatedDocumentList).build();
        return AboutToStartOrSubmitCallbackResponse.builder()
            .state(AWAITING_APPLICANT_INTENTION.name())
            .data(updatedCaseData.toMap(objectMapper))
            .build();
    }

    private List<Element<CaseDocument>> updateSystemGeneratedDocumentsWithTranslationDocument(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        Optional<TranslatedDocument> translatedDocument = caseData.getTranslatedDocument();
        return translatedDocument.map(document -> systemGeneratedDocumentService.getSystemGeneratedDocumentsWithAddedDocument(
            document.getFile(),
            document.getCorrespondingDocumentType(),
            callbackParams
        )).orElse(caseData.getSystemGeneratedCaseDocuments());
    }
}
