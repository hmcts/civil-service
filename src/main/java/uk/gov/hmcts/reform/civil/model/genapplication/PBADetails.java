package uk.gov.hmcts.reform.civil.model.genapplication;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.Setter;

@Setter
@Data
@Builder(toBuilder = true)
public class PBADetails {

    private final String applicantsPbaAccountsList;
    private final String pbaReference;

    @JsonCreator
    PBADetails(@JsonProperty("applicantsPbaAccountsList") String applicantsPbaAccountsList,
               @JsonProperty("pbaReference") String pbaReference) {
        this.applicantsPbaAccountsList = applicantsPbaAccountsList;
        this.pbaReference = pbaReference;
    }
}
