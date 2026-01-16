package uk.gov.hmcts.reform.civil.ga.model.genapplication;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uk.gov.hmcts.reform.civil.model.Fee;
import uk.gov.hmcts.reform.civil.model.PaymentDetails;

import java.time.LocalDateTime;

@Setter
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
public class GeneralApplicationPbaDetails {

    private Fee fee;
    private PaymentDetails paymentDetails;
    private LocalDateTime paymentSuccessfulDate;
    private String serviceReqReference;
    private String additionalPaymentServiceRef;
    private PaymentDetails additionalPaymentDetails;

    @JsonCreator
    GeneralApplicationPbaDetails(
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
