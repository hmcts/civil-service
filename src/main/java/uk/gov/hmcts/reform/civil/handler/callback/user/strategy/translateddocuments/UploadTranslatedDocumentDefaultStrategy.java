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
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.TranslatedDocument;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.service.SystemGeneratedDocumentService;

import java.util.List;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class UploadTranslatedDocumentDefaultStrategy implements UploadTranslatedDocumentStrategy {

    private final SystemGeneratedDocumentService systemGeneratedDocumentService;
    private final ObjectMapper objectMapper;

    @Override
    public CallbackResponse uploadDocument(CallbackParams callbackParams) {
        List<Element<CaseDocument>> updatedDocumentList = updateSystemGeneratedDocumentsWithTranslationDocuments(
            callbackParams);
        CaseDataLiP caseDataLip = callbackParams.getCaseData().getCaseDataLiP();
        if (Objects.nonNull(caseDataLip) && Objects.nonNull(caseDataLip.getTranslatedDocuments())) {
            caseDataLip.getTranslatedDocuments().clear();
        }
        CaseData updatedCaseData = callbackParams.getCaseData().toBuilder().systemGeneratedCaseDocuments(
                updatedDocumentList)
            .caseDataLiP(caseDataLip)
            .businessProcess(BusinessProcess.ready(CaseEvent.UPLOAD_TRANSLATED_DOCUMENT)).build();

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(updatedCaseData.toMap(objectMapper))
            .build();
    }

    private List<Element<CaseDocument>> updateSystemGeneratedDocumentsWithTranslationDocuments(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        List<Element<TranslatedDocument>> translatedDocuments = caseData.getTranslatedDocuments();
        return systemGeneratedDocumentService.getSystemGeneratedDocumentsWithAddedDocument(translatedDocuments, callbackParams);
    }
}
