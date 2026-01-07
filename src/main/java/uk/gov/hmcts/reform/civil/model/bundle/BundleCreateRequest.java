package uk.gov.hmcts.reform.civil.model.bundle;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Value;
import lombok.experimental.Accessors;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@NoArgsConstructor
@Accessors(chain = true)
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
