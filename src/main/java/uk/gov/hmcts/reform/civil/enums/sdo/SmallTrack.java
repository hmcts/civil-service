package uk.gov.hmcts.reform.civil.enums.sdo;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum SmallTrack {
    @JsonProperty("smallClaimCreditHire")
    SMALL_CLAIM_CREDIT_HIRE,
    @JsonProperty("smallClaimRoadTrafficAccident")
    SMALL_CLAIM_ROAD_TRAFFIC_ACCIDENT,
    @JsonProperty("smallClaimDisputeResolutionHearing")
    SMALL_CLAIM_DISPUTE_RESOLUTION_HEARING,
    @JsonProperty("smallClaimFlightDelay")
    SMALL_CLAIM_FLIGHT_DELAY,
    @JsonProperty("smallClaimHousingDisrepair")
    SMALL_CLAIM_HOUSING_DISREPAIR,
    @JsonProperty("smallClaimPPI")
    SMALL_CLAIM_PPI
}
