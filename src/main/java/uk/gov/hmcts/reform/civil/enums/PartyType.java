package uk.gov.hmcts.reform.civil.enums;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;

@Getter
@AllArgsConstructor
public enum PartyType implements HasLabel {

    @JsonProperty("INDIVIDUAL")
    INDIVIDUAL("Individual"),

    @JsonProperty("COMPANY")
    COMPANY("Company"),

    @JsonProperty("ORGANISATION")
    ORGANISATION("Organisation"),

    @JsonProperty("SOLE_TRADER")
    SOLE_TRADER("Sole trader");

    private final String label;
}

