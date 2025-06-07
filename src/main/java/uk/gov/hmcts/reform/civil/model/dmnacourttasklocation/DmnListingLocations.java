package uk.gov.hmcts.reform.civil.model.dmnacourttasklocation;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.CaseLocationCivil;

import java.util.Map;

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

    public static DmnListingLocations constructFromBaseLocation(CaseLocationCivil baseLocation) {
        return DmnListingLocations.builder()
            .cmcListingLocation(DmnListingLocationsModel.builder()
                                    .type("String")
                                    .value(baseLocation.getBaseLocation())
                                    .valueInfo(Map.of()).build())
            .ccmcListingLocation(DmnListingLocationsModel.builder()
                .type("String")
                .value(baseLocation.getBaseLocation())
                .valueInfo(Map.of()).build())
            .ptrListingLocation(DmnListingLocationsModel.builder()
                .type("String")
                .value(baseLocation.getBaseLocation())
                .valueInfo(Map.of()).build())
            .trialListingLocation(DmnListingLocationsModel.builder()
                .type("String")
                .value(baseLocation.getBaseLocation())
                .valueInfo(Map.of()).build())
            .build();
    }
}
