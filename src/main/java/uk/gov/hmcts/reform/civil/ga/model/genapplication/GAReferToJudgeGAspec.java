package uk.gov.hmcts.reform.civil.ga.model.genapplication;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Data
@Accessors(chain = true)
@NoArgsConstructor
public class GAReferToJudgeGAspec {

    private String judgeReferEventDescription;
    private String judgeReferAdditionalInfo;

    @JsonCreator
    GAReferToJudgeGAspec(@JsonProperty("judgeReferEventDescription") String judgeReferEventDescription,
                         @JsonProperty("judgeReferAdditionalInfo") String  judgeReferAdditionalInfo) {

        this.judgeReferEventDescription = judgeReferEventDescription;
        this.judgeReferAdditionalInfo = judgeReferAdditionalInfo;
    }
}
