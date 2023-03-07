package uk.gov.hmcts.reform.cmc.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.minidev.json.annotate.JsonIgnore;
import uk.gov.hmcts.reform.civil.model.citizenui.Claim;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static uk.gov.hmcts.reform.civil.model.citizenui.DtoFieldFormat.DATE_FORMAT;
import static uk.gov.hmcts.reform.civil.model.citizenui.DtoFieldFormat.DATE_TIME_FORMAT;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CmcClaim implements Claim {

    @JsonIgnore
    private static final LocalTime FOUR_PM = LocalTime.of(16, 1, 0);
    private String submitterId;
    private String letterHolderId;
    private String defendantId;
    private String externalId;
    private String referenceNumber;
    private BigDecimal totalAmountTillToday;
    @JsonProperty("claim")
    private ClaimData claimData;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATE_FORMAT)
    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonDeserialize(using = LocalDateDeserializer.class)
    private LocalDate responseDeadline;
    private boolean moreTimeRequested;
    private String submitterEmail;

    public Response response;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATE_FORMAT)
    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonDeserialize(using = LocalDateDeserializer.class)
    private LocalDate moneyReceivedOn;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATE_TIME_FORMAT)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime countyCourtJudgmentRequestedAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATE_FORMAT)
    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonDeserialize(using = LocalDateDeserializer.class)
    private LocalDate admissionPayImmediatelyPastPaymentDate;
    private ClaimantResponse claimantResponse;
    private ClaimState state;

    public String getClaimantName() {
        return claimData.getClaimantName();
    }

    public String getDefendantName() {
        return claimData.getDefendantName();
    }

    @JsonIgnore
    public boolean hasResponse() {
        return response != null;
    }

    @JsonIgnore
    public boolean isTransferred() {
        return state == ClaimState.TRANSFERRED;
    }

    @Override
    @JsonIgnore
    public boolean hasResponsePending() {
        return !hasResponse() && getResponseDeadline().isAfter(LocalDate.now());
    }

    @Override
    @JsonIgnore
    public boolean hasResponsePendingOverdue() {
        return hasResponseDeadlinePassed() && hasBreathingSpace();
    }

    @Override
    @JsonIgnore
    public boolean hasResponseDueToday() {
        return !hasResponse() && getResponseDeadline().isEqual(LocalDate.now())
            && LocalDateTime.now().isBefore(LocalDate.now().atTime(FOUR_PM));
    }

    @Override
    @JsonIgnore
    public boolean hasResponseFullAdmit() {
        return hasResponse() && response.isFullAdmit();
    }

    @Override
    @JsonIgnore
    public boolean defendantRespondedWithFullAdmitAndPayImmediately() {
        return hasResponse() && response.isFullAdmitPayImmediately();
    }

    @Override
    @JsonIgnore
    public boolean defendantRespondedWithFullAdmitAndPayBySetDate() {
        return hasResponse() && response.isFullAdmitPayBySetDate();
    }

    @Override
    @JsonIgnore
    public boolean defendantRespondedWithFullAdmitAndPayByInstallments() {
        return hasResponse() && response.isFullAdmitPayByInstallments();
    }

    @Override
    @JsonIgnore
    public boolean responseDeadlineHasBeenExtended() {
        return hasResponsePending() && isMoreTimeRequested();
    }

    @JsonIgnore
    public boolean isEligibleForCCJ() {
        return hasResponseDeadlinePassed();
    }

    @Override
    @JsonIgnore
    public boolean claimantConfirmedDefendantPaid() {
        return moneyReceivedOn != null && countyCourtJudgmentRequestedAt != null;
    }

    @Override
    @JsonIgnore
    public boolean isSettled() {
        return moneyReceivedOn != null || claimantAcceptedDefendantResponse();
    }

    @Override
    @JsonIgnore
    public boolean isSentToCourt() {
        return isTransferred();
    }

    @Override
    @JsonIgnore
    public boolean claimantRequestedCountyCourtJudgement() {
        return getClaimantResponse() != null && getCountyCourtJudgmentRequestedAt() != null;
    }

    @JsonIgnore
    public boolean claimantAcceptedDefendantResponse() {
        return claimantResponse != null
            && claimantResponse.getType() != null && claimantResponse.getType() == ClaimantResponseType.ACCEPTATION;
    }

    @JsonIgnore
    public boolean hasResponseDeadlinePassed() {
        return !hasResponse() && (getResponseDeadline().isBefore(LocalDate.now())
            || isResponseDeadlinePastFourPmToday());
    }

    @JsonIgnore
    public boolean hasBreathingSpace() {
        return claimData != null && claimData.hasBreathingSpace();
    }

    private boolean isResponseDeadlinePastFourPmToday() {
        return getResponseDeadline().isEqual(LocalDate.now())
            && LocalDateTime.now().isAfter(LocalDate.now().atTime(FOUR_PM));
    }
}
