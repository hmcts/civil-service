package uk.gov.hmcts.reform.civil.model.genapplication;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.Setter;

@Setter
@Data
@Builder(toBuilder = true)
public class GAStatementOfTruth {

    private final String name;
    private final String role;

    @JsonCreator
    GAStatementOfTruth(@JsonProperty("name") String name,
                       @JsonProperty("role") String role) {
        this.name = name;
        this.role = role;
    }

}
