package uk.gov.hmcts.reform.civil.model.dmnacourttasklocation;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class DmnListingLocations {

    @JsonProperty("CMC")
    private DmnListingLocationsModel cmcListingLocation;
    @JsonProperty("CCMC")
    private DmnListingLocationsModel ccmcListingLocation;
    @JsonProperty("PTR")
    private DmnListingLocationsModel ptrListingLocation;
    @JsonProperty("Trial")
    private DmnListingLocationsModel trialListingLocation;
}
