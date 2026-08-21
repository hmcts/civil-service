package uk.gov.hmcts.reform.civil.enums.sdo;

import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.hmcts.ccd.sdk.api.CCD;

public enum FastTrack {
    @CCD(label = "Building Dispute")
    @JsonProperty("fastClaimBuildingDispute")
    FAST_CLAIM_BUILDING_DISPUTE,
    @CCD(label = "Clinical Negligence")
    @JsonProperty("fastClaimClinicalNegligence")
    FAST_CLAIM_CLINICAL_NEGLIGENCE,
    @CCD(label = "Credit Hire")
    @JsonProperty("fastClaimCreditHire")
    FAST_CLAIM_CREDIT_HIRE,
    @CCD(label = "Employers Liability")
    @JsonProperty("fastClaimEmployersLiability")
    FAST_CLAIM_EMPLOYERS_LIABILITY,
    @CCD(label = "Housing Disrepair")
    @JsonProperty("fastClaimHousingDisrepair")
    FAST_CLAIM_HOUSING_DISREPAIR,
    @CCD(label = "Personal Injury")
    @JsonProperty("fastClaimPersonalInjury")
    FAST_CLAIM_PERSONAL_INJURY,
    @CCD(label = "Road Traffic Accident")
    @JsonProperty("fastClaimRoadTrafficAccident")
    FAST_CLAIM_ROAD_TRAFFIC_ACCIDENT,
    @CCD(label = "Noise Induced Hearing Loss (Do not use with other options)")
    @JsonProperty("fastClaimNoiseInducedHearingLoss")
    FAST_CLAIM_NOISE_INDUCED_HEARING_LOSS,
    @CCD(label = "Payment Protection Insurance (PPI)")
    @JsonProperty("fastClaimPPI")
    FAST_CLAIM_PPI
}
