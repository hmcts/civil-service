package uk.gov.hmcts.reform.civil.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;

@JsonIgnoreProperties(ignoreUnknown = true)
@Value
@Builder(toBuilder = true)
public class BundlingCaseData {

    @JsonProperty("bundleConfiguration")
    public String bundleConfiguration;
    @JsonProperty("id")
    public String id;
    @JsonProperty("data")
    public BundlingData data;
}
