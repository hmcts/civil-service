package uk.gov.hmcts.reform.civil.enums.sdo;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum FastTrack {
    @JsonProperty("fastClaimBuildingDispute")
    FAST_CLAIM_BUILDING_DISPUTE,
    @JsonProperty("fastClaimClinicalNegligence")
    FAST_CLAIM_CLINICAL_NEGLIGENCE,
    @JsonProperty("fastClaimCreditHire")
    FAST_CLAIM_CREDIT_HIRE,
    @JsonProperty("fastClaimEmployersLiability")
    FAST_CLAIM_EMPLOYERS_LIABILITY,
    @JsonProperty("fastClaimHousingDisrepair")
    FAST_CLAIM_HOUSING_DISREPAIR,
    @JsonProperty("fastClaimPersonalInjury")
    FAST_CLAIM_PERSONAL_INJURY,
    @JsonProperty("fastClaimRoadTrafficAccident")
    FAST_CLAIM_ROAD_TRAFFIC_ACCIDENT,
    @JsonProperty("fastClaimNoiseInducedHearingLoss")
    FAST_CLAIM_NOISE_INDUCED_HEARING_LOSS,
    @JsonProperty("fastClaimPPI")
    FAST_CLAIM_PPI
}
