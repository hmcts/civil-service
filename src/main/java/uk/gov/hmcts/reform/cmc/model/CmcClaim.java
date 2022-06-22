package uk.gov.hmcts.reform.cmc.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CmcClaim {

    private String submitterId;
    private String letterHolderId;
    private String defendantId;
    private String externalId;
    private String referenceNumber;
    @JsonProperty("claim")
    private ClaimData claimData;
    private LocalDateTime createdAt;
    private LocalDate issuedOn;
    private LocalDate responseDeadline;
    private boolean moreTimeRequested;
    private String submitterEmail;

    public String getClaimantName(){
        return claimData.getClaimantName();
    }

    public String getDefendantName(){
        return claimData.getDefendantName();
    }
}
