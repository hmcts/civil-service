package uk.gov.hmcts.reform.civil.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.payments.response.CardPaymentServiceRequestResponse;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CardPaymentStatusResponse {

    private String externalReference;
    private String paymentReference;
    private String status;
    private String nextUrl;
    private OffsetDateTime dateCreated;
    private String errorCode;
    private String errorDescription;
    private String paymentFor;
    private BigDecimal paymentAmount;

    public static CardPaymentStatusResponse from(CardPaymentServiceRequestResponse cardPaymentServiceRequestResponse) {
        return CardPaymentStatusResponse.builder()
            .paymentReference(cardPaymentServiceRequestResponse.getPaymentReference())
            .externalReference(cardPaymentServiceRequestResponse.getExternalReference())
            .status(cardPaymentServiceRequestResponse.getStatus())
            .nextUrl(cardPaymentServiceRequestResponse.getNextUrl())
            .dateCreated(cardPaymentServiceRequestResponse.getDateCreated())
            .build();
    }
}
