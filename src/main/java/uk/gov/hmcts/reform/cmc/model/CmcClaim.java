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
import java.util.Objects;
import java.util.Optional;

import static uk.gov.hmcts.reform.civil.model.citizenui.DtoFieldFormat.DATE_TIME_FORMAT;
import static uk.gov.hmcts.reform.civil.model.citizenui.DtoFieldFormat.DATE_FORMAT;
import static uk.gov.hmcts.reform.civil.model.citizenui.DtoFieldFormat.DATE_TIME_FORMAT_CMC;

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

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime countyCourtJudgmentRequestedAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime createdAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime reDeterminationRequestedAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATE_FORMAT)
    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonDeserialize(using = LocalDateDeserializer.class)
    private LocalDate admissionPayImmediatelyPastPaymentDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATE_FORMAT)
    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonDeserialize(using = LocalDateDeserializer.class)
    private LocalDate intentionToProceedDeadline;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime claimantRespondedAt;

    private ClaimantResponse claimantResponse;
    private ClaimState state;
    private ProceedOfflineReasonType proceedOfflineReason;
    private Settlement settlement;

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
        return !hasResponse()
            && getResponseDeadline() != null
            && getResponseDeadline().isAfter(LocalDate.now());
    }

    @Override
    @JsonIgnore
    public boolean hasResponsePendingOverdue() {
        return hasResponseDeadlinePassed() && hasBreathingSpace();
    }

    @Override
    @JsonIgnore
    public boolean hasResponseDueToday() {
        return !hasResponse()
            && getResponseDeadline() != null
            && getResponseDeadline().isEqual(LocalDate.now())
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
    public boolean hasResponseDeadlineBeenExtended() {
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
        return moneyReceivedOn != null || (claimantAcceptedDefendantResponse() && !hasCCJByRedetermination());
    }

    @Override
    @JsonIgnore
    public boolean isSentToCourt() {
        return isTransferred();
    }

    @Override
    @JsonIgnore
    public boolean claimantRequestedCountyCourtJudgement() {
        return getCountyCourtJudgmentRequestedAt() != null;
    }

    @Override
    @JsonIgnore
    public boolean isWaitingForClaimantToRespond() {
        return hasResponse() && response.isFullDefence() && claimantResponse == null;
    }

    @Override
    @JsonIgnore
    public boolean isProceedOffline() {
        return ProceedOfflineReasonType.OTHER == proceedOfflineReason;
    }

    @Override
    public boolean isPaperResponse() {
        return hasResponse() && response.isPaperResponse();
    }

    @Override
    @JsonIgnore
    public boolean hasChangeRequestFromDefendant() {
        return ProceedOfflineReasonType.APPLICATION_BY_DEFENDANT == proceedOfflineReason;
    }

    @Override
    @JsonIgnore
    public boolean hasChangeRequestedFromClaimant() {
        return ProceedOfflineReasonType.APPLICATION_BY_CLAIMANT == proceedOfflineReason;
    }

    @Override
    @JsonIgnore
    public boolean isPassedToCountyCourtBusinessCentre() {
        return state == ClaimState.BUSINESS_QUEUE;
    }

    @Override
    @JsonIgnore
    public boolean hasClaimantAskedToSignSettlementAgreement() {
        return hasResponse() && settlement != null && settlement.isAcceptedByClaimant();
    }

    @Override
    public boolean hasClaimantSignedSettlementAgreement() {
        return hasClaimantSignedSettlementAgreementOfferAccepted() || hasClaimantSignedSettlementAgreementChosenByCourt();
    }

    private boolean hasClaimantSignedSettlementAgreementOfferAccepted() {
        return Objects.nonNull(settlement) && settlement.isOfferAccepted() && settlement.isThroughAdmissions()
                && Objects.nonNull(claimantResponse) && !claimantResponse.hasCourtDetermination();
    }

    private boolean hasClaimantSignedSettlementAgreementChosenByCourt() {
        return Objects.nonNull(settlement) && settlement.isOfferAccepted() && !settlement.isRejectedByDefendant() && settlement.isThroughAdmissions()
                && Objects.nonNull(claimantResponse) && claimantResponse.hasCourtDetermination();
    }

    @Override
    public boolean hasClaimantSignedSettlementAgreementAndDeadlineExpired() {
        return Objects.nonNull(settlement) && settlement.isOfferAccepted() && settlement.isThroughAdmissions()
                && Objects.nonNull(claimantRespondedAt) && claimantRespondedAt.plusDays(7).isBefore(LocalDateTime.now());
    }

    @Override
    public boolean hasClaimantAndDefendantSignedSettlementAgreement() {
        return Objects.nonNull(settlement) && !settlement.isRejectedByDefendant() && settlement.isSettled() && settlement.isThroughAdmissions();
    }

    @Override
    public boolean hasDefendantRejectedSettlementAgreement() {
        if (!Objects.nonNull(claimantResponse) || !ClaimantResponseType.ACCEPTATION.equals(claimantResponse.getType())) {
            return false;
        }
        return claimantResponse.getFormaliseOption() == FormaliseOption.SETTLEMENT
                && Objects.nonNull(settlement) && settlement.isOfferRejected();
    }

    @Override
    @JsonIgnore
    public boolean hasClaimantAcceptedPartialAdmissionAmount() {
        return hasResponse() && response.isPartAdmitPayImmediately()
            && claimantAcceptedDefendantResponse() && !hasCCJByRedetermination();
    }

    @Override
    @JsonIgnore
    public boolean haveBothPartiesSignedSettlementAgreement() {
        return hasClaimantAskedToSignSettlementAgreement() && settlement.isSettled();
    }

    @Override
    @JsonIgnore
    public boolean hasCCJByRedetermination() {
        return reDeterminationRequestedAt != null
            || (hasClaimantResponse() && claimantResponse.hasCourtDetermination())
            || (settlement != null && settlement.isThroughAdmissions() && countyCourtJudgmentRequestedAt != null);
    }

    @Override
    public boolean hasDefendantStatedTheyPaid() {
        return hasResponse() && response.hasPaymentDeclaration();
    }

    @Override
    public boolean defendantRespondedWithPartAdmit() {
        return hasResponse() && response.isPartAdmit() && !hasClaimantResponse();
    }

    @Override
    public boolean isHearingFormGenerated() {
        return false;
    }

    @JsonIgnore
    public boolean claimantAcceptedDefendantResponse() {
        return hasClaimantResponse()
            && claimantResponse.getType() != null
            && claimantResponse.getType() == ClaimantResponseType.ACCEPTATION;

    }

    @JsonIgnore
    public boolean hasResponseDeadlinePassed() {
        return !hasResponse()
            && (getResponseDeadline() != null
            && getResponseDeadline().isBefore(LocalDate.now())
            || isResponseDeadlinePastFourPmToday());
    }

    @JsonIgnore
    public boolean hasBreathingSpace() {
        return claimData != null && claimData.hasBreathingSpace();
    }

    @JsonIgnore
    public LocalDate getBySpecifiedDate() {
        return Optional.ofNullable(getResponse())
            .map(Response::getPaymentIntention)
            .map(PaymentIntention::getPaymentDate).orElse(null);
    }

    @JsonIgnore
    public BigDecimal getAdmittedAmount() {
        return hasResponse() ? response.getAmount() : null;
    }

    private boolean isResponseDeadlinePastFourPmToday() {
        return getResponseDeadline() != null
            && getResponseDeadline().isEqual(LocalDate.now())
            && LocalDateTime.now().isAfter(LocalDate.now().atTime(FOUR_PM));
    }

    private boolean isApplicant1ResponseDeadlineEnded() {
        return Optional.ofNullable(getIntentionToProceedDeadline()).filter(deadline ->
                                                                               deadline.isBefore(LocalDate.now()))
            .isPresent() && !hasClaimantResponse();

    }

    private boolean hasClaimantResponse() {
        return claimantResponse != null;
    }

    @Override
    public boolean hasSdoBeenDrawn() {
        return false;
    }

    @Override
    public boolean isBeforeHearing() {
        return false;
    }

    @Override
    public boolean isMoreDetailsRequired() {
        return false;
    }

    @Override
    public boolean isMediationSuccessful() {
        return false;
    }

    @Override
    public boolean isMediationUnsuccessful() {
        return false;
    }

    @Override
    public boolean isMediationPending() {
        return false;
    }

    @Override
    public boolean isCourtReviewing() {
        return false;
    }

    @Override
    public boolean isSDOOrderCreated() {
        return false;
    }

    @Override
    public boolean hasClaimEnded() {
        return (Objects.nonNull(response)
            && response.isFullDefence()
            && Objects.nonNull(claimantResponse)
            && claimantResponse.getType().equals(ClaimantResponseType.REJECTION))
            || isApplicant1ResponseDeadlineEnded();
    }

    @Override
    public boolean isClaimRejectedAndOfferSettleOutOfCourt() {
        return isFullDefenceWithSubmittedOffer()
            && Objects.isNull(moneyReceivedOn)
            && !settlement.isSettled()
            && !settlement.isThroughAdmissions();
    }

    private boolean isFullDefenceWithSubmittedOffer() {
        return Objects.nonNull(settlement)
            && Objects.nonNull(response)
            && response.isFullDefence();
    }

    @Override
    public boolean claimantAcceptedOfferOutOfCourt() {
        return isClaimRejectedAndOfferSettleOutOfCourt()
            && settlement.isAcceptedByClaimant();
    }

    @Override
    public boolean hasClaimantRejectOffer() {
        return isClaimRejectedAndOfferSettleOutOfCourt()
            && settlement.isRejectedByClaimant();
    }

    @Override
    public boolean isPartialAdmissionRejected() {
        return Objects.nonNull(response)
            && response.isPartAdmit()
            && Objects.nonNull(claimantResponse)
            && claimantResponse.getType().equals(ClaimantResponseType.REJECTION);
    }

    @Override
    public boolean isClaimantDefaultJudgement() {
        return false;
    }
}
