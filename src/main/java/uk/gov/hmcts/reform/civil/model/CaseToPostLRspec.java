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
    // private final LocalDate expiryDate;
    // private final String citizenCaseRole;
    // private final Boolean isAlreadyUse;

    @JsonCreator
    public CaseToPostLRspec(@JsonProperty("accessCode") String accessCode,
                            @JsonProperty("respondentCaseRole") CaseRole respondentCaseRole
                            ) {
        this.accessCode = accessCode;
        this.respondentCaseRole = respondentCaseRole;
    }
}
