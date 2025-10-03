package uk.gov.hmcts.reform.hmc.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum MadeBy {
    @JsonProperty("CLAIMANT")
    CLAIMANT,
    @JsonProperty("DEFENDANT")
    DEFENDANT,
    @JsonProperty("COURT")
    COURT
}
