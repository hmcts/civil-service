package uk.gov.hmcts.reform.civil.enums.dj;

import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.hmcts.ccd.sdk.api.CCD;

public enum CaseManagementOrderAdditional {
    @CCD(label = "Building Dispute")
    @JsonProperty("OrderTypeTrialAdditionalDirectionsBuildingDispute")
    ORDER_TYPE_TRIAL_ADDITIONAL_DIRECTIONS_BUILDING_DISPUTE,

    @CCD(label = "Clinical Negligence")
    @JsonProperty("OrderTypeTrialAdditionalDirectionsClinicalNegligence")
    ORDER_TYPE_TRIAL_ADDITIONAL_DIRECTIONS_CLINICAL_NEGLIGENCE,

    @CCD(label = "Credit Hire")
    @JsonProperty("OrderTypeTrialAdditionalDirectionsCreditHire")
    ORDER_TYPE_TRIAL_ADDITIONAL_DIRECTIONS_CREDIT_HIRE,

    @CCD(label = "Employers Liability")
    @JsonProperty("OrderTypeTrialAdditionalDirectionsEmployersLiability")
    ORDER_TYPE_TRIAL_ADDITIONAL_DIRECTIONS_EMPLOYERS_LIABILITY,

    @CCD(label = "Housing Disrepair")
    @JsonProperty("OrderTypeTrialAdditionalDirectionsHousingDisrepair")
    ORDER_TYPE_TRIAL_ADDITIONAL_DIRECTIONS_HOUSING_DISREPAIR,

    @CCD(label = "Personal Injury")
    @JsonProperty("OrderTypeTrialAdditionalDirectionsPersonalInjury")
    ORDER_TYPE_TRIAL_ADDITIONAL_DIRECTIONS_PERSONAL_INJURY,

    @CCD(label = "Road Traffic Accident")
    @JsonProperty("OrderTypeTrialAdditionalDirectionsRoadTrafficAccident")
    ORDER_TYPE_TRIAL_ADDITIONAL_DIRECTIONS_ROAD_TRAFFIC_ACCIDENT,

    @CCD(label = "Payment Protection Insurance (PPI)")
    @JsonProperty("OrderTypeTrialAdditionalDirectionsPPI")
    ORDER_TYPE_TRIAL_ADDITIONAL_DIRECTIONS_PPI
}
