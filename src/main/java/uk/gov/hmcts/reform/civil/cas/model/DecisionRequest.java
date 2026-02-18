package uk.gov.hmcts.reform.civil.cas.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@JsonIgnoreProperties(ignoreUnknown = true)
public class DecisionRequest {

    @JsonProperty("case_details")
    private CaseDetails caseDetails;

    public static DecisionRequest decisionRequest(CaseDetails caseDetails) {
        return new DecisionRequest().setCaseDetails(caseDetails);
    }
}
