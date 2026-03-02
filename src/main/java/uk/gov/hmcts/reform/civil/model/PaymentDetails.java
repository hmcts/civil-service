package uk.gov.hmcts.reform.civil.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import uk.gov.hmcts.reform.civil.enums.PaymentStatus;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class PaymentDetails {

    private PaymentStatus status;
    private String reference;
    private String errorMessage;
    private String errorCode;
    private String customerReference;

    public PaymentDetails copy() {
        return new PaymentDetails()
            .setStatus(status)
            .setReference(reference)
            .setErrorMessage(errorMessage)
            .setErrorCode(errorCode)
            .setCustomerReference(customerReference);
    }
}
