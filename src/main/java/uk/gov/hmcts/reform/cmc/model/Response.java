package uk.gov.hmcts.reform.cmc.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.minidev.json.annotate.JsonIgnore;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseType;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Response {

    public RespondentResponseType responseType;
    private PaymentIntention paymentIntention;

    @JsonIgnore
    public boolean isFullAdmit() {
        return RespondentResponseType.FULL_ADMISSION == responseType;
    }

    @JsonIgnore
    public boolean isFullDefence(){
       return RespondentResponseType.FULL_DEFENCE == responseType;
    }

    @JsonIgnore
    public boolean isPartAdmit() {
        return RespondentResponseType.PART_ADMISSION == responseType;
    }

    @JsonIgnore
    public boolean isFullAdmitPayImmediately() {
        return isFullAdmit() && paymentIntention.isPayImmediately();
    }

    @JsonIgnore
    public boolean isFullAdmitPayBySetDate() {
        return isFullAdmit() && paymentIntention.isPayByDate();
    }

    @JsonIgnore
    public boolean isFullAdmitPayByInstallments() {
        return isFullAdmit() && paymentIntention.isPayByInstallments();
    }

    @JsonIgnore
    public boolean isPartAdmitPayImmediately() {
        return isPartAdmit() && paymentIntention.isPayImmediately();
    }

    @JsonIgnore
    public boolean isPaymentDateOnTime(){
        return paymentIntention != null && paymentIntention.isPaymentDateOnTime();
    }
}
