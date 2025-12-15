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

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.DocumentUpdated;

@Slf4j
@Service
@RequiredArgsConstructor
public class CaseFileViewCallbackHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = List.of(DocumentUpdated);
    private final ObjectMapper mapper;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_START), this::startData,
            callbackKey(ABOUT_TO_SUBMIT), this::submitData,
            callbackKey(SUBMITTED), this::emptySubmittedCallbackResponse
        );
    }

    private CallbackResponse startData(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        List<Element<ManageDocument>> manageDocumentsList = caseData.getManageDocumentsList();

        if (manageDocumentsList != null) {
            log.info("Start data callback called {}", caseData.getCcdCaseReference());
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
            .data(caseData.toMap(mapper))
            .build();
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse submitData(CallbackParams callbackParams) {
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
            .data(caseData.toMap(mapper))
            .build();
    }
}
