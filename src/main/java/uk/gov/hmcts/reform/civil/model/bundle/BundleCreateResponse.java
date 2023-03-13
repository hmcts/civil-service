package uk.gov.hmcts.reform.civil.model.bundle;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Value
@Builder(toBuilder = true)
public class BundleCreateResponse {

    @JsonProperty("data")
    public BundleData data;
    @JsonProperty("errors")
    public List<Object> errors = null;
    @JsonProperty("warnings")
    public List<Object> warnings = null;
    @JsonProperty("documentTaskId")
    public Integer documentTaskId;

}
