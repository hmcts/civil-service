package uk.gov.hmcts.reform.civil.client.sendletter.api;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

/**
 * Letter with PDFs request.
 */
public class LetterWithPdfsRequest {
    private final String type;

    private final List<String> documents;

    @JsonProperty("additional_data")
    private final Map<String, Object> additionalData;

    /**
     * Constructor.
     * @param documents The documents
     * @param type The type
     * @param additionalData The additional data
     */
    public LetterWithPdfsRequest(List<String> documents, String type, Map<String, Object> additionalData) {
        this.documents = documents;
        this.type = type;
        this.additionalData = additionalData;
    }

    /**
     * Get the type.
     * @return The type
     */
    public String getType() {
        return this.type;
    }

    /**
     * Get the additional data.
     * @return The additional data
     */
    public Map<String, Object> getAdditionalData() {
        return this.additionalData;
    }

    /**
     * Get the documents.
     * @return The documents
     */
    public List<String> getDocuments() {
        return documents;
    }
}
