package uk.gov.hmcts.reform.civil.model.genapplication;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.Setter;
import uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypes;

import java.util.List;

@Setter
@Data
@Builder(toBuilder = true)
public class GATypeGAspec {

    private final List<GeneralApplicationTypes> types;

    @JsonCreator
    GATypeGAspec(@JsonProperty("types") List<GeneralApplicationTypes> types) {
        this.types = types;
    }
}
