package uk.gov.hmcts.reform.civil.sampledata;

import uk.gov.hmcts.reform.civil.model.documents.Document;

public class DocumentBuilder {

    private String documentName;

    public static DocumentBuilder builder() {
        return new DocumentBuilder();
    }

    public DocumentBuilder documentName(String documentName) {
        this.documentName = documentName;
        return this;
    }

    public Document build() {
        return Document.builder()
            .documentFileName(documentName)
            .documentBinaryUrl(
                "http://dm-store:4506/documents/73526424-8434-4b1f-acca-bd33a3f8338f/binary")
            .documentUrl("http://dm-store:4506/documents/73526424-8434-4b1f-acca-bd33a3f8338f")
            .build();
    }
}
