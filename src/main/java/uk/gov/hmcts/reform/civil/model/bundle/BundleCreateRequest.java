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
    String caseTypeId;
    @JsonProperty("jurisdictionId")
    String jurisdictionId;
    @JsonProperty("case_details")
    BundlingCaseDetails caseDetails;
    @JsonProperty("event_id")
    String eventId;

}
