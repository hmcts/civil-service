package uk.gov.hmcts.reform.ccd.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(generate = false)
@Getter
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class PreviousOrganisationCollectionItem {

    @JsonProperty("id")
    private String id;

    @JsonProperty("value")
    private PreviousOrganisation value;
}
