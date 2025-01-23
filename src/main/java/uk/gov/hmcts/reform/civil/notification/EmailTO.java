package uk.gov.hmcts.reform.civil.notification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.util.Map;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class EmailTO {

    private CaseData caseData;
    private String emailTemplate;

    private String applicantSol1Email;
    private Map<String, String> applicantSol1Params;
    private String applicantRef;

    private String respondentSol1Email;
    private Map<String, String> respondentSol1Params;
    private String respondentRef;

    private String respondentSol2Email;
    private Map<String, String> respondentSol2Params;
    private Boolean canSendEmailToRespondentSol2;
}
