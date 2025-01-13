package uk.gov.hmcts.reform.civil.model.dmnacourttasklocation;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class DmnListingLocations  {

    @JsonProperty("CMC")
    private DmnListingLocationsModel cmcListingLocation;
    @JsonProperty("CCMC")
    private DmnListingLocationsModel ccmcListingLocation;
    @JsonProperty("PTR")
    private DmnListingLocationsModel ptrListingLocation;
    @JsonProperty("Trial")
    private DmnListingLocationsModel trialListingLocation;
}
