package uk.gov.hmcts.reform.unspec.model.documents;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class Document {

    @JsonProperty("document_url")
    String documentUrl;

    @JsonProperty("document_binary_url")
    String documentBinaryUrl;

    @JsonProperty("document_filename")
    String documentFileName;

    @JsonCreator
    public Document(String documentUrl, String documentBinaryUrl, String documentFileName) {
        this.documentUrl = documentUrl;
        this.documentBinaryUrl = documentBinaryUrl;
        this.documentFileName = documentFileName;
    }
}
