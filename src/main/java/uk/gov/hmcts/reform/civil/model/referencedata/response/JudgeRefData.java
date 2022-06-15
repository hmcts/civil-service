package uk.gov.hmcts.reform.civil.model.referencedata.response;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
public class JudgeRefData {

    private String title;
    private String knownAs;
    private String surname;
    private String fullName;
    private String emailId;
    private String idamId;
    private String personalCode;
    private String isJudge;
    private String isPanelMember;
    private String isMagistrate;

    @JsonCreator
    public JudgeRefData(String title, String knownAs, String surname, String fullName, String emailId, String idamId, String personalCode, String isJudge, String isPanelMember, String isMagistrate) {
        this.title = title;
        this.knownAs = knownAs;
        this.surname = surname;
        this.fullName = fullName;
        this.emailId = emailId;
        this.idamId = idamId;
        this.personalCode = personalCode;
        this.isJudge = isJudge;
        this.isPanelMember = isPanelMember;
        this.isMagistrate = isMagistrate;
    }
}
