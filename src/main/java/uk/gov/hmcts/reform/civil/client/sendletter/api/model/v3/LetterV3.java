package uk.gov.hmcts.reform.civil.client.sendletter.api.model.v3;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

/**
 * Letter V3.
 */
public class LetterV3 {

    @JsonProperty("documents")
    public final List<Document> documents;

    @JsonProperty("type")
    public final String type;

    @JsonProperty("additional_data")
    public final Map<String, Object> additionalData;

    /**
     * Constructor.
     * @param type The type
     * @param documents The documents
     * @param additionalData The additional data
     */
    public LetterV3(
        String type,
        List<Document> documents,
        Map<String, Object> additionalData
    ) {
        this.type = type;
        this.documents = documents;
        this.additionalData = additionalData;
    }
}
