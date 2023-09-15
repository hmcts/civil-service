package uk.gov.hmcts.reform.civil.handler.callback.user.strategy.translateddocuments;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.citizenui.TranslatedDocument;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.service.SystemGeneratedDocumentService;

import java.util.List;

@Component
@RequiredArgsConstructor
public class UploadTranslatedDocumentDefaultStrategy implements UploadTranslatedDocumentStrategy {

    private final SystemGeneratedDocumentService systemGeneratedDocumentService;
    private final ObjectMapper objectMapper;

    @Override
    public CallbackResponse uploadDocument(CallbackParams callbackParams) {
        List<Element<CaseDocument>> updatedDocumentList = updateSystemGeneratedDocumentsWithTranslationDocument(
            callbackParams);
        CaseData updatedCaseData = callbackParams.getCaseData().toBuilder().systemGeneratedCaseDocuments(
                updatedDocumentList)
            .businessProcess(BusinessProcess.ready(CaseEvent.UPLOAD_TRANSLATED_DOCUMENT)).build();
        // null/remove preview Translated document List, as un-necessary to store two copies of same documents
        updatedCaseData.getTranslatedDocument().clear();
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(updatedCaseData.toMap(objectMapper))
            .build();
    }

    private List<Element<CaseDocument>> updateSystemGeneratedDocumentsWithTranslationDocument(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        List<Element<TranslatedDocument>> translatedDocument = caseData.getTranslatedDocument();
        return systemGeneratedDocumentService.getSystemGeneratedDocumentsWithAddedDocument(translatedDocument, callbackParams);
    }
}
