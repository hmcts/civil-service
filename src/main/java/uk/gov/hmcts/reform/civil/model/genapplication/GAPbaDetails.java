package uk.gov.hmcts.reform.civil.model.genapplication;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import uk.gov.hmcts.reform.civil.model.Fee;
import uk.gov.hmcts.reform.civil.model.PaymentDetails;

import java.time.LocalDateTime;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.reform.civil.model.GAPaymentDetails;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "GAPBADetailsGAspec", generate = true)
@Setter
@Data
@NoArgsConstructor
@Accessors(chain = true)
public class GAPbaDetails {

    @CCD(label = " ", searchable = false)
    private Fee fee;
    @CCD(label = "Details of PBA payment", searchable = false, typeParameterClass = GAPaymentDetails.class)
    private PaymentDetails paymentDetails;
    @CCD(label = "Payment successful date", searchable = false)
    private LocalDateTime paymentSuccessfulDate;
    @CCD(label = "Application fee to pay", hint = "  ", searchable = false)
    private String generalAppFeeToPayInText;
    @CCD(
            label = "You will be able to pay for your application once it has been submitted.",
            hint = "  ",
            searchable = false
    )
    private String generalAppPayInformationText;
    @CCD(ignore = true)
    private String serviceReqReference;
    @CCD(ignore = true)
    private String additionalPaymentServiceRef;
    @CCD(ignore = true)
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
