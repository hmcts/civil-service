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
public class SdoR2QuestionsClaimantExpert {

    private String sdoDefendantMayAskTxt;
    private Date sdoDefendantMayAskDate;
    private String sdoQuestionsShallBeAnsweredTxt;
    private Date sdoQuestionsShallBeAnsweredDate;
    private String sdoUploadedToDigitalPortalTxt;
    SdoR2ApplicationToRelyOnFurther
}
