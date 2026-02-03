package uk.gov.hmcts.reform.civil.model.genapplication;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypes;

import java.util.List;

@Data
@NoArgsConstructor
@Accessors(chain = true)
public class GAApplicationType {

    private List<GeneralApplicationTypes> types;

    @JsonCreator
    public GAApplicationType(@JsonProperty("types") List<GeneralApplicationTypes> types) {
        this.types = types;
    }
}
