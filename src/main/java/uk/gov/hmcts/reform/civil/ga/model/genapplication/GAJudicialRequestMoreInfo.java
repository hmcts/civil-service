package uk.gov.hmcts.reform.civil.ga.model.genapplication;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.ga.enums.dq.GAJudgeRequestMoreInfoOption;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Setter
@Data
@Accessors(chain = true)
@NoArgsConstructor
public class GAJudicialRequestMoreInfo {

    private GAJudgeRequestMoreInfoOption requestMoreInfoOption;
    private String judgeRequestMoreInfoText;
    private LocalDate judgeRequestMoreInfoByDate;
    private LocalDateTime deadlineForMoreInfoSubmission;
    private YesOrNo isWithNotice;
    private String judgeRecitalText;

    @JsonCreator
    GAJudicialRequestMoreInfo(@JsonProperty("requestMoreInfoOption")
                                  GAJudgeRequestMoreInfoOption requestMoreInfoOption,
                              @JsonProperty("judgeRequestMoreInfoText")
                                  String judgeRequestMoreInfoText,
                              @JsonProperty("judgeRequestMoreInfoByDate")
                                  LocalDate judgeRequestMoreInfoByDate,
                              @JsonProperty("deadlineForMoreInfoSubmission")
                                  LocalDateTime deadlineForMoreInfoSubmission,
                              @JsonProperty("isWithNotice")
                                  YesOrNo isWithNotice,
                              @JsonProperty("judgeRecitalText") String judgeRecitalText) {
        this.requestMoreInfoOption = requestMoreInfoOption;
        this.judgeRequestMoreInfoText = judgeRequestMoreInfoText;
        this.judgeRequestMoreInfoByDate = judgeRequestMoreInfoByDate;
        this.deadlineForMoreInfoSubmission = deadlineForMoreInfoSubmission;
        this.isWithNotice = isWithNotice;
        this.judgeRecitalText = judgeRecitalText;
    }
}
