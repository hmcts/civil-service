package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.citizenui.ManageDocument;
import uk.gov.hmcts.reform.civil.model.common.Element;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.MANAGE_DOCUMENTS;

@Slf4j
@Service
@RequiredArgsConstructor
public class ManageDocumentsHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = List.of(MANAGE_DOCUMENTS);
    private final ObjectMapper objectMapper;
    private final Map<String, Callback> callbackMap = Map.of(
        callbackKey(ABOUT_TO_SUBMIT),
        this::uploadManageDocuments,
        callbackKey(SUBMITTED), this::emptySubmittedCallbackResponse
    );

    @Override
    protected Map<String, Callback> callbacks() {
        return callbackMap;
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse uploadManageDocuments(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        List<Element<ManageDocument>> manageDocumentsList = caseData.getManageDocumentsList();

        if (manageDocumentsList != null) {
            log.info("submit data callback called {}", caseData.getCcdCaseReference());
            for (Element<ManageDocument> element : manageDocumentsList) {
                ManageDocument manageDocument = element.getValue();
                log.info("Document Name: {}", manageDocument.getDocumentName());
                log.info("Document Type: {}", manageDocument.getDocumentType());
                log.info("DocumentName: {}", manageDocument.getDocumentLink().getDocumentFileName());
                log.info("DocumentURL: {}", manageDocument.getDocumentLink().getDocumentUrl());
                log.info("DocumentBinary: {}", manageDocument.getDocumentLink().getDocumentBinaryUrl());
                log.info("DocumentUploadedTimeStamp: {}", manageDocument.getDocumentLink().getUploadTimestamp());
                log.info("DocumentCategoryID: {}", manageDocument.getDocumentLink().getCategoryID());
            }
        }
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(callbackParams.getCaseData().toMap(objectMapper))
            .build();
    }
}
