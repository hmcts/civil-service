package uk.gov.hmcts.reform.civil.model.caseprogression;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class SecondaryListingLocations  {

    @JsonProperty("CMCListingLocation")
    private SecondaryLocationModel cmcListingLocation;
    @JsonProperty("CCMCListingLocation")
    private SecondaryLocationModel ccmcListingLocation;
    @JsonProperty("PTRListingLocation")
    private SecondaryLocationModel ptrListingLocation;
    @JsonProperty("TrialListingLocation")
    private SecondaryLocationModel trialListingLocation;
}
