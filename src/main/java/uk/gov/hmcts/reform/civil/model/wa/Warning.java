package uk.gov.hmcts.reform.civil.model.wa;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(
    name = "Warning",
    description = "Warning object containing the field to sort on and the order"
)
@EqualsAndHashCode
@ToString
public class Warning {

    @Schema(
        name = "warning_code",
        description = "A code that distinguishes which Warning is to be applied ")
    private final String warningCode;

    @Schema(
        name = "warning_text",
        description = "Text associated to the warning code that can be shown in the UI")
    private final String warningText;

    @JsonCreator
    public Warning(@JsonProperty("warningCode") String warningCode, @JsonProperty("warningText") String warningText) {
        this.warningCode = warningCode;
        this.warningText = warningText;
    }

    public String getWarningCode() {
        return warningCode;
    }

    public String getWarningText() {
        return warningText;
    }
}
