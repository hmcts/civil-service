package uk.gov.hmcts.reform.civil.model.citizenui;

import com.fasterxml.jackson.annotation.JsonValue;

public enum TranslatedDocumentType {
    DEFENDANT_RESPONSE,
    CLAIM_ISSUE;
    @JsonValue
    public static TranslatedDocumentType fromString(String translatedDocument) {
        for (TranslatedDocumentType doc : TranslatedDocumentType.values()) {
            if (doc.name().equalsIgnoreCase(translatedDocument)) {
                return doc;
            }
        }
        return null;
    }
}
