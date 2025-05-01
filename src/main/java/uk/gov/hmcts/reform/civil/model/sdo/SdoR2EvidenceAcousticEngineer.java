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
public class SdoR2EvidenceAcousticEngineer {

    private String sdoEvidenceAcousticEngineerTxt;
    private String sdoInstructionOfTheExpertTxt;
    private LocalDate sdoInstructionOfTheExpertDate;
    private String sdoInstructionOfTheExpertTxtArea;
    private String sdoExpertReportTxt;
    private LocalDate sdoExpertReportDate;
    private String sdoExpertReportDigitalPortalTxt;
    private String sdoWrittenQuestionsTxt;
    private LocalDate sdoWrittenQuestionsDate;
    private String sdoWrittenQuestionsDigitalPortalTxt;
    private String sdoRepliesTxt;
    private LocalDate sdoRepliesDate;
    private String sdoRepliesDigitalPortalTxt;
    private String sdoServiceOfOrderTxt;

}
