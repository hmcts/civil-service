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

@Slf4j
@Service
@RequiredArgsConstructor
public class ManageDocumentsHandler extends CallbackHandler {

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
                Document documentLink = manageDocument.getDocumentLink();
                if (documentLink != null) {
                    manageDocument.setDocumentLink(preserveCategoryId(callbackParams, documentLink));
                }
            }
            caseData.setManageDocuments(manageDocumentsList);
            taskListService.makeViewDocumentTaskAvailable(caseData.getCcdCaseReference().toString());
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
                    Document previousDocumentLink = manageDocument.getDocumentLink();
                    if (previousDocumentLink != null
                        && documentLink.getDocumentBinaryUrl().equals(previousDocumentLink.getDocumentBinaryUrl())) {
                        return new Document()
                            .setDocumentUrl(documentLink.getDocumentUrl())
                            .setDocumentBinaryUrl(documentLink.getDocumentBinaryUrl())
                            .setDocumentFileName(documentLink.getDocumentFileName())
                            .setDocumentHash(documentLink.getDocumentHash())
                            .setCategoryID(previousDocumentLink.getCategoryID())
                            .setUploadTimestamp(documentLink.getUploadTimestamp());
                    }
                }
            }
        }
        return documentLink;
    }
}
