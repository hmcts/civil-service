package uk.gov.hmcts.reform.civil.model;

import uk.gov.hmcts.reform.civil.model.common.MappableObject;
import uk.gov.hmcts.reform.civil.model.documents.Document;

public class BundleDocument implements MappableObject {

    private String name;
    private String description;
    private int sortIndex;
    private Document sourceDocument;

    private BundleDocument() {
        // noop -- for deserializer
    }

    public BundleDocument(
        String name,
        String description,
        int sortIndex,
        Document sourceDocument
    ) {
        this.name = name;
        this.description = description;
        this.sortIndex = sortIndex;
        this.sourceDocument = sourceDocument;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public int getSortIndex() {
        return sortIndex;
    }

    public Document getSourceDocument() {
        return sourceDocument;
    }
}
