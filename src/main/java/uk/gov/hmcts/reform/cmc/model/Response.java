package uk.gov.hmcts.reform.cmc.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.minidev.json.annotate.JsonIgnore;
import uk.gov.hmcts.reform.civil.enums.DefenceType;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseType;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Response {

    public RespondentResponseType responseType;
    public DefenceType defenceType;
    private PaymentIntention paymentIntention;

    @JsonIgnore
    public boolean isResponseFullAdmit() {
        return responseType == RespondentResponseType.FULL_ADMISSION;
    }
    @JsonIgnore
    public boolean isFullAdmitPayImmediately() {
       return isResponseFullAdmit() && paymentIntention.isPayImmediately();
    }

    @JsonIgnore
    public boolean isFullAdmitPayBySetDate() {
        return isResponseFullAdmit()  && paymentIntention.isPayByDate();
    }

    @JsonIgnore
    public boolean isFullAdmitPayByInstallments() {
        return isResponseFullAdmit() && paymentIntention.isPayByInstallments();
    }
}
