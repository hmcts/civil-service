package uk.gov.hmcts.reform.civil.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;

import java.time.LocalDate;

@Data
@Builder
public class CaseToPostLRspec {

    private final String accessCode;
    private final String respondentCaseRole;
    private final LocalDate expiryDate;
    private final String citizenCaseRole;
    private final YesOrNo pinUsedFlg;

    @JsonCreator
    public CaseToPostLRspec(@JsonProperty("accessCode") String accessCode,
                            @JsonProperty("respondentCaseRole") String respondentCaseRole,
                            @JsonProperty("expiryDate") LocalDate expiryDate,
                            @JsonProperty("citizenCaseRole") String citizenCaseRole,
                            @JsonProperty("pinUsedFlg") YesOrNo pinUsedFlg
                            ) {
        this.accessCode = accessCode;
        this.respondentCaseRole = respondentCaseRole;
        this.expiryDate = expiryDate;
        this.citizenCaseRole = citizenCaseRole;
        this.pinUsedFlg = pinUsedFlg;
    }
}
