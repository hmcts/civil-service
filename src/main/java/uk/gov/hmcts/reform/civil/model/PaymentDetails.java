package uk.gov.hmcts.reform.civil.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import uk.gov.hmcts.reform.civil.enums.PaymentStatus;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class PaymentDetails {

    @CCD(
            label = "Payment status",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "PaymentStatus"
    )
    private PaymentStatus status;
    @CCD(label = "Payment reference returned from successful Payments API call", searchable = false)
    private String reference;
    @CCD(label = "Payment error message returned from failed Payments API call", searchable = false)
    private String errorMessage;
    @CCD(label = "Payment error code returned from failed Payments API call", searchable = false)
    private String errorCode;
    @CCD(label = "Customer reference", searchable = false)
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
