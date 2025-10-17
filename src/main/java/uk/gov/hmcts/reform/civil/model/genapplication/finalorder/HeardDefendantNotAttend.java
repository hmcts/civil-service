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
public class HeardDefendantNotAttend {

    private final ClaimantDefendantNotAttendingType listDef;

    @JsonCreator
    HeardDefendantNotAttend(@JsonProperty("listDef") ClaimantDefendantNotAttendingType listDef) {
        this.listDef = listDef;
    }
}
