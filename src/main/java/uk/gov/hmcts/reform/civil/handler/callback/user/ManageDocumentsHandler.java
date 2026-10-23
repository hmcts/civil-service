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
import uk.gov.hmcts.reform.dashboard.services.TaskListService;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.MANAGE_DOCUMENTS;
import static uk.gov.hmcts.reform.civil.model.citizenui.ManageDocumentType.WITHOUT_PREJUDICE_PART_36_OFFER_OR_REJECTIONS;

@Slf4j
@Service
@RequiredArgsConstructor
public class ManageDocumentsHandler extends CallbackHandler {

    private static final String WITHOUT_PREJUDICE_CATEGORY_ID = "WithoutPrejudice";

    private static final List<CaseEvent> EVENTS = List.of(MANAGE_DOCUMENTS);
    private final ObjectMapper objectMapper;
    private final TaskListService taskListService;

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
        if (!manageDocumentsList.isEmpty()) {
            log.info("submit data callback called {}", caseData.getCcdCaseReference());
            for (Element<ManageDocument> element : manageDocumentsList) {
                ManageDocument manageDocument = element.getValue();
                manageDocument.setDocumentLink(resolveCategoryId(callbackParams, manageDocument));
            }
            caseData.setManageDocuments(manageDocumentsList);
            taskListService.makeViewDocumentTaskAvailable(caseData.getCcdCaseReference().toString());
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(callbackParams.getCaseData().toMap(objectMapper))
            .build();
    }

    private Document resolveCategoryId(CallbackParams callbackParams, ManageDocument manageDocument) {
        Document documentLink = manageDocument.getDocumentLink();
        if (documentLink == null) {
            return null;
        }

        Document documentWithCategory = documentLink;
        Document previousDocumentLink = findPreviousDocumentLink(callbackParams, documentLink.getDocumentBinaryUrl());
        if (previousDocumentLink != null) {
            documentWithCategory = copyDocumentWithCategory(documentLink, previousDocumentLink.getCategoryID());
        }

        if (manageDocument.getDocumentType() != WITHOUT_PREJUDICE_PART_36_OFFER_OR_REJECTIONS
            && WITHOUT_PREJUDICE_CATEGORY_ID.equals(documentWithCategory.getCategoryID())) {
            documentWithCategory = copyDocumentWithCategory(documentWithCategory, null);
        }

        if (manageDocument.getDocumentType() == WITHOUT_PREJUDICE_PART_36_OFFER_OR_REJECTIONS
            && documentWithCategory.getCategoryID() == null) {
            return copyDocumentWithCategory(documentWithCategory, WITHOUT_PREJUDICE_CATEGORY_ID);
        }

        return documentWithCategory;
    }

    private Document findPreviousDocumentLink(CallbackParams callbackParams, String documentBinaryUrl) {
        CaseData caseDataBefore = callbackParams.getCaseDataBefore();
        if (caseDataBefore == null) {
            return null;
        }

        List<Element<ManageDocument>> manageDocumentsListBefore = caseDataBefore.getManageDocumentsList();
        for (Element<ManageDocument> element : manageDocumentsListBefore) {
            Document previousDocumentLink = element.getValue().getDocumentLink();
            if (previousDocumentLink != null
                && documentBinaryUrl.equals(previousDocumentLink.getDocumentBinaryUrl())) {
                return previousDocumentLink;
            }
        }

        return null;
    }

    private Document copyDocumentWithCategory(Document source, String categoryId) {
        return new Document()
            .setDocumentUrl(source.getDocumentUrl())
            .setDocumentBinaryUrl(source.getDocumentBinaryUrl())
            .setDocumentFileName(source.getDocumentFileName())
            .setDocumentHash(source.getDocumentHash())
            .setCategoryID(categoryId)
            .setUploadTimestamp(source.getUploadTimestamp());
    }
}
