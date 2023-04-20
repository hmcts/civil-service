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
        return responseType == RespondentResponseType.FULL_ADMISSION;
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
}
