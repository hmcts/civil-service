package uk.gov.hmcts.reform.civil.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.civil.enums.CaseRole;

@Data
@Builder
public class CaseToPostLRspec {

    private final String accessCode;
    private final CaseRole respondentCaseRole;
    private final String respondentEmail;

    @JsonCreator
    public CaseToPostLRspec(@JsonProperty("accessCode") String accessCode,
                            @JsonProperty("respondentCaseRole") CaseRole respondentCaseRole,
                            @JsonProperty("respondentEmail") String respondentEmail) {
        this.accessCode = accessCode;
        this.respondentCaseRole = respondentCaseRole;
        this.respondentEmail = respondentEmail;
    }
}
