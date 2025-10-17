package uk.gov.hmcts.reform.civil.model.genapplication;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.Setter;
import uk.gov.hmcts.reform.civil.enums.dq.GAJudgeDecisionOption;

@Setter
@Data
@Builder(toBuilder = true)
public class GAJudicialDecision {

    private final GAJudgeDecisionOption decision;

    @JsonCreator
    public GAJudicialDecision(@JsonProperty("decision") GAJudgeDecisionOption decision) {
        this.decision = decision;
    }
}
