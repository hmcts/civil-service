package uk.gov.hmcts.reform.civil.model.genapplication;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Data
@NoArgsConstructor
@Builder(toBuilder = true)
public class GAStatementOfTruth {

    private String name;
    private String role;

    @JsonCreator
    GAStatementOfTruth(@JsonProperty("name") String name,
                       @JsonProperty("role") String role) {
        this.name = name;
        this.role = role;
    }

}
