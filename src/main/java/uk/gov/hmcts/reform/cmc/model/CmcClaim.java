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
import uk.gov.hmcts.reform.civil.enums.*;
import uk.gov.hmcts.reform.civil.model.CountyCourtJudgment;
import uk.gov.hmcts.reform.civil.model.Response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.chrono.ChronoLocalDate;

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
    public LocalDate moneyReceivedOn;
    public LocalDateTime countyCourtJudgmentRequestedAt;
    public CountyCourtJudgment ccj;
    public LocalDate admissionPayImmediatelyPastPaymentDate;
    public ClaimantResponse claimantResponse;
    public LocalDateTime reDeterminationRequestedAt;
    public ClaimState state;

    private DefendantResponseStatus status;

    public String getClaimantName() {
        return claimData.getClaimantName();
    }

    public String getDefendantName() {
        return claimData.getDefendantName();
    }

    public DefendantResponseStatus getDefendantResponseStatus(){
        if(response == null)
            status = DefendantResponseStatus.NO_RESPONSE;
        if(moreTimeRequested)
            status = DefendantResponseStatus.MORE_TIME_REQUESTED;
        if(isEligibleForCCJ())
            status = DefendantResponseStatus.ELIGIBLE_FOR_CCJ;
        if(moneyReceivedOn != null && countyCourtJudgmentRequestedAt != null && isCCJPaidWithinMonth())
            status = DefendantResponseStatus.PAID_IN_FULL_CCJ_CANCELLED;
        if(moneyReceivedOn != null && countyCourtJudgmentRequestedAt != null)
            status = DefendantResponseStatus.PAID_IN_FULL_CCJ_SATISFIED;
        if(moneyReceivedOn != null)
            status = DefendantResponseStatus.PAID_IN_FULL;
        if(admissionPayImmediatelyPastPaymentDate != null && claimantResponse == null)
            status = DefendantResponseStatus.ELIGIBLE_FOR_CCJ_AFTER_FULL_ADMIT_PAY_IMMEDIATELY_PAST_DEADLINE;
        if(hasClaimantRespondedStatesPaid())
            status = DefendantResponseStatus.CLAIMANT_ACCEPTED_STATES_PAID;
        if(claimantResponse != null && countyCourtJudgmentRequestedAt != null)
            status = DefendantResponseStatus.REDETERMINATION_BY_JUDGE;
        if(state == ClaimState.TRANSFERRED)
            status = DefendantResponseStatus.TRANSFERRED;

        return status;
    }

    private boolean isEligibleForCCJ() {
        return ccj != null && ccj.paymentDetails != null;
    }

    private boolean hasClaimantRespondedStatesPaid() {
        return claimantResponse != null && claimantResponse == ClaimantResponse.ACCEPTATION &&
            ((response.getResponseType() == RespondentResponseType.PART_ADMISSION && response.paymentDeclaration != null) ||
                (response.getResponseType() == RespondentResponseType.FULL_DEFENCE && response.getDefenceType() == DefenceType.ALREADY_PAID && response.paymentDeclaration != null));
    }

    private boolean isCCJPaidWithinMonth() {
        return moneyReceivedOn.isBefore(ChronoLocalDate.from(countyCourtJudgmentRequestedAt));
    }
}
