package uk.gov.hmcts.reform.civil.model.bundle;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Value
@Builder(toBuilder = true)
public class BundleData {

    @JsonProperty("caseBundles")
    public List<Bundle> caseBundles;

    @JsonCreator
    public BundleData(@JsonProperty("caseBundles") List<Bundle> caseBundles) {
        this.caseBundles = caseBundles;
    }
}
