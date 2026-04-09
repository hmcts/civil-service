package uk.gov.hmcts.reform.civil.model.bundle;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@NoArgsConstructor
@Accessors(chain = true)
public class BundlingRequestDocument {

    @JsonProperty("documentLink")
    DocumentLink documentLink;

    @JsonProperty("documentFileName")
    public String documentFileName;

    @JsonProperty("documentType")
    public String documentType;
}
