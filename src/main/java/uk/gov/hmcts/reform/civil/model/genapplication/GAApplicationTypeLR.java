package uk.gov.hmcts.reform.civil.model.genapplication;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypesLR;

import java.util.List;

@Data
@NoArgsConstructor
@Accessors(chain = true)
public class GAApplicationTypeLR {

    private List<GeneralApplicationTypesLR> types;

    @JsonCreator
    GAApplicationTypeLR(@JsonProperty("types") List<GeneralApplicationTypesLR> types) {
        this.types = types;
    }
}
