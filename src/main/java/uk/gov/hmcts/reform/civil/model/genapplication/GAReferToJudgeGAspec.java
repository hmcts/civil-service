package uk.gov.hmcts.reform.civil.model.genapplication;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.Setter;

@Setter
@Data
@Builder(toBuilder = true)
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
