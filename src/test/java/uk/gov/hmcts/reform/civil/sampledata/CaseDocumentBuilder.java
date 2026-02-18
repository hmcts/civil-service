package uk.gov.hmcts.reform.civil.sampledata;

import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;

import java.time.LocalDateTime;

import static java.time.LocalDateTime.of;
import static java.util.Objects.nonNull;

public class CaseDocumentBuilder {

    private String documentName;
    private DocumentType documentType;

    private LocalDateTime createdDatetime;

    public static CaseDocumentBuilder builder() {
        return new CaseDocumentBuilder();
    }

    public CaseDocumentBuilder documentName(String documentName) {
        this.documentName = documentName;
        return this;
    }

    public CaseDocumentBuilder setDocumentName(String documentName) {
        return documentName(documentName);
    }

    public CaseDocumentBuilder documentType(DocumentType documentType) {
        this.documentType = documentType;
        return this;
    }

    public CaseDocumentBuilder setDocumentType(DocumentType documentType) {
        return documentType(documentType);
    }

    public CaseDocumentBuilder createdDatetime(LocalDateTime createdDatetime) {
        this.createdDatetime = createdDatetime;
        return this;
    }

    public CaseDocumentBuilder setCreatedDatetime(LocalDateTime createdDatetime) {
        return createdDatetime(createdDatetime);
    }

    public CaseDocument build() {
        return new CaseDocument()
            .setDocumentLink(DocumentBuilder.builder().documentName(documentName).build())
            .setDocumentSize(56975)
            .setCreatedDatetime(nonNull(createdDatetime) ? createdDatetime : of(2020, 7, 16, 14, 5, 15, 550439))
            .setDocumentType(documentType)
            .setCreatedBy("Civil")
            .setDocumentName(documentName);
    }
}
