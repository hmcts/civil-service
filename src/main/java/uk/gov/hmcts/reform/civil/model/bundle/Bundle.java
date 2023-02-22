package uk.gov.hmcts.reform.civil.model.bundle;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;

@JsonIgnoreProperties(ignoreUnknown = true)
@Value
@Builder(toBuilder = true)
public class Bundle {

    private BundleDetails value;

    @JsonCreator
    public Bundle(@JsonProperty("value") BundleDetails value) {
        this.value = value;
    }

}
