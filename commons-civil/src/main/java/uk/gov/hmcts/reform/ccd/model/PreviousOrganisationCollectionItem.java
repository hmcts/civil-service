package uk.gov.hmcts.reform.ccd.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class PreviousOrganisationCollectionItem {

    @JsonProperty("id")
    private String id;

    @JsonProperty("value")
    private PreviousOrganisation value;
}
