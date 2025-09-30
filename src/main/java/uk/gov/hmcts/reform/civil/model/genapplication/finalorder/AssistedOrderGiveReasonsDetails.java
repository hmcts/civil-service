package uk.gov.hmcts.reform.civil.model.genapplication.finalorder;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.Setter;

@Setter
@Data
@Builder(toBuilder = true)
public class AssistedOrderGiveReasonsDetails {

    private final String reasonsText;

    @JsonCreator
    AssistedOrderGiveReasonsDetails(@JsonProperty("reasonsText") String reasonsText) {
        this.reasonsText = reasonsText;
    }
}
