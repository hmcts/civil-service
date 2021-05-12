package uk.gov.hmcts.reform.civil.sampledata;

import uk.gov.hmcts.reform.civil.model.documents.CaseDocument;
import uk.gov.hmcts.reform.civil.model.documents.DocumentType;

import static java.time.LocalDateTime.of;

public class CaseDocumentBuilder {

    private String documentName;
    private DocumentType documentType;

    public static CaseDocumentBuilder builder() {
        return new CaseDocumentBuilder();
    }

    public CaseDocumentBuilder documentName(String documentName) {
        this.documentName = documentName;
        return this;
    }

    public CaseDocumentBuilder documentType(DocumentType documentType) {
        this.documentType = documentType;
        return this;
    }

    public CaseDocument build() {
        return CaseDocument.builder()
            .documentLink(DocumentBuilder.builder().documentName(documentName).build())
            .documentSize(56975)
            .createdDatetime(of(2020, 7, 16, 14, 5, 15, 550439))
            .documentType(documentType)
            .createdBy("Civil")
            .documentName(documentName)
            .build();
    }
}
