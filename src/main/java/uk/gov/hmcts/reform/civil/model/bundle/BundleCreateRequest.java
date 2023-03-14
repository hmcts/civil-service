package uk.gov.hmcts.reform.civil.model.bundle;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;

@JsonIgnoreProperties(ignoreUnknown = true)
@Value
@Builder(toBuilder = true)
public class BundleCreateRequest {

    @JsonProperty("caseTypeId")
    private String caseTypeId;
    @JsonProperty("jurisdictionId")
    private String jurisdictionId;
    @JsonProperty("case_details")
    private BundlingCaseDetails caseDetails;
    @JsonProperty("event_id")
    private String eventId;

}
