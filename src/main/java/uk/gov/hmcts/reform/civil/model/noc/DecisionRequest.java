package uk.gov.hmcts.reform.civil.model.noc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@JsonIgnoreProperties(ignoreUnknown = true)
public class DecisionRequest {

    @JsonProperty("case_details")
    private CaseDetails caseDetails;

    public static DecisionRequest decisionRequest(CaseDetails caseDetails) {
        return DecisionRequest.builder().caseDetails(caseDetails).build();
    }
}
