package uk.gov.hmcts.reform.civil.model.caseprogression;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class SecondaryListingLocations  {
    @JsonProperty("PrelistingAdminLocation")
    private SecondaryLocationModel prelistingAdminLocation;
    @JsonProperty("CMCListingLocation")
    private SecondaryLocationModel cmcListingLocation;
    @JsonProperty("TrialListingLocation")
    private SecondaryLocationModel trialListingLocation;
    @JsonProperty("PTRListingLocation")
    private SecondaryLocationModel ptrListingLocation;
    @JsonProperty("CCMCListingLocation")
    private SecondaryLocationModel ccmcListingLocation;
    @JsonProperty("PostlistingAdminLocation")
    private SecondaryLocationModel postlistingAdminLocation;
}
