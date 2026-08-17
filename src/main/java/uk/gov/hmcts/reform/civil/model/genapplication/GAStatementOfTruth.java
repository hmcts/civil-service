package uk.gov.hmcts.reform.civil.model.genapplication;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "GAStatementOfTruthGAspec", generate = true)
@Setter
@Data
@NoArgsConstructor
@Accessors(chain = true)
public class GAStatementOfTruth {

    @CCD(label = "Name", searchable = false, max = 70)
    private String name;
    @CCD(label = "Role", searchable = false, max = 40)
    private String role;

    @JsonCreator
    GAStatementOfTruth(@JsonProperty("name") String name,
                       @JsonProperty("role") String role) {
        this.name = name;
        this.role = role;
    }

}
