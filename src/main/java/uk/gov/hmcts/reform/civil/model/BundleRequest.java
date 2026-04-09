package uk.gov.hmcts.reform.civil.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.CaseDetails;

@Data
@NoArgsConstructor
public class BundleRequest {

    @JsonProperty("case_details")
    private CaseDetails caseDetails;

    public BundleRequest(CaseDetails caseDetails) {
        this.caseDetails = caseDetails;
    }
}
