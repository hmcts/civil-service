package uk.gov.hmcts.reform.civil.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class DefendantPinToPostLRspec {

    private final String accessCode;
    private final String respondentCaseRole;
    private final LocalDate expiryDate;
    private final String citizenCaseRole;

    @JsonCreator
    public DefendantPinToPostLRspec(@JsonProperty("accessCode") String accessCode,
                                    @JsonProperty("respondentCaseRole") String respondentCaseRole,
                                    @JsonProperty("expiryDate") LocalDate expiryDate,
                                    @JsonProperty("citizenCaseRole") String citizenCaseRole
    ) {
        this.accessCode = accessCode;
        this.respondentCaseRole = respondentCaseRole;
        this.expiryDate = expiryDate;
        this.citizenCaseRole = citizenCaseRole;
    }
}
