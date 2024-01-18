package uk.gov.hmcts.reform.civil.model.citizenui;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum TranslatedDocumentType {
    @JsonProperty("DEFENDANT_RESPONSE")
    DEFENDANT_RESPONSE,
    @JsonProperty("CLAIM_ISSUE")
    CLAIM_ISSUE
}
