package uk.gov.hmcts.reform.civil.model.bundle;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;

@JsonIgnoreProperties(ignoreUnknown = true)
@Value
@Builder(toBuilder = true)
public class BundlingRequestDocument {

    @JsonProperty("documentLink")
    private final DocumentLink documentLink;

    @JsonProperty("documentFileName")
    public String documentFileName;

    @JsonProperty("documentType")
    public String documentType;
}
