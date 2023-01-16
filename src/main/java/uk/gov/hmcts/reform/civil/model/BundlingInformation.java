package uk.gov.hmcts.reform.civil.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

@Data
@Getter
@Builder
@Jacksonized
public class BundlingInformation {

    @JsonProperty("caseBundles")
    private List<Bundle> caseBundles;
    @JsonProperty("historicalBundles")
    private List<Bundle> historicalBundles;
    @JsonProperty("bundleConfiguration")
    private String bundleConfiguration;
    @JsonProperty("multiBundleConfiguration")
    private List<MultiBundleConfig> multiBundleConfiguration;
}
