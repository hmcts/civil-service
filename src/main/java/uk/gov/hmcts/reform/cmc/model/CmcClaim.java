package uk.gov.hmcts.reform.cmc.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.minidev.json.annotate.JsonIgnore;
import uk.gov.hmcts.reform.civil.model.citizenui.DashboardClaimStatus;
import uk.gov.hmcts.reform.civil.model.CountyCourtJudgment;

import java.math.BigDecimal;
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
    private BigDecimal totalAmountTillToday;
    @JsonProperty("claim")
    private ClaimData claimData;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonDeserialize(using = LocalDateDeserializer.class)
    private LocalDate responseDeadline;
    private boolean moreTimeRequested;
    private String submitterEmail;

    public Response response;
    private LocalDate moneyReceivedOn;
    private LocalDateTime countyCourtJudgmentRequestedAt;
    private CountyCourtJudgment countyCourtJudgment;
    private LocalDate admissionPayImmediatelyPastPaymentDate;
    private ClaimantResponse claimantResponse;
    private LocalDateTime reDeterminationRequestedAt;
    private ClaimState state;

    private DashboardClaimStatus status;

    public String getClaimantName() {
        return claimData.getClaimantName();
    }

    public String getDefendantName() {
        return claimData.getDefendantName();
    }

    @JsonIgnore
    public boolean hasResponse(){
        return response != null;
    }
   @JsonIgnore
    public boolean isTransferred(){
        return state == ClaimState.TRANSFERRED;
    }

    @JsonIgnore
    public boolean isEligibleForCCJ() {
        return countyCourtJudgment == null
            && hasResponseDeadlinePassed();
    }

    @JsonIgnore
    public boolean claimantAcceptedDefendantResponse(){
        return claimantResponse != null
            && claimantResponse == ClaimantResponse.ACCEPTATION;
    }

    @JsonIgnore
    public boolean responseIsFullAdmitAndPayImmediately(){
       return hasResponse()
           && response.isFullAdmitPayImmediately();
    }

    @JsonIgnore
    public boolean responseIsFullAdmitAndPayBySetDate() {
        return hasResponse() && response.isFullAdmitPayBySetDate();
    }

    @JsonIgnore
    public boolean responseIsFullAdmitAndPayByInstallments() {
        return hasResponse() && response.isFullAdmitPayImmediately();
    }

    @JsonIgnore
    public boolean hasResponseDeadlinePassed(){
        return !hasResponse() && (getResponseDeadline().isAfter(LocalDate.now())
            || isResponseIsPastFourPmToday());
    }

    @JsonIgnore
    public boolean isResponseDeadlineToday() {
        return !hasResponse() && getResponseDeadline().isEqual(LocalDate.now())
            && LocalDateTime.now().isBefore(LocalDate.now().atTime(16, 0, 0));
    }

    @JsonIgnore
    public boolean isResponseDeadlineOnTime() {
        return !hasResponse() && getResponseDeadline().isBefore(LocalDate.now());
    }
    @JsonIgnore
    public boolean hasBreathingSpace() {
        return claimData.hasBreathingSpace();
    }

    private boolean isResponseIsPastFourPmToday() {
        return getResponseDeadline().isEqual(LocalDate.now())
            && LocalDateTime.now().isAfter(LocalDate.now().atTime(16, 0, 0));
    }
}
