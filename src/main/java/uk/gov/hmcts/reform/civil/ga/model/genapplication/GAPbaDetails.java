package uk.gov.hmcts.reform.civil.ga.model.genapplication;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.Setter;
import uk.gov.hmcts.reform.civil.model.Fee;
import uk.gov.hmcts.reform.civil.model.PaymentDetails;

import java.time.LocalDateTime;

@Setter
@Data
@Builder(toBuilder = true)
public class GAPbaDetails {

    private final Fee fee;
    private final PaymentDetails paymentDetails;
    private final LocalDateTime paymentSuccessfulDate;
    private final String serviceReqReference;
    private final String additionalPaymentServiceRef;
    private final PaymentDetails additionalPaymentDetails;

    @JsonCreator
    GAPbaDetails(
        @JsonProperty("fee") Fee fee,
                 @JsonProperty("paymentDetails") PaymentDetails paymentDetails,
                 @JsonProperty("paymentSuccessfulDate") LocalDateTime paymentSuccessfulDate,
                 @JsonProperty("serviceRequestReference") String serviceReqReference,
                 @JsonProperty("additionalPaymentServiceRef") String additionalPaymentServiceRef,
                 @JsonProperty("additionalPaymentDetails") PaymentDetails additionalPaymentDetails) {
        this.fee = fee;
        this.paymentDetails = paymentDetails;
        this.paymentSuccessfulDate = paymentSuccessfulDate;
        this.serviceReqReference = serviceReqReference;
        this.additionalPaymentServiceRef = additionalPaymentServiceRef;
        this.additionalPaymentDetails = additionalPaymentDetails;
    }
}
