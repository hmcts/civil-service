package uk.gov.hmcts.reform.civil.enums;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;

@Getter
@AllArgsConstructor
public enum PartyTypeSpec implements HasLabel {

    @JsonProperty("individual")
    INDIVIDUAL("INDIVIDUAL"),

    @JsonProperty("company")
    COMPANY("COMPANY"),

    @JsonProperty("organisation")
    ORGANISATION("ORGANISATION"),

    @JsonProperty("soleTrader")
    SOLE_TRADER("SOLE_TRADER");

    private final String label;
}

