package uk.gov.hmcts.reform.civil.model.genapplication.finalorder;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.Setter;
import uk.gov.hmcts.reform.civil.enums.dq.ClaimantDefendantNotAttendingType;

@Setter
@Data
@Builder(toBuilder = true)
public class HeardDefendantTwoNotAttend {

    private final ClaimantDefendantNotAttendingType listDefTwo;

    @JsonCreator
    HeardDefendantTwoNotAttend(@JsonProperty("listDefTwo") ClaimantDefendantNotAttendingType listDefTwo) {
        this.listDefTwo = listDefTwo;
    }
}
