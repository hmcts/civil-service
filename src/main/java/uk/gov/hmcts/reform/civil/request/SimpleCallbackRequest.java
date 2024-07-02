package uk.gov.hmcts.reform.civil.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@Jacksonized
public class SimpleCallbackRequest {

    @JsonProperty("case_details")
    private SimpleCaseDetails caseDetails;

}
