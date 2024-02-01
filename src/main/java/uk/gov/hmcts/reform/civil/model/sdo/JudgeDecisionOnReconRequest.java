package uk.gov.hmcts.reform.civil.model.sdo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class JudgeDecisionOnReconRequest {

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private final String input;

    @JsonCreator
    public JudgeDecisionOnReconRequest(@JsonProperty("judgeDecisionOnReconRequest") String input) {
        this.input = input;
    }
}
