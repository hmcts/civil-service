package uk.gov.hmcts.reform.civil.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Data
@Builder(toBuilder = true)
public class HearingFeeServiceRequestDetails {

    private final String serviceRequestReference;
    private final Fee fee;
    private final PaymentDetails paymentDetails;
    private final LocalDateTime paymentSuccessfulDate;

    @JsonCreator
    HearingFeeServiceRequestDetails(@JsonProperty("serviceRequestReference") String serviceRequestReference,
                                    @JsonProperty("fee") Fee fee,
                                    @JsonProperty("paymentDetails") PaymentDetails paymentDetails,
                                    @JsonProperty("paymentSuccessfulDate") LocalDateTime paymentSuccessfulDate) {
        this.serviceRequestReference = serviceRequestReference;
        this.fee = fee;
        this.paymentDetails = paymentDetails;
        this.paymentSuccessfulDate = paymentSuccessfulDate;
    }
}
