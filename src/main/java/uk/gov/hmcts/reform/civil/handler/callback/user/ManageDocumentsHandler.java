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
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.citizenui.ManageDocument;
import uk.gov.hmcts.reform.civil.model.common.Element;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.MANAGE_DOCUMENTS;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.element;

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
        List<Element<ManageDocument>> manageDocumentsNewList = new ArrayList<>();
        if (!manageDocumentsList.isEmpty()) {
            log.info("submit data callback called {}", caseData.getCcdCaseReference());
            for (Element<ManageDocument> element : manageDocumentsList) {
                ManageDocument manageDocument = element.getValue();
                ManageDocument manageDocumentNew = new ManageDocument();
                manageDocumentNew.setDocumentLink(preserveCategoryId(callbackParams, manageDocument.getDocumentLink()));
                manageDocumentNew.setDocumentName(manageDocument.getDocumentName());
                manageDocumentNew.setDocumentType(manageDocument.getDocumentType());
                manageDocumentNew.setDocumentTypeOther(manageDocument.getDocumentTypeOther());
                manageDocumentNew.setCreatedDatetime(manageDocument.getCreatedDatetime());
                manageDocumentsNewList.add(element(manageDocumentNew));
            }
            caseData.setManageDocuments(manageDocumentsNewList);
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(callbackParams.getCaseData().toMap(objectMapper))
            .build();
    }

    private Document preserveCategoryId(CallbackParams callbackParams, Document documentLink) {
        CaseData caseDataBefore = callbackParams.getCaseDataBefore();
        if (caseDataBefore != null) {
            List<Element<ManageDocument>> manageDocumentsListBefore = caseDataBefore.getManageDocumentsList();
            if (!manageDocumentsListBefore.isEmpty()) {
                for (Element<ManageDocument> element : manageDocumentsListBefore) {
                    ManageDocument manageDocument = element.getValue();
                    if (manageDocument.getDocumentLink().getDocumentBinaryUrl().equals(documentLink.getDocumentBinaryUrl())) {
                        Document document = new Document();
                        document.setDocumentFileName(documentLink.getDocumentFileName());
                        document.setUploadTimestamp(documentLink.getUploadTimestamp());
                        document.setDocumentUrl(documentLink.getDocumentUrl());
                        document.setDocumentBinaryUrl(documentLink.getDocumentBinaryUrl());
                        document.setCategoryID(manageDocument.getDocumentLink().getCategoryID());
                        return document;
                    }
                }
            }
        }
        return  documentLink;
    }
}
