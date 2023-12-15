package uk.gov.hmcts.reform.civil.service;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.Element;

import java.util.List;

import static uk.gov.hmcts.reform.civil.utils.ElementUtils.element;

@Service
public class CaseWorkerDocumentService {

    public List<Element<CaseDocument>> getCaseWorkerDocumentsWithAddedDocument(CaseDocument caseDocument, CaseData caseData) {
        List<Element<CaseDocument>> caseWorkerDocuments = caseData.getCaseWorkerDocuments();
        caseWorkerDocuments.add(element(caseDocument));
        return caseWorkerDocuments;
    }
}
