package uk.gov.hmcts.reform.civil.model.genapplication.finalorder;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.Setter;

@Setter
@Data
@Builder(toBuilder = true)
public class AssistedOrderRecitalRecord {

    private final String text;

    @JsonCreator
    AssistedOrderRecitalRecord(@JsonProperty("text") String text) {
        this.text = text;
    }
}
