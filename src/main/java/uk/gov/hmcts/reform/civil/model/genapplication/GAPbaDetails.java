package uk.gov.hmcts.reform.civil.model.genapplication;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import uk.gov.hmcts.reform.civil.model.Fee;
import uk.gov.hmcts.reform.civil.model.PaymentDetails;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Accessors(chain = true)
public class GAPbaDetails {

    private Fee fee;
    private PaymentDetails paymentDetails;
    private LocalDateTime paymentSuccessfulDate;
    private String generalAppFeeToPayInText;
    private String generalAppPayInformationText;
    private String serviceReqReference;
    private String additionalPaymentServiceRef;
    private PaymentDetails additionalPaymentDetails;

    @JsonCreator
    GAPbaDetails(@JsonProperty("fee") Fee fee,
                 @JsonProperty("paymentDetails") PaymentDetails paymentDetails,
                 @JsonProperty("paymentSuccessfulDate") LocalDateTime paymentSuccessfulDate,
                 @JsonProperty("generalAppFeeToPayInText") String generalAppFeeToPayInText,
                 @JsonProperty("generalAppPayInformationText") String generalAppPayInformationText,
                 @JsonProperty("serviceRequestReference") String serviceReqReference,
                 @JsonProperty("additionalPaymentServiceRef") String additionalPaymentServiceRef,
                 @JsonProperty("additionalPaymentDetails") PaymentDetails additionalPaymentDetails) {

        this.fee = fee;
        this.paymentDetails = paymentDetails;
        this.paymentSuccessfulDate = paymentSuccessfulDate;
        this.generalAppFeeToPayInText = generalAppFeeToPayInText;
        this.generalAppPayInformationText = generalAppPayInformationText;
        this.serviceReqReference = serviceReqReference;
        this.additionalPaymentServiceRef = additionalPaymentServiceRef;
        this.additionalPaymentDetails = additionalPaymentDetails;
    }
}
