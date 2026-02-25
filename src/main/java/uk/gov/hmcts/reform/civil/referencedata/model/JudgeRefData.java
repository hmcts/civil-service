package uk.gov.hmcts.reform.civil.referencedata.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
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

    public JudgeRefData() {
        // default constructor for frameworks/tests
    }

    @JsonCreator
    public JudgeRefData(@JsonProperty("post_nominals") String title,
                        @JsonProperty("known_as") String knownAs,
                        @JsonProperty("surname") String surname,
                        @JsonProperty("full_name") String fullName,
                        @JsonProperty("ejudiciary_email") String emailId,
                        @JsonProperty("sidam_id") String idamId,
                        @JsonProperty("personal_code") String personalCode,
                        @JsonProperty("is_judge") String isJudge,
                        @JsonProperty("is_panel_member") String isPanelMember,
                        @JsonProperty("is_magistrate") String isMagistrate) {
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
