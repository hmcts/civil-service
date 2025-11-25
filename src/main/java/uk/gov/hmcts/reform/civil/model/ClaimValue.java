package uk.gov.hmcts.reform.civil.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import uk.gov.hmcts.reform.civil.utils.MonetaryConversions;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
public class ClaimValue {

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal statementOfValueInPennies;

    @JsonCreator
    public ClaimValue(@JsonProperty("statementOfValueInPennies") BigDecimal statementOfValueInPennies) {
        this.statementOfValueInPennies = statementOfValueInPennies;
    }

    public BigDecimal toPounds() {
        return MonetaryConversions.penniesToPounds(this.statementOfValueInPennies);
    }

    public String formData() {
        return "up to £" + this.toPounds();
    }
}
