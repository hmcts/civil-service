package uk.gov.hmcts.reform.civil.model;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.civil.enums.PaymentStatus;

@Data
@Builder(toBuilder = true)
public class PaymentDetails {

    private PaymentStatus status;
    private String reference;
    private String errorMessage;
    private String errorCode;
    private String customerReference;
}
