package uk.gov.hmcts.reform.civil.model.bundle;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;

@JsonIgnoreProperties(ignoreUnknown = true)
@Value
@Builder(toBuilder = true)
public class BundlingCaseDetails {

    @JsonProperty("case_data")
    public BundlingCaseData caseData;

    @JsonProperty("filename_identifier")
    public String filenamePrefix;

}
