package uk.gov.hmcts.reform.civil.service.documentmanagement;

import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.documents.CaseDocument;
import uk.gov.hmcts.reform.civil.model.documents.DocumentMetaData;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class DocumentUtil {

    public List<DocumentMetaData> fetchDocumentsFromCaseData(CaseData caseData, CaseDocument caseDocument) {
        List<DocumentMetaData> documentMetaDataList = new ArrayList<>();
        DocumentMetaData documentMetaData = new DocumentMetaData(
            caseData.getSpecClaimTemplateDocumentFiles(),
            "doc1",
            "doc1"
        );

        documentMetaDataList.add(new DocumentMetaData(caseDocument.getDocumentLink(),
                                                      "Sealed Claim form",
                                                      LocalDate.now().toString()));
        if (caseData.getSpecClaimTemplateDocumentFiles() != null) {
            documentMetaDataList.add(new DocumentMetaData(
                caseData.getSpecClaimTemplateDocumentFiles(),
                "Claim timeline",
                LocalDate.now().toString()
            ));
        }
        if (caseData.getSpecClaimDetailsDocumentFiles() != null) {
            documentMetaDataList.add(new DocumentMetaData(
                caseData.getSpecClaimDetailsDocumentFiles(),
                "Supported docs",
                LocalDate.now().toString()
            ));
        }

        return documentMetaDataList;
    }
}
