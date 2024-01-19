package uk.gov.hmcts.reform.civil.model.citizenui;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public enum TranslatedDocumentType {
    DEFENDANT_RESPONSE,
    CLAIM_ISSUE;

    @JsonValue
    public static TranslatedDocumentType fromString(String translatedDocument) {
        log.info("--fromString------------1----",translatedDocument);
        for (TranslatedDocumentType doc : TranslatedDocumentType.values()) {
            if (doc.name().equalsIgnoreCase(translatedDocument)) {
                log.info("--fromString-------------2---",doc);
                return doc;
            }
        }
        return null;
    }
}
