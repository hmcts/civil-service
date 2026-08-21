package uk.gov.hmcts.reform.civil.enums.sdo;

import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.hmcts.ccd.sdk.api.CCD;

public enum SmallTrack {
    @CCD(label = "Credit Hire")
    @JsonProperty("smallClaimCreditHire")
    SMALL_CLAIM_CREDIT_HIRE,
    @CCD(label = "Road Traffic Accident")
    @JsonProperty("smallClaimRoadTrafficAccident")
    SMALL_CLAIM_ROAD_TRAFFIC_ACCIDENT,
    @CCD(label = "Dispute resolution hearing (Do not use with other options)")
    @JsonProperty("smallClaimDisputeResolutionHearing")
    SMALL_CLAIM_DISPUTE_RESOLUTION_HEARING,
    @CCD(label = "Flight Delay")
    @JsonProperty("smallClaimFlightDelay")
    SMALL_CLAIM_FLIGHT_DELAY,
    @CCD(label = "Housing Disrepair")
    @JsonProperty("smallClaimHousingDisrepair")
    SMALL_CLAIM_HOUSING_DISREPAIR,
    @CCD(label = "Payment Protection Insurance (PPI)")
    @JsonProperty("smallClaimPPI")
    SMALL_CLAIM_PPI
}
