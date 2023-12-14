package uk.gov.hmcts.reform.civil.enums.hearing;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PartyRole {
    CLAIMANT_ROLE("CLAI"),
    DEFENDANT_ROLE("DEFE"),
    LITIGATION_FRIEND_ROLE("LIFR"),
    LEGAL_REP_ROLE("LGRP"),
    EXPERT_ROLE("EXPR"),
    WITNESS_ROLE("WITN");

    @JsonValue
    private final String partyRoleValue;
}
