package uk.gov.hmcts.reform.civil.model.sdo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class JudgementSum {

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Double judgementSum;

    @JsonCreator
    public JudgementSum(@JsonProperty("judgementSum") Double judgementSum) {
        this.judgementSum = judgementSum;
    }
}
