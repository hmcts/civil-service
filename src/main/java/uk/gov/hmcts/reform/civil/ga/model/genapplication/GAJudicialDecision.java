package uk.gov.hmcts.reform.civil.ga.model.genapplication;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.civil.ga.enums.dq.GAJudgeDecisionOption;

@Setter
@Data
@Accessors(chain = true)
@NoArgsConstructor
public class GAJudicialDecision {

    private GAJudgeDecisionOption decision;

    @JsonCreator
    public GAJudicialDecision(@JsonProperty("decision") GAJudgeDecisionOption decision) {
        this.decision = decision;
    }
}
