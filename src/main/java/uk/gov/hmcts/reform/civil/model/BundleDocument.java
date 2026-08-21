package uk.gov.hmcts.reform.civil.model;

import uk.gov.hmcts.reform.civil.model.common.MappableObject;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

public class BundleDocument implements MappableObject {

    @CCD(label = "Document Name", showCondition = "name=\"DUMMY_VALUE_TO_HIDE_FIELD\"", searchable = false)
    private String name;
    @CCD(
            label = "Short Description",
            showCondition = "name=\"DUMMY_VALUE_TO_HIDE_FIELD\"",
            searchable = false,
            typeOverride = FieldType.TextArea
    )
    private String description;
    @CCD(label = "Sort Index", showCondition = "name=\"DUMMY_VALUE_TO_HIDE_FIELD\"", searchable = false)
    private int sortIndex;
    @CCD(label = "Source Document", showCondition = "name=\"DUMMY_VALUE_TO_HIDE_FIELD\"", searchable = false)
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
