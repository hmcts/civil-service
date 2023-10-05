package uk.gov.hmcts.reform.civil.model.citizenui.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;

public enum RepaymentDecisionType {
    IN_FAVOUR_OF_CLAIMANT,
    IN_FAVOUR_OF_DEFENDANT;

    @JsonIgnore
    public boolean isInFavourOfDefendant() {
        return this == IN_FAVOUR_OF_DEFENDANT;
    }
}
