package uk.gov.hmcts.reform.civil.model.search;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class SupportingDocuments {

    @JsonProperty("document_url")
    @JsonAlias("document_url")
    private String documentUrl;

    @JsonProperty("document_binary_url")
    @JsonAlias("document_binary_url")
    private String documentBinaryUrl;

    @JsonProperty("document_filename")
    @JsonAlias("document_filename")
    private String documentFilename;

}
