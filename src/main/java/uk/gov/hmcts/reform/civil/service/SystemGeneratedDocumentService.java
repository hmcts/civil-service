package uk.gov.hmcts.reform.civil.service;

import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.citizenui.TranslatedDocument;
import uk.gov.hmcts.reform.civil.model.common.Element;

import java.util.List;
import java.util.Objects;

import static uk.gov.hmcts.reform.civil.utils.ElementUtils.element;

@Service
@Scope(value = "prototype", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class SystemGeneratedDocumentService {

    public List<Element<CaseDocument>> getSystemGeneratedDocumentsWithAddedDocument(CaseDocument caseDocument, CaseData caseData) {
        List<Element<CaseDocument>> systemGeneratedDocuments = caseData.getSystemGeneratedCaseDocuments();
        systemGeneratedDocuments.add(element(caseDocument));
        return systemGeneratedDocuments;
    }

    public List<Element<CaseDocument>> getSystemGeneratedDocumentsWithAddedDocument(List<Element<TranslatedDocument>> translatedDocuments, CallbackParams callbackParams) {

        CaseData caseData = callbackParams.getCaseData();
        List<Element<CaseDocument>> systemGeneratedDocuments = caseData.getSystemGeneratedCaseDocuments();
        if (Objects.nonNull(translatedDocuments)) {
            for (Element<TranslatedDocument> translateDocument : translatedDocuments) {
                CaseDocument caseDocument = CaseDocument.toCaseDocument(translateDocument.getValue().getFile(), translateDocument.getValue().getCorrespondingDocumentType());
                systemGeneratedDocuments.add(element(caseDocument));
            }
        }
        return systemGeneratedDocuments;
    }

}
