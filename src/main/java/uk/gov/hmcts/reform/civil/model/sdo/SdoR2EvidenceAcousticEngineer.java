package uk.gov.hmcts.reform.civil.model.sdo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class SdoR2EvidenceAcousticEngineer {

    private String sdoEvidenceAcousticEngineerTxt;
    private String sdoInstructionOfTheExpertTxt;
    private Date sdoInstructionOfTheExpertDate;
    private String sdoInstructionOfTheExpertTxtArea;
    private String sdoExpertReportTxt;
    private Date sdoExpertReportDate;
    private String sdoExpertReportDigitalPortalTxt;
    private String sdoWrittenQuestionsTxt;
    private Date sdoWrittenQuestionsDate;
    private String sdoWrittenQuestionsDigitalPortalTxt;
    private String sdoRepliesTxt;
    private Date sdoRepliesDate;
    private String sdoRepliesDigitalPortalTxt;
    private String sdoServiceOfOrderTxt;

}
