package uk.gov.hmcts.reform.civil.enums;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "GARadioYesOrNo", generate = true)
@Getter
@RequiredArgsConstructor
public enum YesOrNo {
    @JsonProperty("Yes")
    YES("Yes"),
    @JsonProperty("No")
    NO("No");

    private final String label;
}
