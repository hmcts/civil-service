package uk.gov.hmcts.reform.civil.handler.callback.user.strategy.translateddocuments;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.citizenui.TranslatedDocument;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.service.SystemGeneratedDocumentService;

import java.util.List;
import java.util.Optional;

import static uk.gov.hmcts.reform.civil.enums.CaseState.AWAITING_APPLICANT_INTENTION;

@Component
@RequiredArgsConstructor
public class UploadTranslatedDocumentDefaultStrategy implements UploadTranslatedDocumentStrategy{
    private final SystemGeneratedDocumentService systemGeneratedDocumentService;
    private final ObjectMapper objectMapper;
    @Override
    public CallbackResponse uploadDocument(CallbackParams callbackParams) {
        List<Element<CaseDocument>> updatedDocumentList = updateSystemGeneratedDocumentsWithTranslationDocument(
            callbackParams);
        CaseData updatedCaseData = callbackParams.getCaseData().toBuilder().systemGeneratedCaseDocuments(
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
