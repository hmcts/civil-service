package uk.gov.hmcts.reform.civil.enums.dj;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum CaseManagementOrderAdditional {
    @JsonProperty("OrderTypeTrialAdditionalDirectionsBuildingDispute")
    ORDER_TYPE_TRIAL_ADDITIONAL_DIRECTIONS_BUILDING_DISPUTE,

    @JsonProperty("OrderTypeTrialAdditionalDirectionsClinicalNegligence")
    ORDER_TYPE_TRIAL_ADDITIONAL_DIRECTIONS_CLINICAL_NEGLIGENCE,

    @JsonProperty("OrderTypeTrialAdditionalDirectionsCreditHire")
    ORDER_TYPE_TRIAL_ADDITIONAL_DIRECTIONS_CREDIT_HIRE,

    @JsonProperty("OrderTypeTrialAdditionalDirectionsEmployersLiability")
    ORDER_TYPE_TRIAL_ADDITIONAL_DIRECTIONS_EMPLOYERS_LIABILITY,

    @JsonProperty("OrderTypeTrialAdditionalDirectionsHousingDisrepair")
    ORDER_TYPE_TRIAL_ADDITIONAL_DIRECTIONS_HOUSING_DISREPAIR,

    @JsonProperty("OrderTypeTrialAdditionalDirectionsPersonalInjury")
    ORDER_TYPE_TRIAL_ADDITIONAL_DIRECTIONS_PERSONAL_INJURY,

    @JsonProperty("OrderTypeTrialAdditionalDirectionsRoadTrafficAccident")
    ORDER_TYPE_TRIAL_ADDITIONAL_DIRECTIONS_ROAD_TRAFFIC_ACCIDENT,

    @JsonProperty("OrderTypeTrialAdditionalDirectionsPPI")
    ORDER_TYPE_TRIAL_ADDITIONAL_DIRECTIONS_PPI,

}
