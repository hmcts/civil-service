package uk.gov.hmcts.reform.civil.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.CaseDetails;

@Data
@Builder(toBuilder = true)
public class BundleRequest {

    @JsonProperty("case_details")
    private final CaseDetails caseDetails;

    public BundleRequest(CaseDetails caseDetails) {
        this.caseDetails = caseDetails;
    }
}
