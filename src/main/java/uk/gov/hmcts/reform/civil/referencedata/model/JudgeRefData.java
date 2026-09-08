package uk.gov.hmcts.reform.civil.referencedata.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class JudgeRefData {

    @JsonAlias("post_nominals")
    private String title;
    @JsonAlias("known_as")
    private String knownAs;
    private String surname;
    @JsonAlias("full_name")
    private String fullName;
    @JsonAlias("ejudiciary_email")
    private String emailId;
    @JsonAlias("sidam_id")
    private String idamId;
    @JsonAlias("personal_code")
    private String personalCode;
    @JsonAlias("is_judge")
    private String isJudge;
    @JsonAlias("is_panel_member")
    private String isPanelMember;
    @JsonAlias("is_magistrate")
    private String isMagistrate;

    public JudgeRefData() {
        // default constructor for frameworks/tests
    }
}
