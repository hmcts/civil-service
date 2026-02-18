package uk.gov.hmcts.reform.civil.sampledata;

import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;

public class DocumentBuilder {

    private String documentName;

    public static DocumentBuilder builder() {
        return new DocumentBuilder();
    }

    public DocumentBuilder documentName(String documentName) {
        this.documentName = documentName;
        return this;
    }

    public DocumentBuilder setDocumentName(String documentName) {
        return documentName(documentName);
    }

    public Document build() {
        return new Document()
            .setDocumentFileName(documentName)
            .setDocumentBinaryUrl(
                "http://dm-store:4506/documents/73526424-8434-4b1f-acca-bd33a3f8338f/binary")
            .setDocumentUrl("http://dm-store:4506/documents/73526424-8434-4b1f-acca-bd33a3f8338f");
    }
}
