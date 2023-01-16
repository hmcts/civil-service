package uk.gov.hmcts.reform.civil.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;
import uk.gov.hmcts.reform.civil.enums.BundlingDocGroupEnum;
import uk.gov.hmcts.reform.civil.model.documents.Document;

@JsonIgnoreProperties(ignoreUnknown = true)
@Value
@Builder(toBuilder = true)
public class BundlingRequestDocument {

    @JsonProperty("documentLink")
    private final Document documentLink;

    @JsonProperty("documentFileName")
    public String documentFileName;

    @JsonProperty("documentGroup")
    public BundlingDocGroupEnum documentGroup;
}
