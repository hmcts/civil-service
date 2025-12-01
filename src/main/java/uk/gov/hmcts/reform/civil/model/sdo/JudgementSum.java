package uk.gov.hmcts.reform.civil.model.sdo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
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
