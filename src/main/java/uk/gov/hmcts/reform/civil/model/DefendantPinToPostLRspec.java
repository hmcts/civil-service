package uk.gov.hmcts.reform.civil.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDate;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@Data
@NoArgsConstructor
@Accessors(chain = true)
public class DefendantPinToPostLRspec {

    @CCD(label = "Access Code", searchable = false)
    private String accessCode;
    @CCD(label = "Case Role", searchable = false)
    private String respondentCaseRole;
    @CCD(label = "Expiry Date", searchable = false)
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate expiryDate;
    @CCD(label = "Citizen Case Role", searchable = false)
    private String citizenCaseRole;

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
