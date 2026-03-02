package uk.gov.hmcts.reform.civil.ga.model.genapplication.finalorder;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Data
@Accessors(chain = true)
@NoArgsConstructor
public class DetailText {

    private String detailText;

    @JsonCreator
    DetailText(@JsonProperty("detailsRepresentationText") String detailText
    ) {

        this.detailText = detailText;
    }
}
