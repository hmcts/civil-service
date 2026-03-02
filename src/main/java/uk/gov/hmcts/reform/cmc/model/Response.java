package uk.gov.hmcts.reform.cmc.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import net.minidev.json.annotate.JsonIgnore;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseType;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@Data
public class Response {

    private RespondentResponseType responseType;
    private PaymentIntention paymentIntention;
    private BigDecimal amount;
    private PaymentDeclaration paymentDeclaration;
    private ResponseMethod responseMethod;

    @JsonIgnore
    public boolean isFullAdmit() {
        return RespondentResponseType.FULL_ADMISSION == responseType;
    }

    @JsonIgnore
    public boolean isFullDefence() {
        return RespondentResponseType.FULL_DEFENCE == responseType;
    }

    @JsonIgnore
    public boolean isPartAdmit() {
        return RespondentResponseType.PART_ADMISSION == responseType;
    }

    @JsonIgnore
    public boolean isFullAdmitPayImmediately() {
        return isFullAdmit() && paymentIntention != null && paymentIntention.isPayImmediately();
    }

    @JsonIgnore
    public boolean isFullAdmitPayBySetDate() {
        return isFullAdmit() && paymentIntention != null && paymentIntention.isPayByDate();
    }

    @JsonIgnore
    public boolean isFullAdmitPayByInstallments() {
        return isFullAdmit() && paymentIntention != null && paymentIntention.isPayByInstallments();
    }

    @JsonIgnore
    public boolean isPartAdmitPayImmediately() {
        return isPartAdmit() && paymentIntention != null && paymentIntention.isPayImmediately();
    }

    @JsonIgnore
    public boolean isPaymentDateOnTime() {
        return paymentIntention != null && paymentIntention.hasAlreadyPaid();
    }

    @JsonIgnore
    public boolean hasPaymentDeclaration() {
        return paymentDeclaration != null;
    }

    @JsonIgnore
    public boolean isPaperResponse() {
        return ResponseMethod.OFFLINE == responseMethod;
    }
}
