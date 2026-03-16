package uk.gov.hmcts.reform.civil.client.sendletter.api.model.v3;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Document.
 */
public class Document {
    @JsonProperty("content")
    public final String content;

    @JsonProperty("copies")
    public final int copies;

    /**
     * Constructor.
     * @param content The content
     * @param copies The copies
     */
    public Document(String content, int copies) {
        this.content = content;
        this.copies = copies;
    }
}
