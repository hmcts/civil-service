package uk.gov.hmcts.reform.civil.model.genapplication;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.Setter;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;

@Setter
@Data
@Builder(toBuilder = true)
public class GAPbaDetails {

    private final DynamicList applicantsPbaAccounts;
    private final String pbaReference;

    @JsonCreator
    GAPbaDetails(@JsonProperty("applicantsPbaAccounts") DynamicList applicantsPbaAccounts,
                 @JsonProperty("pbaReference") String pbaReference) {
        this.applicantsPbaAccounts = applicantsPbaAccounts;
        this.pbaReference = pbaReference;
    }
}
