package uk.gov.hmcts.reform.civil.service;

import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.Element;

import java.util.List;

import static uk.gov.hmcts.reform.civil.utils.ElementUtils.element;

@Service
@Scope(value = "prototype", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class SystemGeneratedDocumentService {

    public List<Element<CaseDocument>> getSystemGeneratedDocumentsWithAddedDocument(Document document, DocumentType documentType, CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        List<Element<CaseDocument>> systemGeneratedDocuments = caseData.getSystemGeneratedCaseDocuments();
        CaseDocument caseDocument = CaseDocument.toCaseDocument(document, documentType);
        systemGeneratedDocuments.add(element(caseDocument));
        return systemGeneratedDocuments;
    }
}
