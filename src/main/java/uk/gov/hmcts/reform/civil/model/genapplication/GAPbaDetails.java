package uk.gov.hmcts.reform.civil.model.genapplication;

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
    private final String generalAppFeeToPayInText;
    private final String generalAppPayInformationText;

    @JsonCreator
    GAPbaDetails(@JsonProperty("fee") Fee fee,
                 @JsonProperty("paymentDetails") PaymentDetails paymentDetails,
                 @JsonProperty("paymentSuccessfulDate") LocalDateTime paymentSuccessfulDate,
                 @JsonProperty("generalAppFeeToPayInText") String generalAppFeeToPayInText,
                @JsonProperty("generalAppPayInformationText") String generalAppPayInformationText) {

        this.fee = fee;
        this.paymentDetails = paymentDetails;
        this.paymentSuccessfulDate = paymentSuccessfulDate;
        this.generalAppFeeToPayInText = generalAppFeeToPayInText;
        this.generalAppPayInformationText = generalAppPayInformationText;
    }
}
