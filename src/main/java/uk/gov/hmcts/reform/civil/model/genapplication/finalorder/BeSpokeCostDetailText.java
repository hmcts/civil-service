package uk.gov.hmcts.reform.civil.model.genapplication.finalorder;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.Setter;

@Setter
@Data
@Builder(toBuilder = true)
public class BeSpokeCostDetailText {

    private final String detailText;

    @JsonCreator
    BeSpokeCostDetailText(@JsonProperty("beSpokeCostDetailsText") String detailText) {

        this.detailText = detailText;
    }
}
