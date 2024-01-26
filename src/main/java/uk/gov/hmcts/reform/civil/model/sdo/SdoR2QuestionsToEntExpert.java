package uk.gov.hmcts.reform.civil.model.sdo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class SdoR2QuestionsToEntExpert {

    private String sdoWrittenQuestionsTxt;
    private LocalDate sdoWrittenQuestionsDate;
    private String sdoWrittenQuestionsDigPortalTxt;
    private String sdoQuestionsShallBeAnsweredTxt;
    private LocalDate sdoQuestionsShallBeAnsweredDate;
    private String sdoShallBeUploadedTxt;

}
