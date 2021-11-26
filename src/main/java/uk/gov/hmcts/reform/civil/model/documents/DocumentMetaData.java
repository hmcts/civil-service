package uk.gov.hmcts.reform.civil.model.documents;

import lombok.EqualsAndHashCode;
import lombok.ToString;

import static java.util.Objects.requireNonNull;

@EqualsAndHashCode
@ToString
public class DocumentMetaData {

    private Document document;
    private String description;
    private String dateUploaded;
    private String suppliedBy;

    private DocumentMetaData() {
        // noop -- for deserializer
    }

    public DocumentMetaData(
        Document document,
        String description,
        String dateUploaded) {
        this(document, description, dateUploaded, null);
    }

    public DocumentMetaData(
        Document document,
        String description,
        String dateUploaded,
        String suppliedBy) {
        this.document = document;
        this.description = description;
        this.dateUploaded = dateUploaded;

        this.suppliedBy = suppliedBy;
    }

    public Document getDocument() {
        requireNonNull(document);
        return document;
    }

    public String getDescription() {
        return description;
    }

    public String getDateUploaded() {
        requireNonNull(dateUploaded);
        return dateUploaded;
    }

    public String getSuppliedBy() {
        return suppliedBy;
    }
}
