package uk.gov.hmcts.reform.civil.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum FurtherEvidenceDocumentType {

    @JsonProperty("consentOrder")
    consentOrder("consentOrder", "Consent order"),
    @JsonProperty("miamCertificate")
    miamCertificate("miamCertificate", "MIAM certificate"),
    @JsonProperty("previousOrders")
    previousOrders("previousOrders", "Previous orders");

    private final String id;
    private final String displayedValue;

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    @JsonCreator
    public static FurtherEvidenceDocumentType getValue(String key) {
        return FurtherEvidenceDocumentType.valueOf(key);
    }
}
